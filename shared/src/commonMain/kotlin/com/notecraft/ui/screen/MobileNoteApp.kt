package com.notecraft.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notecraft.data.importexport.FileDialogService
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.ViewMode
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.domain.repository.SettingsRepository
import com.notecraft.presentation.note.*
import com.notecraft.presentation.settings.SettingsViewModel
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.ui.theme.NotecraftTheme
import com.notecraft.ui.theme.AppSpacing
import com.notecraft.util.Strings
import com.notecraft.util.TimeFormat

@Composable
fun MobileNoteApp(
    noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    fileDialogService: FileDialogService? = null,
    isWideScreen: Boolean = false,
    backSignal: Int = 0,
    onExitApp: () -> Unit = {}
) {
    val listViewModel: NoteListViewModel = viewModel { NoteListViewModel(noteRepository) }
    val editorViewModel: NoteEditorViewModel = viewModel { NoteEditorViewModel(noteRepository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(settingsRepository) }

    val listState by listViewModel.state.collectAsStateWithLifecycle()
    val editorState by editorViewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    // React to platform back button signal
    LaunchedEffect(backSignal) {
        if (backSignal == 0) return@LaunchedEffect
        if (showEditor) {
            editorViewModel.saveAndContinue {
                showEditor = false
                listViewModel.selectNote("")
            }
        } else {
            onExitApp()
        }
    }

    LaunchedEffect(listState.selectedNoteId) {
        if (listState.selectedNoteId != null && !isWideScreen) {
            showEditor = true
        }
        listState.selectedNoteId?.let { id ->
            editorViewModel.loadNote(id)
        } ?: editorViewModel.clearEditor()
    }

    showDeleteConfirm?.let { noteId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(Strings.deleteTitle) },
            text = { Text(Strings.deleteMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = null
                    if (noteId == listState.selectedNoteId) {
                        editorViewModel.clearEditor()
                        showEditor = false
                    }
                    listViewModel.deleteNote(noteId)
                }) { Text(Strings.deleteConfirm, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text(Strings.cancel) } }
        )
    }

    val isDarkTheme = when (settingsState.config.theme) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val currentFontSize = settingsState.config.fontSize

    NotecraftTheme(darkTheme = isDarkTheme, fontSize = currentFontSize) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    MobileNoteListPanel(
                        state = listState,
                        onCreateNote = {
                            editorViewModel.saveAndContinue {
                                listViewModel.createNote()
                                showEditor = true
                            }
                        },
                        onSelectNote = { id ->
                            editorViewModel.saveAndContinue {
                                listViewModel.selectNote(id)
                            }
                        },
                        onDeleteNote = { showDeleteConfirm = it },
                        onSearchQueryChange = { listViewModel.setSearchQuery(it) },
                        onSortModeChange = { listViewModel.setSortMode(it) },
                        modifier = Modifier.weight(0.35f).fillMaxHeight()
                    )
                    VerticalDivider()
                    if (editorState.noteId != null) {
                        MobileEditorPanel(
                            state = editorState,
                            onTitleChange = { editorViewModel.updateTitle(it) },
                            onContentChange = { editorViewModel.updateContent(it) },
                            onSave = { editorViewModel.save() },
                            onViewModeChange = { editorViewModel.setViewMode(it) },
                            onBack = { },
                            modifier = Modifier.weight(0.65f).fillMaxHeight()
                        )
                    } else {
                        Box(modifier = Modifier.weight(0.65f).fillMaxHeight(),
                            contentAlignment = Alignment.Center) {
                            Text(Strings.selectNoteHint,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = showEditor,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "phone_nav"
                ) { editing ->
                    if (editing) {
                        MobileEditorPanel(
                            state = editorState,
                            onTitleChange = { editorViewModel.updateTitle(it) },
                            onContentChange = { editorViewModel.updateContent(it) },
                            onSave = { editorViewModel.save() },
                            onViewModeChange = { editorViewModel.setViewMode(it) },
                            onBack = {
                                if (showEditor) {
                                    editorViewModel.saveAndContinue {
                                        showEditor = false
                                        listViewModel.selectNote("")
                                    }
                                }
                            }
                        )
                    } else {
                        MobileNoteListPanel(
                            state = listState,
                            onCreateNote = {
                                editorViewModel.saveAndContinue {
                                    listViewModel.createNote()
                                    showEditor = true
                                }
                            },
                            onSelectNote = { id ->
                                editorViewModel.saveAndContinue {
                                    listViewModel.selectNote(id)
                                }
                            },
                            onDeleteNote = { showDeleteConfirm = it },
                            onSearchQueryChange = { listViewModel.setSearchQuery(it) },
                            onSortModeChange = { listViewModel.setSortMode(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileNoteListPanel(
    state: NoteListState,
    onCreateNote: () -> Unit,
    onSelectNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(Strings.noteListTitle(state.filteredNotes.size)) },
            scrollBehavior = null,
            actions = {
                TextButton(onClick = onCreateNote) {
                    Text(Strings.newNote)
                }
            }
        )

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(Strings.searchNotes) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .height(AppSpacing.searchFieldHeight),
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(Modifier.height(AppSpacing.sm))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md)) {
            TextButton(onClick = { onSortModeChange(SortMode.RECENTLY_UPDATED) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(Strings.recent, fontSize = 11.sp,
                    fontWeight = if (state.sortMode == SortMode.RECENTLY_UPDATED)
                        FontWeight.Bold else FontWeight.Normal)
            }
            TextButton(onClick = { onSortModeChange(SortMode.TITLE) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(Strings.sortByTitle, fontSize = 11.sp,
                    fontWeight = if (state.sortMode == SortMode.TITLE)
                        FontWeight.Bold else FontWeight.Normal)
            }
        }

        state.error?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = AppSpacing.md))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (state.filteredNotes.isEmpty()) {
                item {
                    val msg = if (state.searchQuery.isNotBlank())
                        Strings.noResultsFor(state.searchQuery) else Strings.noNotesYet
                    Text(msg, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(AppSpacing.editorPadding))
                }
            }
            for (group in state.filteredGroups) {
                val label = group.category.ifEmpty { Strings.uncategorized }
                item(key = "cat_" + group.category) {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                items(group.notes, key = { it.id }) { note ->
                    MobileNoteItem(note = note, onClick = { onSelectNote(note.id) })
                }
            }
        }
    }
}

@Composable
private fun MobileNoteItem(
    note: NoteMetadata,
    onClick: () -> Unit
) {
    val displayTitle = if (note.title.isNotBlank()) note.title else Strings.untitled
    val previewText = if (note.title.isNotBlank()) note.preview else ""

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = displayTitle, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
                Spacer(Modifier.width(4.dp))
                Text(text = TimeFormat.relativeTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (previewText.isNotBlank()) {
                Text(text = previewText, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileEditorPanel(
    state: NoteEditorState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = state.title.ifBlank { Strings.untitled }, maxLines = 1)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<")
                }
            },
            actions = {
                val saveText = when (state.saveState) {
                    is SaveState.Idle -> ""
                    is SaveState.Dirty -> Strings.saveUnsaved
                    is SaveState.Saving -> Strings.saveSaving
                    is SaveState.Saved -> Strings.saveSaved
                    is SaveState.Error -> Strings.saveError(state.saveState.message)
                }
                if (saveText.isNotBlank()) {
                    Text(saveText, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = AppSpacing.sm))
                }
                Button(onClick = onSave,
                    enabled = state.saveState is SaveState.Dirty,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(Strings.save, style = MaterialTheme.typography.labelMedium)
                }
            },
            scrollBehavior = null
        )

        if (state.noteId == null) {
            Text(Strings.selectNoteHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(AppSpacing.editorPadding))
            return
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md),
            horizontalArrangement = Arrangement.Center) {
            SingleChoiceSegmentedButtonRow {
                ViewMode.entries.forEachIndexed { idx, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = ViewMode.entries.size),
                        onClick = { onViewModeChange(mode) },
                        selected = state.viewMode == mode
                    ) {
                        Text(when (mode) {
                            ViewMode.EDIT -> Strings.editMode
                            ViewMode.SPLIT -> Strings.splitMode
                            ViewMode.PREVIEW -> Strings.previewMode
                        }, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(AppSpacing.sm))

        when (state.viewMode) {
            ViewMode.EDIT -> {
                OutlinedTextField(
                    value = state.title, onValueChange = onTitleChange,
                    label = { Text(Strings.editorTitle) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(AppSpacing.sm))
                OutlinedTextField(
                    value = state.content, onValueChange = onContentChange,
                    label = { Text(Strings.editorContent) },
                    modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.md),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
            ViewMode.PREVIEW -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.md)) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp))
                    MarkdownContent(content = state.content, fontSize = 14,
                        modifier = Modifier.fillMaxSize())
                }
            }
            ViewMode.SPLIT -> {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.sm)) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        OutlinedTextField(value = state.title, onValueChange = onTitleChange,
                            label = { Text(Strings.editorTitle) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                        Spacer(Modifier.height(AppSpacing.sm))
                        OutlinedTextField(value = state.content, onValueChange = onContentChange,
                            label = { Text(Strings.editorContent) },
                            modifier = Modifier.fillMaxSize(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                    }
                    Spacer(Modifier.width(AppSpacing.sm))
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        MarkdownContent(content = state.content, fontSize = 14,
                            modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

package com.notecraft.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notecraft.data.repository.InMemoryNoteRepository
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.data.storage.InMemorySettingsStorage
import com.notecraft.data.importexport.ImportExportUseCase
import com.notecraft.data.importexport.FileDialogService
import com.notecraft.domain.model.ViewMode
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.domain.repository.SettingsRepository
import com.notecraft.presentation.note.*
import com.notecraft.presentation.settings.SettingsViewModel
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.ui.theme.NotecraftTheme
import com.notecraft.util.Strings
import com.notecraft.util.NoteUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteApp(
    noteRepository: NoteRepository = remember { InMemoryNoteRepository() },
    settingsRepository: SettingsRepository = remember { SettingsRepositoryImpl(InMemorySettingsStorage()) },
    fileDialogService: FileDialogService? = null,
    onToggleTile: ((String) -> Unit)? = null
) {
    val listViewModel: NoteListViewModel = viewModel { NoteListViewModel(noteRepository) }
    val editorViewModel: NoteEditorViewModel = viewModel { NoteEditorViewModel(noteRepository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(settingsRepository) }
    val scope = rememberCoroutineScope()
    val importExport = remember(fileDialogService) { fileDialogService?.let { ImportExportUseCase(noteRepository, it) } }

    val listState by listViewModel.state.collectAsStateWithLifecycle()
    val editorState by editorViewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listState.selectedNoteId) {
        listState.selectedNoteId?.let { id ->
            if (editorState.noteId == id && editorState.title.isEmpty())
                titleFocusRequester.requestFocus()
        }
    }
    LaunchedEffect(listState.selectedNoteId) {
        listState.selectedNoteId?.let { id -> editorViewModel.loadNote(id) }
            ?: editorViewModel.clearEditor()
    }

    showDeleteConfirm?.let { noteId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(Strings.deleteTitle) },
            text = { Text(Strings.deleteMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = null
                    if (noteId == listState.selectedNoteId) editorViewModel.clearEditor()
                    listViewModel.deleteNote(noteId)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text(Strings.cancel) } }
        )
    }

    val isDarkTheme = when (settingsState.config.theme) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    NotecraftTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp && event.isCtrlPressed && event.key == Key.S) {
                        editorViewModel.save(); true
                    } else false
                }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NoteListPanel(
                        state = listState,
                        onCreateNote = { editorViewModel.saveAndContinue { listViewModel.createNote() } },
                        onSelectNote = { id -> editorViewModel.saveAndContinue { listViewModel.selectNote(id) } },
                        onDeleteNote = { showDeleteConfirm = it },
                        onSortModeChange = { listViewModel.setSortMode(it) },
                        onSearchQueryChange = { listViewModel.setSearchQuery(it) },
                        onSettingsClick = { settingsViewModel.toggleOpen() },
                        onImport = { if (importExport != null) scope.launch { importExport.importMarkdownFile(); listViewModel.loadAll() } },
                        onExport = { val nid = listState.selectedNoteId; if (nid != null && importExport != null) scope.launch { importExport.exportMarkdownFile(nid) } },
                        modifier = Modifier.width(260.dp).fillMaxHeight()
                    )
                    VerticalDivider()
                    if (settingsState.isOpen) {
                        SettingsContent(
                            state = settingsState,
                            onThemeChange = { settingsViewModel.updateTheme(it) },
                            onFontSizeChange = { settingsViewModel.updateFontSize(it) },
                            onTabIndentChange = { settingsViewModel.updateTabIndentSize(it) },
                            onAutoSaveChange = { settingsViewModel.updateNoteAutoSave(it) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    } else {
                        EditorPanel(
                            state = editorState,
                            onTitleChange = { editorViewModel.updateTitle(it) },
                            onContentChange = { editorViewModel.updateContent(it) },
                            onSave = { editorViewModel.save() },
                            onViewModeChange = { editorViewModel.setViewMode(it) },
                            onToggleTile = onToggleTile,
                            titleFocusRequester = titleFocusRequester,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteListPanel(
    state: NoteListState,
    onCreateNote: () -> Unit,
    onSelectNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIdx by remember(state.selectedNoteId, state.filteredNotes) {
        mutableStateOf(state.filteredNotes.indexOfFirst { it.id == state.selectedNoteId }.coerceAtLeast(0))
    }

    Column(modifier = modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.noteListTitle(state.filteredNotes.size),
                style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(28.dp)) {
                Text("...", fontSize = 14.sp)
            }
        }
        OutlinedTextField(
            value = state.searchQuery, onValueChange = onSearchQueryChange,
            placeholder = { Text(Strings.searchNotes, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).height(46.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
        Button(onClick = onCreateNote, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(Strings.newNote)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onImport, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(Strings.importAction, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onExport, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(Strings.exportAction, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            TextButton(onClick = { onSortModeChange(SortMode.RECENTLY_UPDATED) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(Strings.recent, fontSize = 11.sp,
                    fontWeight = if (state.sortMode == SortMode.RECENTLY_UPDATED) FontWeight.Bold else FontWeight.Normal)
            }
            TextButton(onClick = { onSortModeChange(SortMode.TITLE) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(Strings.sortByTitle, fontSize = 11.sp,
                    fontWeight = if (state.sortMode == SortMode.TITLE) FontWeight.Bold else FontWeight.Normal)
            }
        }
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        state.error?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
                        val next = (selectedIdx + 1).coerceAtMost(state.filteredNotes.size - 1)
                        if (next >= 0 && next < state.filteredNotes.size) { selectedIdx = next; onSelectNote(state.filteredNotes[next].id) }; true
                    }
                    event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
                        val prev = (selectedIdx - 1).coerceAtLeast(0)
                        if (prev < state.filteredNotes.size) { selectedIdx = prev; onSelectNote(state.filteredNotes[prev].id) }; true
                    }
                    event.type == KeyEventType.KeyUp && event.key == Key.Enter -> {
                        if (selectedIdx in state.filteredNotes.indices) { onSelectNote(state.filteredNotes[selectedIdx].id) }; true
                    }
                    else -> false
                }
            }
        ) {
            if (state.filteredNotes.isEmpty()) {
                item {
                    val emptyMsg = if (state.searchQuery.isNotBlank()) "No results for: " + state.searchQuery else "No notes yet. Create one with +."
                    Text(emptyMsg, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            }
            for (group in state.filteredGroups) {
                val label = group.category.ifEmpty { Strings.uncategorized }
                item(key = "cat_" + group.category) {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                items(group.notes, key = { it.id }) { note ->
                    val title = if (note.title.isNotBlank()) note.title else (note.preview.take(20).ifEmpty { "untitled" })
                    val isSelected = note.id == state.selectedNoteId
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp)
                            .clickable { selectedIdx = state.filteredNotes.indexOf(note); onSelectNote(note.id) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onDeleteNote(note.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.size(28.dp)) {
                                Text("x", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorPanel(
    state: NoteEditorState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onToggleTile: ((String) -> Unit)? = null,
    titleFocusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(16.dp)) {
        if (state.noteId == null) {
            Text(Strings.selectNoteHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            val saveText = when (state.saveState) {
                is SaveState.Idle -> ""
                is SaveState.Dirty -> Strings.saveUnsaved
                is SaveState.Saving -> Strings.saveSaving
                is SaveState.Saved -> Strings.saveSaved
                is SaveState.Error -> Strings.saveError(state.saveState.message)
            }
            Text(saveText, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.weight(1f))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.width(180.dp)) {
                ViewMode.entries.forEachIndexed { idx, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = ViewMode.entries.size),
                        onClick = { onViewModeChange(mode) },
                        selected = state.viewMode == mode
                    ) {
                        Text(when (mode) {
                            ViewMode.EDIT -> Strings.editMode; ViewMode.SPLIT -> Strings.splitMode; ViewMode.PREVIEW -> Strings.previewMode
                        }, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            onToggleTile?.let { toggle ->
                IconButton(onClick = { toggle(state.noteId!!) }, modifier = Modifier.size(32.dp)) {
                    Text(Strings.pin, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(Strings.words(state.wordCount), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSave, enabled = state.saveState is SaveState.Dirty,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text(Strings.save, style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        when (state.viewMode) {
            ViewMode.EDIT -> {
                EditorFields(state, onTitleChange, onContentChange, titleFocusRequester, focusManager)
            }
            ViewMode.PREVIEW -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 12.dp))
                    MarkdownContent(content = state.content, fontSize = 14, modifier = Modifier.fillMaxSize())
                }
            }
            ViewMode.SPLIT -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        OutlinedTextField(value = state.title, onValueChange = onTitleChange,
                            label = { Text(Strings.editorTitle) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = state.content, onValueChange = onContentChange,
                            label = { Text("Markdown") },
                            modifier = Modifier.fillMaxSize(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp)) {
                        MarkdownContent(content = state.content, fontSize = 14, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColumnScope.EditorFields(
    state: NoteEditorState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    titleFocusRequester: FocusRequester,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(value = state.title, onValueChange = onTitleChange,
        label = { Text(Strings.editorTitle) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester).onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp && event.key == Key.Tab) { focusManager.clearFocus(); true } else false
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = state.content, onValueChange = onContentChange,
        label = { Text(Strings.editorContent) },
        modifier = Modifier.fillMaxWidth().weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
}

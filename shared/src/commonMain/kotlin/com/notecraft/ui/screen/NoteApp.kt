package com.notecraft.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notecraft.data.importexport.ImportExportUseCase
import com.notecraft.data.importexport.FileDialogService
import com.notecraft.domain.model.ViewMode
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.domain.repository.SettingsRepository
import com.notecraft.presentation.note.*
import com.notecraft.presentation.settings.SettingsViewModel
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.ui.theme.NotecraftTheme
import com.notecraft.ui.theme.AppComponentDefaults
import com.notecraft.ui.theme.AppShapes
import com.notecraft.ui.theme.AppSpacing
import com.notecraft.util.Strings
import com.notecraft.util.TimeFormat
import com.notecraft.util.NoteUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteApp(
    noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    fileDialogService: FileDialogService? = null,
    onToggleTile: ((String) -> Unit)? = null,
    onCurrentNoteTitleChange: (String?) -> Unit = {},
    desktopTitleBar: (@Composable () -> Unit)? = null
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
    var isSidebarVisible by remember { mutableStateOf(true) }

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
    LaunchedEffect(editorState.noteId, editorState.title) {
        onCurrentNoteTitleChange(
            editorState.noteId?.let { editorState.title.ifBlank { Strings.currentNotePlaceholder } }
        )
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
            Column(modifier = Modifier.fillMaxSize()) {
                desktopTitleBar?.invoke()
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp && event.isCtrlPressed && event.key == Key.S) {
                            editorViewModel.save(); true
                        } else false
                    }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                    if (isSidebarVisible) {
                        NoteListPanel(
                            state = listState,
                            onCreateNote = { editorViewModel.saveAndContinue { listViewModel.createNote() } },
                            onSelectNote = { id -> editorViewModel.saveAndContinue { listViewModel.selectNote(id) } },
                            onDeleteNote = { showDeleteConfirm = it },
                            onSortModeChange = { listViewModel.setSortMode(it) },
                            onSearchQueryChange = { listViewModel.setSearchQuery(it) },
                            onSettingsClick = { settingsViewModel.toggleOpen() },
                            onImport = {
                                if (importExport != null) {
                                    scope.launch {
                                        importExport.importMarkdownFile()
                                        listViewModel.loadAll()
                                    }
                                }
                            },
                            onExport = {
                                val nid = listState.selectedNoteId
                                if (nid != null && importExport != null) {
                                    scope.launch { importExport.exportMarkdownFile(nid) }
                                }
                            },
                            modifier = Modifier.width(AppSpacing.sidebarWidth).fillMaxHeight()
                        )
                        VerticalDivider()
                    }
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
                            onDelete = { editorState.noteId?.let { showDeleteConfirm = it } },
                            onUndo = { editorViewModel.undo() },
                            onRedo = { editorViewModel.redo() },
                            onToggleSidebar = { isSidebarVisible = !isSidebarVisible },
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

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.appBrandName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.notesCount(state.notes.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(AppSpacing.iconButtonSmall)
                ) {
                    Text(
                        text = "...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(Strings.searchNotes, style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                shape = AppShapes.control,
                modifier = Modifier.fillMaxWidth().height(AppSpacing.searchFieldHeight),
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = if (state.searchQuery.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(AppSpacing.iconButtonSmall)
                        ) {
                            Text("x", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    null
                }
            )

            Spacer(Modifier.height(AppSpacing.sm))
            SidebarAction(
                icon = "+",
                text = Strings.newNote,
                onClick = onCreateNote,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SidebarAction(
                    icon = "v",
                    text = Strings.importAction,
                    onClick = onImport,
                    modifier = Modifier.weight(1f)
                )
                SidebarAction(
                    icon = "^",
                    text = Strings.exportAction,
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    enabled = state.selectedNoteId != null
                )
            }

            Spacer(Modifier.height(AppSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.notesCount(state.filteredNotes.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { onSortModeChange(SortMode.RECENTLY_UPDATED) },
                    contentPadding = AppComponentDefaults.toolbarPadding
                ) {
                    Text(
                        text = Strings.recent,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (state.sortMode == SortMode.RECENTLY_UPDATED) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
                TextButton(
                    onClick = { onSortModeChange(SortMode.TITLE) },
                    contentPadding = AppComponentDefaults.toolbarPadding
                ) {
                    Text(
                        text = Strings.sortByTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (state.sortMode == SortMode.TITLE) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
            }
            state.error?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = AppSpacing.sm)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onKeyEvent { event ->
                        when {
                            event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
                                val next = (selectedIdx + 1).coerceAtMost(state.filteredNotes.size - 1)
                                if (next >= 0 && next < state.filteredNotes.size) {
                                    selectedIdx = next
                                    onSelectNote(state.filteredNotes[next].id)
                                }
                                true
                            }
                            event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
                                val prev = (selectedIdx - 1).coerceAtLeast(0)
                                if (prev < state.filteredNotes.size) {
                                    selectedIdx = prev
                                    onSelectNote(state.filteredNotes[prev].id)
                                }
                                true
                            }
                            event.type == KeyEventType.KeyUp && event.key == Key.Enter -> {
                                if (selectedIdx in state.filteredNotes.indices) {
                                    onSelectNote(state.filteredNotes[selectedIdx].id)
                                }
                                true
                            }
                            else -> false
                        }
                    },
                contentPadding = PaddingValues(vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                if (state.filteredNotes.isEmpty()) {
                    item {
                        val emptyMsg = if (state.searchQuery.isNotBlank()) {
                            Strings.noResultsFor(state.searchQuery)
                        } else {
                            Strings.noNotesYet
                        }
                        Text(
                            text = emptyMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppSpacing.lg)
                        )
                    }
                }
                for (group in state.filteredGroups) {
                    val label = group.category.ifEmpty { Strings.uncategorized }
                    item(key = "cat_" + group.category) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = AppSpacing.sm, bottom = AppSpacing.xs)
                        )
                    }
                    items(group.notes, key = { it.id }) { note ->
                        NoteListItem(
                            note = note,
                            isSelected = note.id == state.selectedNoteId,
                            onClick = {
                                selectedIdx = state.filteredNotes.indexOfFirst { it.id == note.id }
                                onSelectNote(note.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarAction(
    icon: String,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = AppShapes.control,
        contentPadding = AppComponentDefaults.compactPadding
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun NoteListItem(
    note: com.notecraft.domain.model.NoteMetadata,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember(note.id) { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember(note.id) { mutableStateOf(false) }
    val displayTitle = note.title.ifBlank { Strings.untitled }
    val itemColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isHovered -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .background(itemColor, AppShapes.control)
            .hoverable(interactionSource)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else itemColor,
                shape = AppShapes.control
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(end = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(54.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else itemColor,
                    AppShapes.compact
                )
        )
        Column(
            modifier = Modifier.padding(start = AppSpacing.md, top = AppSpacing.sm, bottom = AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = TimeFormat.relativeTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (note.preview.isNotBlank()) {
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AppSpacing.xs)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeFormat.formatDateTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = Strings.words(note.wordCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
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
    onDelete: () -> Unit = {},
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onToggleSidebar: () -> Unit = {},
    onViewModeChange: (ViewMode) -> Unit,
    onToggleTile: ((String) -> Unit)? = null,
    titleFocusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(AppSpacing.editorPadding)) {
        if (state.noteId == null) {
            Text(Strings.selectNoteHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }

        EditorActionBar(
            state = state,
            onToggleSidebar = onToggleSidebar,
            onToggleTile = onToggleTile,
            onUndo = onUndo,
            onRedo = onRedo,
            onSave = onSave,
            onDelete = onDelete,
            onViewModeChange = onViewModeChange
        )
        Spacer(Modifier.height(AppSpacing.sm))
        EditorNoteHeader(state = state)
        HorizontalDivider(modifier = Modifier.padding(top = AppSpacing.sm))
        Spacer(Modifier.height(AppSpacing.md))
        when (state.viewMode) {
            ViewMode.EDIT -> {
                EditorFields(state, onTitleChange, onContentChange, titleFocusRequester, focusManager)
            }
            ViewMode.PREVIEW -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    MarkdownContent(content = state.content, fontSize = 14, modifier = Modifier.fillMaxSize())
                }
            }
            ViewMode.SPLIT -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        MarkdownToolbar(
                            content = state.content,
                            onContentChange = onContentChange
                        )
                        OutlinedTextField(value = state.title, onValueChange = onTitleChange,
                            label = { Text(Strings.editorTitle) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                        Spacer(Modifier.height(AppSpacing.md))
                        OutlinedTextField(value = state.content, onValueChange = onContentChange,
                            label = { Text(Strings.editorContent) },
                            modifier = Modifier.fillMaxSize(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = AppSpacing.md)) {
                        MarkdownContent(content = state.content, fontSize = 14, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
    }
}

@Composable
private fun EditorActionBar(
    state: NoteEditorState,
    onToggleSidebar: () -> Unit,
    onToggleTile: ((String) -> Unit)?,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onViewModeChange: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(AppSpacing.editorHeaderHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditorActionButton(
            glyph = "[]",
            label = "Toggle sidebar",
            onClick = onToggleSidebar
        )
        onToggleTile?.let { toggle ->
            EditorActionButton(
                glyph = "P",
                label = Strings.pin,
                onClick = { state.noteId?.let(toggle) }
            )
        }
        EditorActionButton(
            glyph = "<-",
            label = "Undo",
            enabled = state.canUndo,
            onClick = onUndo
        )
        EditorActionButton(
            glyph = "->",
            label = "Redo",
            enabled = state.canRedo,
            onClick = onRedo
        )
        Spacer(Modifier.weight(1f))
        EditorTextAction(
            glyph = "S",
            label = Strings.save,
            enabled = state.saveState is SaveState.Dirty,
            onClick = onSave
        )
        EditorTextAction(
            glyph = "X",
            label = Strings.deleteConfirm,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDelete
        )
        Spacer(Modifier.width(AppSpacing.sm))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.width(AppSpacing.segmentedButtonWidth)) {
            ViewMode.entries.forEachIndexed { idx, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = ViewMode.entries.size),
                    onClick = { onViewModeChange(mode) },
                    selected = state.viewMode == mode
                ) {
                    Text(
                        text = when (mode) {
                            ViewMode.EDIT -> Strings.editMode
                            ViewMode.SPLIT -> Strings.splitMode
                            ViewMode.PREVIEW -> Strings.previewMode
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorActionButton(
    glyph: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    EditorTooltip(label = label) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(AppSpacing.iconButtonMedium)
                .semantics { contentDescription = label }
        ) {
            Text(
                text = glyph,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
private fun EditorTextAction(
    glyph: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    EditorTooltip(label = label) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.semantics { contentDescription = label },
            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
        ) {
            Text(text = glyph, style = MaterialTheme.typography.labelLarge, color = tint)
            Spacer(Modifier.width(AppSpacing.sm))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = tint)
        }
    }
}

@Composable
private fun EditorTooltip(
    label: String,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val tooltipOffset = with(LocalDensity.current) {
        -AppSpacing.editorHeaderHeight.roundToPx()
    }
    Box(modifier = Modifier.hoverable(interactionSource)) {
        content()
        if (isHovered) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, tooltipOffset),
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = AppShapes.compact
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorNoteHeader(state: NoteEditorState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = state.title.ifBlank { Strings.currentNotePlaceholder },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.updatedAt?.let {
                Text(
                    text = TimeFormat.formatDateTime(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(AppSpacing.lg))
            }
            Text(
                text = Strings.words(state.wordCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(AppSpacing.lg))
            SaveStateLabel(state.saveState)
        }
    }
}

@Composable
private fun SaveStateLabel(saveState: SaveState) {
    val text = when (saveState) {
        SaveState.Idle -> Strings.saveSaved
        SaveState.Dirty -> Strings.saveUnsaved
        SaveState.Saving -> Strings.saveSaving
        SaveState.Saved -> Strings.saveSaved
        is SaveState.Error -> Strings.saveError(saveState.message)
    }
    val color = when (saveState) {
        is SaveState.Error -> MaterialTheme.colorScheme.error
        SaveState.Dirty -> MaterialTheme.colorScheme.primary
        SaveState.Saving -> MaterialTheme.colorScheme.onSurfaceVariant
        SaveState.Idle, SaveState.Saved -> MaterialTheme.colorScheme.tertiary
    }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
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
    MarkdownToolbar(
        content = state.content,
        onContentChange = onContentChange
    )
    OutlinedTextField(value = state.title, onValueChange = onTitleChange,
        label = { Text(Strings.editorTitle) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester).onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp && event.key == Key.Tab) { focusManager.clearFocus(); true } else false
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    Spacer(Modifier.height(AppSpacing.lg))
    MarkdownToolbar(
        content = state.content,
        onContentChange = onContentChange
    )
    OutlinedTextField(value = state.content, onValueChange = onContentChange,
        label = { Text(Strings.editorContent) },
        modifier = Modifier.fillMaxWidth().weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
}

@Composable
private fun MarkdownToolbar(
    content: String,
    onContentChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val buttons = listOf(
            "B" to { "**" + content + "**" },
            "I" to { "*" + content + "*" },
            "H" to { "# " + content },
            "—" to { content + "\n---\n" },
            "•" to { content + "\n- " },
            "1." to { content + "\n1. " },
            "<>" to { "```\n" + content + "\n```" },
            "❝" to { content + "\n> " }
        )
        buttons.forEach { (label, transform) ->
            TextButton(
                onClick = { onContentChange(transform()) },
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
import com.notecraft.ui.editor.EditorStatusInfo
import com.notecraft.ui.editor.MarkdownFormat
import com.notecraft.ui.editor.MarkdownFormatting
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.ui.theme.NotecraftTheme
import com.notecraft.ui.theme.AppShapes
import com.notecraft.ui.theme.AppSpacing
import com.notecraft.util.Strings
import com.notecraft.util.TimeFormat
import com.notecraft.util.NoteUtils
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteApp(
    noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    fileDialogService: FileDialogService? = null,

    settingsToggleSignal: androidx.compose.runtime.MutableState<Int>? = null,
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

    LaunchedEffect(settingsToggleSignal?.value) {
        val count = settingsToggleSignal?.value ?: 0
        if (count > 0) {
            settingsViewModel.toggleOpen()
        }
    }
    LaunchedEffect(editorState.noteId, editorState.title) {
        onCurrentNoteTitleChange(
            editorState.noteId?.let { editorState.title.ifBlank { Strings.currentNotePlaceholder } }
        )
    }

    showDeleteConfirm?.let { noteId ->
        val deleteFocusRequester = remember(noteId) { FocusRequester() }
        LaunchedEffect(noteId) {
            deleteFocusRequester.requestFocus()
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(Strings.deleteConfirmTitle) },
            text = { Text(Strings.deleteMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = null
                        if (noteId == listState.selectedNoteId) editorViewModel.clearEditor()
                        listViewModel.deleteNote(noteId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.focusRequester(deleteFocusRequester)
                ) {
                    Text(Strings.deleteConfirm)
                }
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
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val showSidebar = isSidebarVisible && maxWidth >= AppSpacing.sidebarAutoCollapseWidth
                        val sidebarWidth = if (maxWidth < AppSpacing.sidebarNarrowWindowWidth) {
                            AppSpacing.sidebarMinWidth
                        } else {
                            AppSpacing.sidebarWidth
                        }
                        val editorPadding = if (maxWidth < AppSpacing.sidebarAutoCollapseWidth) {
                            AppSpacing.editorCompactPadding
                        } else {
                            AppSpacing.editorPadding
                        }
                        val topSectionHeight = (maxHeight * 0.25f).coerceIn(176.dp, 204.dp)

                        Row(modifier = Modifier.fillMaxSize()) {
                            if (showSidebar) {
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
                                    modifier = Modifier.width(sidebarWidth).fillMaxHeight()
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
                                    onCloseToTrayChange = { settingsViewModel.updateCloseToTray(it) },
                                    onClose = { settingsViewModel.close() },
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
                                    contentPadding = editorPadding,
                                    topSectionHeight = topSectionHeight,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = AppSpacing.lg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.searchFieldHeight)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f), AppShapes.control)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.78f),
                            shape = AppShapes.control
                        )
                        .padding(horizontal = AppSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                    )
                    Spacer(Modifier.width(AppSpacing.md))
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.searchQuery.isBlank()) {
                                    Text(
                                        text = Strings.searchNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (state.searchQuery.isNotBlank()) {
                        Text(
                            text = "x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = AppSpacing.md)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacing.md))

                SidebarAction(
                    icon = "+",
                    text = Strings.newNote.removePrefix("+ "),
                    onClick = onCreateNote,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(AppSpacing.xs))

                SidebarAction(
                    icon = "\u2193",
                    text = Strings.importAction,
                    onClick = onImport,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(AppSpacing.lg))

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
                        onClick = onCreateNote,
                        contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 0.dp)
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
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
                contentPadding = PaddingValues(top = AppSpacing.md, bottom = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
                            modifier = Modifier.padding(vertical = AppSpacing.lg)
                        )
                    }
                }
                items(state.filteredNotes, key = { it.id }) { note ->
                    NoteListItem(
                        note = note,
                        isSelected = note.id == state.selectedNoteId,
                        onClick = { onSelectNote(note.id) },
                        onDelete = { onDeleteNote(note.id) }
                    )
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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }
    val actionColor =
        if (enabled && (isHovered || isFocused)) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent

    Row(
        modifier = modifier
            .height(32.dp)
            .background(actionColor, AppShapes.control)
            .hoverable(interactionSource, enabled)
            .focusable(enabled)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(AppSpacing.md))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NoteListItem(
    note: com.notecraft.domain.model.NoteMetadata,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember(note.id) { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember(note.id) { mutableStateOf(false) }
    var menuExpanded by remember(note.id) { mutableStateOf(false) }
    val displayTitle = note.title.ifBlank { Strings.untitled }
    val itemColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
        isHovered -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp)
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
                .height(22.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    AppShapes.compact
                )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppSpacing.lg, top = AppSpacing.sm, bottom = AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = TimeFormat.formatMonthDay(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
                if (isHovered) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(AppSpacing.iconButtonSmall)
                                .semantics { contentDescription = Strings.moreActions }
                        ) {
                            Text(
                                text = "...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = Strings.deleteConfirm,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
            Text(
                text = note.preview.ifBlank { Strings.uncategorized },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.84f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = AppSpacing.xs)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeFormat.formatDateTime(note.updatedAt).substringAfter(' '),
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
    contentPadding: androidx.compose.ui.unit.Dp = AppSpacing.editorPadding,
    topSectionHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val previewScrollState = remember(state.noteId) { ScrollState(0) }
    var contentValue by remember(state.noteId) {
        mutableStateOf(TextFieldValue(state.content))
    }

    LaunchedEffect(state.noteId, state.content) {
        if (contentValue.text != state.content) {
            val selection = TextRange(
                contentValue.selection.start.coerceIn(0, state.content.length),
                contentValue.selection.end.coerceIn(0, state.content.length)
            )
            contentValue = TextFieldValue(state.content, selection = selection)
        }
    }

    val onContentValueChange: (TextFieldValue) -> Unit = { value ->
        contentValue = value
        onContentChange(value.text)
    }

    Column(
        modifier = modifier.padding(
            start = contentPadding,
            top = 0.dp,
            end = AppSpacing.xxl,
            bottom = 0.dp
        )
    ) {
        if (state.noteId == null) {
            Text(Strings.selectNoteHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }

        Column(modifier = Modifier.fillMaxWidth().height(topSectionHeight)) {
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
            Spacer(Modifier.height(AppSpacing.xxl))
            EditorNoteHeader(
                state = state,
                onTitleChange = onTitleChange,
                titleFocusRequester = titleFocusRequester
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = AppSpacing.md),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            )
            Spacer(Modifier.weight(1f))
            if (state.viewMode != ViewMode.PREVIEW) {
                MarkdownToolbar(
                    value = contentValue,
                    onValueChange = onContentValueChange
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (state.viewMode) {
                ViewMode.EDIT -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        EditorFields(
                            contentValue = contentValue,
                            onContentValueChange = onContentValueChange,
                            showMarkdownToolbar = false
                        )
                    }
                }
                ViewMode.PREVIEW -> {
                    PreviewPane(
                        content = state.content,
                        scrollState = previewScrollState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ViewMode.SPLIT -> {
                    if (maxWidth < AppSpacing.splitCollapseWidth) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            EditorMarkdownBody(
                                contentValue = contentValue,
                                onContentValueChange = onContentValueChange,
                                showToolbar = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            EditorMarkdownBody(
                                contentValue = contentValue,
                                onContentValueChange = onContentValueChange,
                                showToolbar = false,
                                modifier = Modifier.weight(1.08f).fillMaxHeight()
                            )
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = AppSpacing.xl),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                            )
                            PreviewPane(
                                content = contentValue.text,
                                scrollState = previewScrollState,
                                showLabel = true,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        EditorStatusBar(value = contentValue)
    }
}

@Composable
private fun EditorStatusBar(value: TextFieldValue) {
    val cursor = EditorStatusInfo.cursorPosition(value.text, value.selection.end)
    val sizeLabel = EditorStatusInfo.utf8SizeLabel(value.text)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < AppSpacing.statusCompactWidth
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppSpacing.statusBarHeight)
                .padding(horizontal = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusText(text = "Ln ${cursor.line}", color = labelColor)
            StatusSeparator(color = labelColor, compact = compact)
            StatusText(text = "Col ${cursor.column}", color = labelColor)
            if (!compact) {
                StatusSeparator(color = labelColor)
                StatusText(text = "Markdown", color = labelColor)
            }
            Spacer(Modifier.weight(1f))
            if (!compact) {
                StatusText(text = "UTF-8", color = labelColor)
                StatusSeparator(color = labelColor)
            }
            StatusText(text = sizeLabel, color = labelColor)
        }
    }
}

@Composable
private fun StatusText(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun StatusSeparator(color: androidx.compose.ui.graphics.Color, compact: Boolean = false) {
    Text(
        text = "|",
        style = MaterialTheme.typography.labelSmall,
        color = color.copy(alpha = 0.42f),
        modifier = Modifier.padding(horizontal = if (compact) AppSpacing.sm else AppSpacing.md)
    )
}

@Composable
private fun PreviewPane(
    content: String,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showLabel) {
            Text(
                text = Strings.previewMode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                modifier = Modifier.padding(start = AppSpacing.md, bottom = AppSpacing.sm)
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            MarkdownContent(
                content = content,
                fontSize = 14,
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .widthIn(max = AppSpacing.previewMaxWidth)
                    .align(Alignment.TopStart)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < AppSpacing.editorActionCompactWidth
        val narrow = maxWidth < AppSpacing.editorActionNarrowWidth
        if (compact) {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = AppSpacing.toolbarHeight)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorActionIcon(
                        icon = SidebarToggleIcon,
                        label = "Toggle sidebar",
                        onClick = onToggleSidebar
                    )
                    onToggleTile?.let { toggle ->
                        EditorActionIcon(
                            icon = PinIcon,
                            label = Strings.pin,
                            onClick = { state.noteId?.let(toggle) }
                        )
                    }
                    EditorActionIcon(
                        icon = UndoIcon,
                        label = "Undo",
                        enabled = state.canUndo,
                        onClick = onUndo
                    )
                    EditorActionIcon(
                        icon = RedoIcon,
                        label = "Redo",
                        enabled = state.canRedo,
                        onClick = onRedo
                    )
                    Spacer(Modifier.weight(1f))
                    EditorActionIcon(
                        icon = SaveIcon,
                        label = Strings.save,
                        enabled = state.saveState is SaveState.Dirty,
                        onClick = onSave
                    )
                    EditorActionIcon(
                        icon = DeleteIcon,
                        label = Strings.deleteConfirm,
                        onClick = onDelete
                    )
                }
                Spacer(Modifier.height(AppSpacing.sm))
                EditorViewModeSelector(
                    state = state,
                    compactLabels = narrow,
                    onViewModeChange = onViewModeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorActionIcon(
                    icon = SidebarToggleIcon,
                    label = "Toggle sidebar",
                    onClick = onToggleSidebar
                )
                onToggleTile?.let { toggle ->
                    EditorActionIcon(
                        icon = PinIcon,
                        label = Strings.pin,
                        onClick = { state.noteId?.let(toggle) }
                    )
                }
                EditorActionIcon(
                    icon = UndoIcon,
                    label = "Undo",
                    enabled = state.canUndo,
                    onClick = onUndo
                )
                EditorActionIcon(
                    icon = RedoIcon,
                    label = "Redo",
                    enabled = state.canRedo,
                    onClick = onRedo
                )
                EditorActionIcon(
                    icon = SaveIcon,
                    label = Strings.save,
                    enabled = state.saveState is SaveState.Dirty,
                    onClick = onSave
                )
                EditorActionIcon(
                    icon = DeleteIcon,
                    label = Strings.deleteConfirm,
                    onClick = onDelete
                )
                Spacer(Modifier.weight(1f))
                EditorViewModeSelector(
                    state = state,
                    compactLabels = false,
                    onViewModeChange = onViewModeChange,
                    modifier = Modifier.width(AppSpacing.segmentedButtonWidth)
                )
            }
        }
    }
}

@Composable
private fun EditorViewModeSelector(
    state: NoteEditorState,
    compactLabels: Boolean,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        ViewMode.entries.forEachIndexed { idx, mode ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = idx, count = ViewMode.entries.size),
                onClick = { onViewModeChange(mode) },
                selected = state.viewMode == mode,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.surface,
                    activeContentColor = MaterialTheme.colorScheme.primary,
                    activeBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.36f),
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    inactiveBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = when (mode) {
                        ViewMode.EDIT -> if (compactLabels) Strings.editMode else Strings.editMode
                        ViewMode.SPLIT -> if (compactLabels) Strings.splitMode else Strings.splitMode
                        ViewMode.PREVIEW -> if (compactLabels) Strings.previewMode else Strings.previewMode
                    },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EditorActionIcon(
    icon: @Composable (Color) -> Unit,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    EditorTooltip(label = label) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val buttonColor = when {
            !enabled -> Color.Transparent
            isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
            else -> Color.Transparent
        }
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(AppSpacing.iconButtonMedium)
                .hoverable(interactionSource, enabled)
                .background(buttonColor, AppShapes.compact)
                .semantics { contentDescription = label }
        ) {
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                icon(
                    if (enabled) tint
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
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
        -AppSpacing.toolbarHeight.roundToPx()
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

// === Editor toolbar vector icons ===

private val SidebarToggleIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val inset = 2f
        val dividerX = size.width * 0.32f
        val w = size.width - inset * 2
        val h = size.height - inset * 2
        // Main right area
        drawRect(color = color, topLeft = Offset(dividerX, inset), size = androidx.compose.ui.geometry.Size(w - dividerX + inset, h),
            style = Stroke(width = 1.6f))
        // Left panel vertical line
        drawLine(color = color, start = Offset(dividerX, inset), end = Offset(dividerX, size.height - inset),
            strokeWidth = 1.6f)
    }
}

private val PinIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Pin head (circle at top)
        drawCircle(color = color, radius = 2.5f, center = Offset(cx, cy - 4f))
        // Pin shaft (line going down and slightly right)
        drawLine(color = color, start = Offset(cx, cy - 1.5f), end = Offset(cx + 3f, cy + 4f), strokeWidth = 1.6f)
        // Pin base (line going right)
        drawLine(color = color, start = Offset(cx, cy + 4f), end = Offset(cx + 5f, cy + 4f), strokeWidth = 1.6f)
    }
}

private val UndoIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = 5f
        // Left-curving arrow
        drawArc(color = color, startAngle = 90f, sweepAngle = -200f, useCenter = false,
            topLeft = Offset(cx - 2f, cy - r), size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
            style = Stroke(width = 1.6f))
        // Arrow head
        val tipX = cx - 2f - r * 0.5f
        val tipY = cy - r * 0.7f
        drawLine(color = color, start = Offset(tipX + 3f, tipY - 3f), end = Offset(tipX, tipY), strokeWidth = 1.6f)
        drawLine(color = color, start = Offset(tipX, tipY), end = Offset(tipX + 3f, tipY + 3f), strokeWidth = 1.6f)
    }
}

private val RedoIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = 5f
        drawArc(color = color, startAngle = -90f, sweepAngle = 200f, useCenter = false,
            topLeft = Offset(cx - 2f, cy - r), size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
            style = Stroke(width = 1.6f))
        val tipX = cx + 2f + r * 0.5f
        val tipY = cy - r * 0.7f
        drawLine(color = color, start = Offset(tipX - 3f, tipY - 3f), end = Offset(tipX, tipY), strokeWidth = 1.6f)
        drawLine(color = color, start = Offset(tipX, tipY), end = Offset(tipX - 3f, tipY + 3f), strokeWidth = 1.6f)
    }
}

private val SaveIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val inset = 2f
        val w = size.width - inset * 2
        val h = size.height - inset * 2
        // Floppy disk body
        drawRect(color = color, topLeft = Offset(inset, inset), size = androidx.compose.ui.geometry.Size(w, h),
            style = Stroke(width = 1.6f))
        // Top tab (label area)
        drawRect(color = color, topLeft = Offset(inset + 3f, inset), style = Stroke(width = 1.6f),
            size = androidx.compose.ui.geometry.Size(w - 6f, h * 0.45f))
        // Bottom slot (metal slider area)
        drawRect(color = color, topLeft = Offset(inset + w * 0.3f, inset + h * 0.5f), style = Stroke(width = 1.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.35f))
    }
}

private val DeleteIcon: @Composable (Color) -> Unit = { color ->
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val inset = 2f
        val w = size.width - inset * 2
        val h = size.height - inset * 2
        val bodyTop = inset + 3.5f
        // Body
        drawRect(color = color, topLeft = Offset(inset + 1.5f, bodyTop), style = Stroke(width = 1.6f),
            size = androidx.compose.ui.geometry.Size(w - 3f, h - 3.5f))
        // Lid
        drawLine(color = color, start = Offset(inset + 1f, bodyTop), end = Offset(size.width - inset - 1f, bodyTop),
            strokeWidth = 1.6f)
        // Lid handle
        drawLine(color = color, start = Offset(inset + 4f, bodyTop - 2.5f), end = Offset(inset + 4f + w * 0.35f, bodyTop - 2.5f),
            strokeWidth = 1.6f)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorNoteHeader(
    state: NoteEditorState,
    onTitleChange: (String) -> Unit,
    titleFocusRequester: FocusRequester
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val titleStyle = MaterialTheme.typography.headlineSmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        BasicTextField(
            value = state.title,
            onValueChange = onTitleChange,
            singleLine = true,
            textStyle = titleStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
            decorationBox = { innerTextField ->
                Box {
                    if (state.title.isBlank()) {
                        Text(
                            text = Strings.currentNotePlaceholder,
                            style = titleStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            state.updatedAt?.let {
                Text(
                    text = TimeFormat.formatDateTime(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = Strings.words(state.wordCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColumnScope.EditorFields(
    contentValue: TextFieldValue,
    onContentValueChange: (TextFieldValue) -> Unit,
    showMarkdownToolbar: Boolean = true
) {
    EditorMarkdownBody(
        contentValue = contentValue,
        onContentValueChange = onContentValueChange,
        showToolbar = showMarkdownToolbar,
        modifier = Modifier.fillMaxWidth().weight(1f)
    )
}

@Composable
private fun EditorMarkdownBody(
    contentValue: TextFieldValue,
    onContentValueChange: (TextFieldValue) -> Unit,
    showToolbar: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (showToolbar) {
            MarkdownToolbar(
                value = contentValue,
                onValueChange = onContentValueChange
            )
        }
        BasicTextField(
            value = contentValue,
            onValueChange = onContentValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = AppSpacing.xs)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkdownToolbar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 30.dp).padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        val buttons = listOf(
            "B" to MarkdownFormat.BOLD,
            "I" to MarkdownFormat.ITALIC,
            "H1" to MarkdownFormat.HEADING,
            "---" to MarkdownFormat.THEMATIC_BREAK,
            "- " to MarkdownFormat.BULLET_LIST,
            "1. " to MarkdownFormat.ORDERED_LIST,
            "```" to MarkdownFormat.CODE_BLOCK,
            "> " to MarkdownFormat.BLOCKQUOTE
        )
        buttons.forEach { (label, format) ->
            TextButton(
                onClick = {
                    val result = MarkdownFormatting.apply(
                        text = value.text,
                        selectionStart = value.selection.start,
                        selectionEnd = value.selection.end,
                        format = format
                    )
                    onValueChange(
                        TextFieldValue(
                            text = result.text,
                            selection = TextRange(result.selectionStart, result.selectionEnd)
                        )
                    )
                },
                modifier = Modifier
                    .widthIn(min = 30.dp)
                    .height(28.dp)
                    .focusProperties { canFocus = false },
                shape = AppShapes.compact,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                ),
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 0.dp)
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}




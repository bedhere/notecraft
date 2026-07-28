package com.notecraft.util

/**
 * Centralized user-facing strings used by the shared UI.
 */
object Strings {
    const val appBrandName = "笺造"
    const val appDisplayName = "笺造 · Notecraft"
    const val appName = appDisplayName

    fun notesCount(count: Int) = "$count 篇笔记"
    fun noteListTitle(count: Int) = notesCount(count)
    const val searchNotes = "搜索笔记..."
    const val newNote = "+ 新建笔记"
    const val importAction = "导入"
    const val exportAction = "导出"
    const val recent = "最近"
    const val sortByTitle = "按标题"
    fun noResultsFor(query: String) = "没有找到：$query"
    const val noNotesYet = "还没有笔记，点击 + 新建"
    const val uncategorized = "未分类"
    const val untitled = "无标题笔记"
    const val currentNotePlaceholder = "无标题笔记"
    const val selectNoteHint = "选择或新建一篇笔记"

    const val editorTitle = "标题"
    const val editorContent = "内容（Markdown）"
    const val editMode = "编辑"
    const val splitMode = "分栏"
    const val previewMode = "预览"
    const val save = "保存"
    const val pin = "钉屏"
    fun words(count: Int) = "字数：$count"

    const val saveUnsaved = "未保存"
    const val saveSaving = "保存中..."
    const val saveSaved = "已保存"
    fun saveError(msg: String) = "错误：$msg"

    const val deleteTitle = "删除笔记"
    const val deleteMessage = "确定要删除吗？此操作不可撤销。"
    const val deleteConfirm = "删除"
    const val cancel = "取消"

    const val settings = "设置"
    const val close = "关闭"
    const val theme = "主题"
    const val themeLight = "浅色"
    const val themeDark = "深色"
    const val themeSystem = "跟随系统"
    const val fontSize = "字体大小"
    const val autoSave = "自动保存"
    const val autoSaveEnabled = "已启用"
    const val autoSaveDisabled = "已禁用"
    const val tabIndent = "Tab 缩进大小"
    const val loading = "加载中..."
    const val appearance = "外观"
    const val editorSection = "编辑器"
    const val aboutSection = "关于"
    const val aboutDescription = "基于 Kotlin Multiplatform 和 Compose Multiplatform 构建的跨平台 Markdown 笔记应用。"
    const val version = "版本 1.0.0"
    const val desktopSection = "桌面"
    const val deleteConfirmTitle = "确认删除"
    const val saveFailed = "保存失败"
    const val importFailed = "导入失败"
    const val exportFailed = "导出失败"
    const val loadFailed = "加载失败"
    const val trayShow = "显示笺造 · Notecraft"
    const val trayQuickNote = "快速笔记"
    const val trayCloseToTray = "关闭到托盘"
    const val trayQuit = "退出"
    const val moreActions = "更多操作"
}

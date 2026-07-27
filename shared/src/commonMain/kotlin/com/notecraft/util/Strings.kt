 package com.notecraft.util
 
 /**
  * Centralized string constants for Notecraft UI.
  * English defaults. Chinese localized strings provided where applicable.
  * In future, these can be replaced with generated [Res.string] from compose resources.
  */
 object Strings {
     // App
     const val appName = "Notecraft"
 
     // Note List
     fun noteListTitle(count: Int) = "Notecraft ($count)"
     const val searchNotes = "搜索笔记…"
     const val newNote = "+ 新建笔记"
     const val importAction = "导入"
     const val exportAction = "导出"
     const val recent = "最近"
     const val sortByTitle = "按标题"
     fun noResultsFor(query: String) = "没有找到：$query"
     const val noNotesYet = "还没有笔记，点击 + 新建"
     const val uncategorized = "未分类"
     const val untitled = "无标题"
     const val selectNoteHint = "选择或新建一篇笔记"
 
     // Editor
     const val editorTitle = "标题"
     const val editorContent = "内容（Markdown）"
     const val editMode = "编辑"
     const val splitMode = "分栏"
     const val previewMode = "预览"
     const val save = "保存"
     const val pin = "钉屏"
     fun words(count: Int) = "字数：$count"
 
     // Save States
     const val saveUnsaved = "未保存"
     const val saveSaving = "保存中…"
     const val saveSaved = "已保存"
     fun saveError(msg: String) = "错误：$msg"
 
     // Delete Dialog
     const val deleteTitle = "删除笔记"
     const val deleteMessage = "确定要删除吗？此操作不可撤销。"
     const val deleteConfirm = "删除"
     const val cancel = "取消"
 
     // Settings
     const val settings = "设置"
     const val theme = "主题"
     const val themeLight = "浅色"
     const val themeDark = "深色"
     const val themeSystem = "跟随系统"
     const val fontSize = "字体大小"
     const val autoSave = "自动保存"
     const val autoSaveEnabled = "已启用"
     const val autoSaveDisabled = "已禁用"
    const val tabIndent = "Tab 缩进大小"
    const val loading = "加载中…"
    const val appearance = "外观"
    const val editorSection = "编辑器"
    const val aboutSection = "关于"
    const val version = "版本 1.0.0"
    const val deleteConfirmTitle = "确认删除"
    const val saveFailed = "保存失败"
    const val importFailed = "导入失败"
    const val exportFailed = "导出失败"
    const val loadFailed = "加载失败"
    const val trayShow = "显示笔记工坊"
    const val trayQuickNote = "快速笔记"
    const val trayCloseToTray = "关闭到托盘"
    const val trayQuit = "退出"
}

 # 中文化审计

 ## 当前状态

 所有 UI 文本均为硬编码英文，无国际化资源体系。

 ## 需翻译文本清单

 | 位置 | 原文 | 建议中文 | 优先级 |
 |------|------|---------|--------|
 | NoteApp.kt | "Notecraft" | "Notecraft" | P1 |
 | NoteApp.kt | "Search notes..." | "搜索笔记…" | P1 |
 | NoteApp.kt | "+ New Note" | "+ 新建笔记" | P1 |
 | NoteApp.kt | "Import" | "导入" | P1 |
 | NoteApp.kt | "Export" | "导出" | P1 |
 | NoteApp.kt | "Recent" | "最近" | P1 |
 | NoteApp.kt | "A-Z" | "按标题" | P1 |
 | NoteApp.kt | "No results for: " | "没有找到：" | P1 |
 | NoteApp.kt | "No notes yet. Create one with +." | "还没有笔记，点击 + 新建" | P1 |
 | NoteApp.kt | "Uncategorized" | "未分类" | P1 |
 | NoteApp.kt | "Select a note or create a new one" | "选择或新建一篇笔记" | P1 |
 | NoteApp.kt | "Unsaved" | "未保存" | P1 |
 | NoteApp.kt | "Saving..." | "保存中…" | P1 |
 | NoteApp.kt | "Saved" | "已保存" | P1 |
 | NoteApp.kt | "Error" | "错误" | P1 |
 | NoteApp.kt | "Edit" | "编辑" | P1 |
 | NoteApp.kt | "Split" | "分栏" | P1 |
 | NoteApp.kt | "Preview" | "预览" | P1 |
 | NoteApp.kt | "Pin" | "钉屏" | P1 |
 | NoteApp.kt | "Words: " | "字数：" | P1 |
 | NoteApp.kt | "Save" | "保存" | P1 |
 | NoteApp.kt | "Title" | "标题" | P1 |
 | NoteApp.kt | "Content (Markdown)" | "内容 (Markdown)" | P1 |
 | NoteApp.kt | "Delete Note" | "删除笔记" | P1 |
 | NoteApp.kt | "Are you sure? This cannot be undone." | "确定要删除吗？此操作不可撤销。" | P1 |
 | NoteApp.kt | "Delete" | "删除" | P1 |
 | NoteApp.kt | "Cancel" | "取消" | P1 |
 | SettingsScreen.kt | "Settings" | "设置" | P1 |
 | SettingsScreen.kt | "Theme" | "主题" | P1 |
 | SettingsScreen.kt | "Light" | "浅色" | P1 |
 | SettingsScreen.kt | "Dark" | "深色" | P1 |
 | SettingsScreen.kt | "System" | "跟随系统" | P1 |
 | SettingsScreen.kt | "Font Size" | "字体大小" | P1 |
 | SettingsScreen.kt | "Auto Save" | "自动保存" | P1 |
 | SettingsScreen.kt | "Enabled" | "已启用" | P1 |
 | SettingsScreen.kt | "Disabled" | "已禁用" | P1 |
 | SettingsScreen.kt | "Tab Indent Size" | "Tab 缩进大小" | P1 |

 ## 实施计划

 1. 创建资源文件体系（compose-components-resources）
 2. 定义 StringResources（英文 + 简体中文）
 3. 替换所有 Composable 中的硬编码字符串
 4. 错误提示本地化
 5. Desktop 托盘汉化
 6. Android 应用名称汉化
 7. Web 页面标题汉化

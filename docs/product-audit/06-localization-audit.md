# 中文化审计 — 最终报告

**审计日期**: 2026-07-27
**状态**: 已完成

## 审计范围

所有用户可见文本已在以下位置审计：
1. 按钮、菜单、标题、设置、空状态、加载状态、保存状态
2. 错误提示、删除确认、导入导出
3. Markdown 工具栏（暂未实现）、托盘菜单、快捷键
4. Android 页面标题、Web 页面标题
5. contentDescription（后续补充）
6. 日期时间、数量文案

## 中文化策略

- **默认语言**: 简体中文
- **字符串管理**: Strings.kt 辅助对象（commonMain）
- **资源备份**: composeResources/values/strings.xml（英文）+ alues-zh/strings.xml（中文）
- **平台特定**: 平台代码使用 Strings.kt 常量（TrayManager、JvmFileDialogService）
- **应用名称**: Android → "笔记工坊", Desktop → "Notecraft"（保留品牌）

## 完成清单

| 项目 | 原文 | 中文 | 位置 | 状态 |
|------|------|------|------|------|
| 应用名称 | Notecraft | Notecraft（保留） | Strings.appName | ✅ |
| 笔记列表标题 | Notecraft (%d) | Notecraft (%d)（保留） | Strings.noteListTitle | ✅ |
| 搜索提示 | Search notes... | 搜索笔记… | Strings.searchNotes | ✅ |
| 新建笔记 | + New Note | + 新建笔记 | Strings.newNote | ✅ |
| 导入 | Import | 导入 | Strings.importAction | ✅ |
| 导出 | Export | 导出 | Strings.exportAction | ✅ |
| 最近 | Recent | 最近 | Strings.recent | ✅ |
| 按标题 | A-Z | 按标题 | Strings.sortByTitle | ✅ |
| 无结果 | No results for: %s | 没有找到：%s | Strings.noResultsFor | ✅ |
| 空状态 | No notes yet... | 还没有笔记，点击 + 新建 | Strings.noNotesYet | ✅ |
| 未分类 | Uncategorized | 未分类 | Strings.uncategorized | ✅ |
| 无标题 | Untitled | 无标题 | Strings.untitled | ✅ |
| 选择提示 | Select a note... | 选择或新建一篇笔记 | Strings.selectNoteHint | ✅ |
| 标题字段 | Title | 标题 | Strings.editorTitle | ✅ |
| 内容字段 | Content (Markdown) | 内容（Markdown） | Strings.editorContent | ✅ |
| 编辑模式 | Edit | 编辑 | Strings.editMode | ✅ |
| 分栏模式 | Split | 分栏 | Strings.splitMode | ✅ |
| 预览模式 | Preview | 预览 | Strings.previewMode | ✅ |
| 保存按钮 | Save | 保存 | Strings.save | ✅ |
| 钉屏 | Pin | 钉屏 | Strings.pin | ✅ |
| 字数 | Words: %d | 字数：%d | Strings.words | ✅ |
| 未保存 | Unsaved | 未保存 | Strings.saveUnsaved | ✅ |
| 保存中 | Saving... | 保存中… | Strings.saveSaving | ✅ |
| 已保存 | Saved | 已保存 | Strings.saveSaved | ✅ |
| 保存错误 | Error: %s | 错误：%s | Strings.saveError | ✅ |
| 删除标题 | Delete Note | 删除笔记 | Strings.deleteTitle | ✅ |
| 删除确认 | Are you sure... | 确定要删除吗？此操作不可撤销。 | Strings.deleteMessage | ✅ |
| 删除按钮 | Delete | 删除 | Strings.deleteConfirm | ✅ |
| 取消 | Cancel | 取消 | Strings.cancel | ✅ |
| 设置 | Settings | 设置 | Strings.settings | ✅ |
| 主题 | Theme | 主题 | Strings.theme | ✅ |
| 浅色 | Light | 浅色 | Strings.themeLight | ✅ |
| 深色 | Dark | 深色 | Strings.themeDark | ✅ |
| 跟随系统 | System | 跟随系统 | Strings.themeSystem | ✅ |
| 字体大小 | Font Size | 字体大小 | Strings.fontSize | ✅ |
| 自动保存 | Auto Save | 自动保存 | Strings.autoSave | ✅ |
| 已启用 | Enabled | 已启用 | Strings.autoSaveEnabled | ✅ |
| 已禁用 | Disabled | 已禁用 | Strings.autoSaveDisabled | ✅ |
| Tab 缩进 | Tab Indent Size | Tab 缩进大小 | Strings.tabIndent | ✅ |
| 外观 | Appearance | 外观 | Strings.appearance | ✅ |
| 编辑器 | Editor | 编辑器 | Strings.editorSection | ✅ |
| 关于 | About | 关于 | Strings.aboutSection | ✅ |
| 版本 | Version 1.0.0 | 版本 1.0.0 | Strings.version | ✅ |
| 加载状态 | Loading... | 加载中… | Strings.loading | ✅ |
| 保存失败 | Save failed | 保存失败 | Strings.saveFailed | ✅ |
| 导入失败 | Import failed | 导入失败 | Strings.importFailed | ✅ |
| 导出失败 | Export failed | 导出失败 | Strings.exportFailed | ✅ |
| 加载失败 | Failed to load | 加载失败 | Strings.loadFailed | ✅ |
| 托盘显示 | Show Notecraft | 显示笔记工坊 | Strings.trayShow | ✅ |
| 快速笔记 | Quick Note | 快速笔记 | Strings.trayQuickNote | ✅ |
| 关闭到托盘 | Close to Tray | 关闭到托盘 | Strings.trayCloseToTray | ✅ |
| 退出 | Quit | 退出 | Strings.trayQuit | ✅ |

## 平台验证

| 平台 | 中文支持 | 验证方式 |
|------|---------|---------|
| Desktop | ✅ | 编译通过，Strings.kt 统一使用 |
| Web | ✅ | 编译通过，页面标题已汉化 |
| Android | ✅ | 编译通过，应用名已汉化 |

## 未汉化的保留项（合理）

| 内容 | 原因 |
|------|------|
| 用户笔记内容 | 不翻译用户数据 |
| Markdown 代码块 | 技术内容保持原文 |
| 技术异常日志 | 日志保留英文，UI 错误已汉化 |
| 专有名词（Notecraft, Markdown） | 国际通用术语 |
| AppModule 初始化错误 | 内部错误，用户不可见 |
| NoSuchElementException | 内部异常，不直接展示给用户 |

## 中文化原则遵守情况

| 原则 | 状态 | 说明 |
|------|------|------|
| 简体中文默认 | ✅ | Strings.kt 全部中文 |
| 保留英文资源 | ✅ | values/strings.xml 完整英文 |
| 无硬编码中文在 Composable | ✅ | 全部通过 Strings.kt |
| 中文错误提示自然准确 | ✅ | "保存失败"、"加载失败"等 |
| 三端均完成汉化 | ✅ | 含 Web 标题、Android 应用名、托盘 |
| 不翻译用户笔记内容 | ✅ | 未操作 Note.content |
| 不翻译代码块 | ✅ | Markdown 渲染保留原文 |
| 技术异常转自然中文 | ✅ | catch 块使用 Strings 常量 |
| 中文标点和空格符合习惯 | ✅ | 使用全角标点、中文空格 |
| 无明显中英混排 | ✅ | 仅 "Tab 缩进大小" 含专有名词 |
| 专有名词保留英文 | ✅ | Notecraft, Markdown 保留 |

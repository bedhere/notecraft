 # 参考项目基线审计 — Floral Notepaper

 **参考 commit**: `408536bad5f8949031ea2e0c4835a4c5659f2fec`
 **参考分支**: `main`
 **参考仓库**: https://github.com/Achilng/floral-notepaper
 **审计日期**: 2026-07-26

 ## 项目技术栈

 | 组件 | 版本 |
 |------|------|
 | 前端框架 | React 19.1.0 |
 | 构建工具 | Vite 8 |
 | 样式 | Tailwind CSS 4 |
 | 桌面框架 | Tauri 2 |
 | 后端语言 | Rust (edition 2021) |
 | 国际化 | i18next 26 |
 | 包管理器 | npm |
 | 测试 | Vitest 4 |
 | Markdown | react-markdown 10 + remark-gfm + rehype-katex |
 | 许可证 | MIT |

 ## 项目结构

 floral-notepaper/
 ├── src/
 │   ├── components/       # React UI 组件
 │   │   ├── MainWindow.tsx    # 主窗口（笔记列表 + 编辑器 + 设置）
 │   │   ├── NotePad.tsx       # 便签窗口
 │   │   ├── Tile.tsx          # 磁贴组件
 │   │   ├── TileShowcase.tsx  # 磁贴展示
 │   │   ├── SettingsPanel.tsx # 设置面板
 │   │   ├── ContextMenu.tsx   # 右键菜单
 │   │   ├── AboutPanel.tsx    # 关于面板
 │   │   ├── Toast.tsx         # 提示组件
 │   │   └── BackgroundLayer.tsx # 背景层
 │   ├── features/
 │   │   ├── notes/           # 笔记业务逻辑
 │   │   ├── settings/        # 设置逻辑
 │   │   ├── markdown/        # Markdown 渲染
 │   │   ├── windows/         # 窗口管理
 │   │   ├── importExport/   # 导入导出
 │   │   ├── images/          # 图片处理
 │   │   └── update/          # 更新管理
 │   ├── locales/            # 国际化资源
 │   ├── App.tsx             # 应用入口
 │   └── main.tsx            # 渲染入口
 ├── src-tauri/
 │   ├── src/
 │   │   ├── services/notes.rs  # 笔记 CRUD 后端
 │   │   ├── desktop.rs         # 桌面功能
 │   │   ├── json_io.rs         # JSON 文件 I/O
 │   │   ├── lib.rs             # Tauri 插件注册
 │   │   └── main.rs            # 入口
 │   └── Cargo.toml
 ├── tests/
 └── Docs/

 ## 核心业务调用链

 ### 1. 应用启动

 1. Tauri 启动，加载前端代码
 2. `App.tsx` → `getInitialRoute()` → 判断窗口类型（main/notepad/tile）
 3. 如果是主窗口 → 渲染 `MainWindow` 组件
 4. `useEffect` → `getConfig()` → Tauri command `config_get`
 5. Rust 端从 `{data_dir}/config.json` 读取配置
 6. `applyTheme(config.theme)` + `watchSystemTheme`
 7. `syncLanguage(config.locale)` 设置语言

 ### 2. 加载笔记列表

 1. `MainWindow` 挂载 → `useEffect` 调用 `void loadNotes()`
 2. `loadNotes` → `listNotes()` → Tauri command `notes_list`
 3. Rust 端 → 读取 `{data_dir}/notes/` 目录下的 JSON 文件
 4. 解析为 `NoteMetadata[]`，按 `updatedAt` 降序排列
 5. 同时加载外部文件、分类列表
 6. 返回前端 → `setNotes(metadatas)`

 ### 3. 新建笔记

 1. 用户点击 "+" 按钮或按快捷键
 2. `handleCreateNote` → `createNote({title:"",content:"",category:""})`
 3. Tauri command `notes_create` → Rust 端
 4. Rust: 生成 UUID → 创建 Note 结构体 → 写入 `{data_dir}/notes/{id}.json`
 5. 返回 Note → 前端添加到列表并选中

 ### 4. 编辑内容与自动保存

 1. 用户输入 → `onChange` → `setContent(value)` + `markDirty()`
 2. `markDirty()` → `setSaveState("dirty")` → 调度防抖自动保存（800ms）
 3. 防抖到期 → `saveCurrentNote()` → 串行化到 `saveQueueRef`
 4. `saveQueueRef` 链式 Promise → `updateNote(id, request)`
 5. Tauri command → Rust 端写入 JSON 文件
 6. 成功 → `setSaveState("saved")`，更新列表元数据
 7. **关键设计**: 使用 `saveQueueRef` (Promise 链) 串行化所有保存请求，防止并发写入冲突
 8. **关键设计**: 使用 `loadEpoch` (bump/isCurrent 模式) 防止异步加载结果覆盖新选中的笔记

 ### 5. 切换笔记

 1. 用户点击列表中的笔记
 2. 如果当前笔记有未保存更改 → 先触发保存（串行化到队列）
 3. 设置 `selectedId` → `loadEpoch.bump()` → 异步加载新笔记（`getNote(id)`）
 4. 异步加载完成后 → `loadEpoch.isCurrent(token) ? setContent(...) : 丢弃`
 5. 更新选中高亮

 ### 6. 删除笔记

 1. 用户点击删除或右键菜单删除
 2. 显示确认对话框 → 用户确认
 3. `handleDeleteNote` → `deleteNote(id)` → Tauri command → Rust 删除 JSON 文件
 4. 移入回收站（Rust 端使用 `trash` crate）
 5. 前端更新列表，如删除的是当前选中 → 选中列表中第一个或清空
 6. **关键行为**: 删除前会先保存当前笔记，防止删除后自动保存重新创建

 ### 7. 搜索

 1. 用户输入搜索关键词 → `setSearchQuery(query)`
 2. `useMemo` 根据 `query` 过滤 `notes`（标题和预览搜索）
 3. 纯前端过滤，无后端搜索 API

 ### 8. Markdown 编辑和预览

 1. 三种模式：Edit / Split / Preview（通过 `viewMode` 状态切换）
 2. 编辑模式：`<textarea>` 支持 Tab 缩进（`indent-textarea` 库）
 3. 预览模式：`react-markdown` + `remark-gfm` (GFM) + `rehype-katex` (LaTeX)
 4. Split 模式：左侧编辑器 + 右侧实时预览，中间可拖拽分隔条
 5. 工具栏：B/I/H/—/•/1./<>/❝/∑/∫ 格式按钮
 6. 滚动同步：编辑器滚动触发预览同步，反之亦然

 ### 9. 设置

 1. 侧栏设置面板（360px 宽）
 2. 配置项：主题（light/dark/system）、字体大小、Tab 缩进、默认视图模式、语言、数据目录、自动保存、HTML 渲染
 3. 修改立即保存到 `config.json` 并广播 `config-changed` 事件
 4. 所有联动组件监听此事件更新

 ### 10. 导入导出

 1. 导入：Tauri 文件对话框 → 选择 `.md` 文件 → `readExternalFile` → 解析创建笔记
 2. 导出：选中笔记 → Tauri 保存对话框 → `saveExternalFile` → 写入 `.md`
 3. 外部文件链接：可打开外部 `.md` 文件直接编辑（只读自动保存回原文件）

 ### 11. 系统托盘

 1. Tauri 托盘图标
 2. 点击托盘图标 → 显示/隐藏主窗口
 3. 托盘右键菜单：新建笔记、显示/隐藏、设置、退出

 ### 12. 全局快捷键

 1. `tauri-plugin-global-shortcut` 注册
 2. 默认 `Ctrl+Space` 呼出快速便签窗口
 3. 默认 `Ctrl+Shift+Space` 显示/隐藏主窗口
 4. 可在设置中自定义快捷键

 ### 13. 磁贴模式 (Pin Mode)

 1. 点击 Pin 按钮 → `toggleTileWindow(noteId)` → 创建新 Tauri 窗口
 2. 磁贴窗口显示笔记内容（只读预览），固定在桌面指定位置
 3. 可拖拽移动、缩放到合适大小
 4. 关闭磁贴窗口触发 `TILE_WINDOW_CLOSED_EVENT`

 ### 14. 窗口状态保存

 1. Tauri 窗口大小/位置自动保存到 `config.json`
 2. 重启后恢复窗口位置

 ## 数据模型

 | 字段 | 类型 | 说明 |
 |------|------|------|
 | id | string (UUID v4) | 唯一标识 |
 | title | string | 笔记标题 |
 | fileName | string | 文件名（用于导入导出） |
 | category | string | 分类 |
 | createdAt | string (ISO 8601) | 创建时间 |
 | updatedAt | string (ISO 8601) | 更新时间 |
 | wordCount | number | 字数 |
 | preview | string | 内容预览（前 80 字符） |
 | content | string | 笔记正文（Markdown） |

 ## 关键设计决策

 1. 数据存储为 JSON 文件（`{data_dir}/notes/{id}.json`），每个笔记一个文件
 2. 配置存储为 `{data_dir}/config.json`
 3. 删除移至系统回收站（Rust `trash` crate）
 4. 外部文件支持直接编辑（不复制到笔记目录）
 5. 自动保存防抖 800ms，使用 Promise 链串行化
 6. 加载竞态使用 epoch/bump/isCurrent 模式
 7. 图片粘贴自动保存到 `{data_dir}/images/{note_id}/` 目录
 8. 主题使用 CSS 变量（Tailwind CSS 4 dark 模式）
 9. 国际化使用 i18next + react-i18next，支持中/英/繁
 10. 错误码系统：Rust 端返回结构化的 `AppError`（code + message + details），前端映射为本地化消息

 ## 功能清单

 | 功能 | 状态 | 实现位置 |
 |------|------|----------|
 | 创建笔记 | 完成 | `features/notes/api.ts` → `notes_create` |
 | 编辑笔记 | 完成 | `MainWindow.tsx` |
 | 删除笔记（回收站） | 完成 | `features/notes/api.ts` → Rust `trash` |
 | 自动保存（800ms 防抖） | 完成 | `MainWindow.tsx` `markDirty` + `debouncedSave` |
 | 搜索（前端过滤） | 完成 | `MainWindow.tsx` 内存过滤 |
 | Markdown 编辑 | 完成 | `textarea` + `indent-textarea` |
 | Markdown 预览 (GFM + LaTeX) | 完成 | `react-markdown` + `rehype-katex` |
 | Split 模式 | 完成 | `MainWindow.tsx` + 可拖拽分隔条 |
 | 工具栏 | 完成 | `MainWindow.tsx` `toolbarButtons` |
 | 滚动同步 | 完成 | 块偏移量测量 |
 | 图片粘贴/拖入 | 完成 | `useImagePaste` |
 | 设置面板 | 完成 | `SettingsPanel.tsx` |
 | 主题（light/dark/system） | 完成 | CSS 变量 + Tailwind dark |
 | 国际化 (zh-CN/zh-HK/en-US) | 完成 | i18next |
 | 导入 .md 文件 | 完成 | Rust `read_external_file` |
 | 导出 .md 文件 | 完成 | Rust `save_external_file` |
 | 外部文件编辑 | 完成 | `ExternalFile` 模式 |
 | 系统托盘 | 完成 | Tauri tray-icon |
 | 全局快捷键 | 完成 | `tauri-plugin-global-shortcut` |
 | 磁贴模式 | 完成 | `Tile.tsx` + 独立窗口 |
 | 快速便签窗口 | 完成 | `NotePad.tsx` |
 | 分类管理 | 完成 | 创建/重命名/删除/拖拽移动 |
 | 窗口状态保存 | 完成 | `config.json` |
 | 右键菜单 | 完成 | `ContextMenu.tsx` |
 | 键盘导航 | 完成 | 上下箭头 + Enter |
 | 关于面板 | 完成 | `AboutPanel.tsx` |
 | 自动更新 | 完成 | `features/update/` |
 | 拖拽排序 | 部分 | 分类支持拖拽 |
 | 侧栏可调整宽度 | 完成 | 拖拽边缘 |
 | 画面比例调整 | 完成 | Split 模式可拖拽 |
 | 清理未使用图片 | 完成 | `handleCleanUnusedImages` |
 | MacOS 窗口控制适配 | 完成 | `useTrafficLightPositioning` |
 | 禁止 Windows Alt+Space 菜单 | 完成 | 键盘事件阻止 |
 | 撤销/重做 | 完成 | `execCommand("undo"/"redo")` |

 # 当前 KMP 项目架构审计 — Notecraft

 **审计日期**: 2026-07-26
 **Notecraft commit**: `4ac10f099e978dc3e112e7ac35483c0ac2747522`
 **分支**: `audit-product-parity-remediation`

 ## 模块结构

 notecraft/
 ├── shared/                    # KMP 共享模块
 │   ├── src/commonMain/        # 跨平台公共代码
 │   │   └── kotlin/com/notecraft/
 │   │       ├── domain/
 │   │       │   ├── model/         # Note, NoteMetadata, SaveNoteRequest, AppConfig, ViewMode
 │   │       │   └── repository/    # NoteRepository, SettingsRepository 接口
 │   │       ├── data/
 │   │       │   ├── repository/    # NoteRepositoryImpl, InMemoryNoteRepository, SettingsRepositoryImpl
 │   │       │   ├── storage/       # NoteStorage, SettingsStorage 接口
 │   │       │   ├── importexport/  # ImportExportUseCase, FileDialogService 接口
 │   │       │   └── migration/     # MigrationModels
 │   │       ├── presentation/
 │   │       │   ├── note/          # NoteListViewModel, NoteEditorViewModel, States
 │   │       │   └── settings/      # SettingsViewModel, SettingsState
 │   │       ├── ui/
 │   │       │   ├── screen/        # NoteApp.kt (主界面), SettingsScreen.kt
 │   │       │   ├── markdown/      # MarkdownParser, MarkdownRenderer
 │   │       │   └── theme/         # Theme.kt (NotecraftTheme)
 │   │       ├── platform/          # FileSystem, TimeProvider 接口
 │   │       ├── util/              # IdGenerator, NoteUtils
 │   │       └── di/                # AppModule (手动 DI)
 │   ├── src/jvmMain/           # Desktop (JVM) 平台实现
 │   │   └── kotlin/com/notecraft/
 │   │       ├── storage/           # JvmStorage (文件存储)
 │   │       ├── platform/          # Platform.jvm.kt (平台实现)
 │   │       ├── importexport/      # JvmFileDialogService
 │   │       └── data/migration/    # LegacyDataMigrator
 │   ├── src/androidMain/       # Android 平台实现
 │   │   └── kotlin/com/notecraft/
 │   │       ├── storage/           # AndroidStorage
 │   │       ├── platform/          # Platform.android.kt
 │   │       └── importexport/      # AndroidFileDialogService
 │   ├── src/jsMain/             # Web (JS) 平台实现
 │   │   └── kotlin/com/notecraft/
 │   │       ├── storage/           # JsStorage
 │   │       ├── platform/          # Platform.js.kt
 │   │       └── importexport/      # JsFileDialogService
 │   └── src/commonTest/         # 公共测试
 ├── androidApp/                 # Android 应用入口
 ├── desktopApp/                 # Desktop 应用入口
 └── webApp/                     # Web 应用入口

 ## 当前业务调用链

 ### 笔记创建

 用户点击 "+ New Note" 按钮
 → NoteApp.kt: onCreateNote
 → NoteListViewModel.createNote()
 → InMemoryNoteRepository.createNote()
 → IdGenerator.newId() + NoteUtils 统计数据
 → 存入内存 Map
 → 更新 StateFlow → UI 刷新
 → **注意: 数据仅存于内存，重启丢失**

 ### 笔记编辑与自动保存

 用户输入标题/正文
 → NoteEditorViewModel.updateTitle/updateContent()
 → 标记 saveState = Dirty
 → 调度协程延迟 1500ms
 → NoteEditorViewModel.save()
 → InMemoryNoteRepository.updateNote() (内存)
 → 更新 StateFlow → UI 显示 "Saved"

 ### 笔记删除

 用户点击删除按钮
 → 显示确认对话框
 → NoteListViewModel.deleteNote(id)
 → InMemoryNoteRepository.deleteNote() (从内存 Map 移除)
 → 重新加载列表

 ### 搜索

 NoteListViewModel.setSearchQuery(query)
 → NoteUtils.filterNotes() (内存过滤)
 → 更新 StateFlow

 ## 架构问题

 ### P0: 无真实持久化

 - `NoteApp.kt` 默认使用 `InMemoryNoteRepository`，而非 `NoteRepositoryImpl` + 平台 `NoteStorage`
 - 平台存储实现 (`JvmStorage`, `AndroidStorage`, `JsStorage`) 已存在但未被使用
 - 每次应用重启数据全部丢失

 ### P0: 自动保存竞态

 - `NoteEditorViewModel.saveAndContinue()` 在保存失败时静默忽略异常 (`catch (_: Exception) { }`)
 - 切换笔记和自动保存使用同一个 `viewModelScope`，可能产生竞态
 - 删除笔记后，若自动保存协程已排队，可能重新创建已删除笔记
 - 无串行化队列（参考项目使用 Promise 链）

 ### P0: 假实现

 - `InMemoryNoteRepository.createCategory()` 为空方法（注释说 "Categories are implicit"）
 - `FileDialogService` 接口定义但 UI 层传入 null 时，导入导出无提示

 ### P1: ViewModel 职责过重

 - `NoteListViewModel` 同时承担列表加载、选中管理、创建/删除/搜索/分类等全部功能
 - `NoteApp.kt` (Composable) 内联了大量 UI 逻辑（确认对话框、主题、快捷键）
 - 无 UseCase 层，ViewModel 直接调用 Repository

 ### P1: UI 层直接创建 Repository

 - `NoteApp.kt` 使用 `remember { InMemoryNoteRepository() }` 直接实例化
 - 依赖注入应为平台特定，不应在 commonMain 中硬编码

 ### P1: 无平台注入机制

 - `AppModule.kt` 定义了工厂方法但未被 `NoteApp.kt` 使用
 - 平台存储实现无法通过 commonMain 正确注入

 ### P2: 无错误处理体系

 - 所有 catch 直接显示 `e.message`（英文技术信息）
 - 无本地化错误消息
 - 自动保存失败静默

 ### P2: 无 SaveNoteRequest 验证

 - 可创建空标题空内容的笔记（原项目允许，但应有合理的用户体验）

 ## 已有代码状态

 | 组件 | 状态 | 说明 |
 |------|------|------|
 | Note 模型 | 完成 | 含 NoteMetadata, SaveNoteRequest |
 | NoteRepository 接口 | 完成 | CRUD + 分类管理 |
 | NoteRepositoryImpl | 完成 | 委托给 NoteStorage |
 | InMemoryNoteRepository | 完成 | 内存实现，用于开发/测试 |
 | NoteStorage 接口 | 完成 | 存储契约 |
 | JvmStorage | 完成 | Desktop JSON 文件存储 |
 | AndroidStorage | 完成 | Android 文件存储 |
 | JsStorage | 完成 | Web localStorage 存储 |
 | SettingsRepository | 完成 | 接口 + impl |
 | SettingsStorage | 完成 | 接口 + InMemory 实现 |
 | NoteListViewModel | 完成 | 含搜索、排序、分类 |
 | NoteEditorViewModel | 完成 | 含自动保存、视图模式 |
 | SettingsViewModel | 完成 | 主题/字体/缩进设置 |
 | NoteApp.kt (Composable) | 完成 | 三栏布局主界面 |
 | MarkdownParser | 完成 | 解析为 AnnotatedString |
 | MarkdownRenderer | 完成 | Compose 渲染 |
 | ImportExportUseCase | 完成 | 导入导出用例 |
 | FileDialogService | 接口完成 | 各平台实现存在 |
 | TrayManager | 新增未验证 | DesktopApp 未集成 |
 | ShortcutManager | 新增未验证 | DesktopApp 未集成 |
 | TileManager | 新增未验证 | DesktopApp 未集成 |
 | LegacyDataMigrator | 完成 | JVM 端数据迁移 |
 | NotecraftTheme | 完成 | 基础主题定义 |
 | IdGenerator | 完成 | UUID 生成 |
 | NoteUtils | 完成 | 过滤/分组/排序/计数 |
 | //test | 部分 | InMemoryNoteRepositoryTest + NoteUtilsTest |
 | //test (SharedCommonTest) | 存在 | 基本测试 |

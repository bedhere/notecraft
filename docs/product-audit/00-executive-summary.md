 # 产品审计与修复 - 执行摘要

 **审计日期**: 2026-07-26
 **审计分支**: `audit-product-parity-remediation`
 **Notecraft commit**: `4ac10f099e978dc3e112e7ac35483c0ac2747522`
 **Floral Notepaper commit**: `408536bad5f8949031ea2e0c4835a4c5659f2fec`

 ## 项目概览

 Notecraft 是一个基于 Kotlin Multiplatform + Compose Multiplatform 构建的跨平台笔记应用，目标是与 Floral Notepaper (Tauri 2 + React 桌面应用) 实现业务对齐。

 当前状态：KMP 项目框架已搭建，四个模块（androidApp、desktopApp、shared、webApp）可编译运行，shared 模块包含大量业务代码（模型、仓库、ViewModel、UI）。

 ## 关键发现

 ### 业务对齐 (P0)

 - **数据持久化使用 InMemory 实现**：`NoteRepositoryImpl` 依赖 `NoteStorage` 接口，但 `NoteApp.kt` 中默认注入的是 `InMemoryNoteRepository`，导致数据不持久。
 - **平台存储实现存在但未正确连接**：JvmStorage、AndroidStorage、JsStorage 均已实现，但 UI 层未正确注入这些平台实现。
 - **自动保存竞态风险**：`NoteEditorViewModel` 的自动保存和 `saveAndContinue` 方法存在竞态条件，删除后自动保存可能重新创建笔记。

 ### 中文化 (P1)

 - 所有 UI 文本均为硬编码英文。
 - 无资源文件体系，Composable 中直接写入英文字符串。
 - 错误提示为英文技术信息。

 ### Desktop UI (P1)

 - 当前 UI 为基础 Material 3 布局，与 Floral Notepaper 的优雅设计差异显著。
 - 无设计令牌系统，颜色/字体/间距直接硬编码。
 - 无托盘、全局快捷键、磁贴模式等桌面专属功能。
 - 右键菜单、Markdown 工具栏等交互缺失。

 ### Android (P1)

 - 当前共享三栏布局（列表+编辑器），不适合手机屏幕。
 - 无独立的手机导航（列表页→编辑页）。
 - 系统返回键行为未定义。
 - 软键盘遮挡处理缺失。
 - 旋转/后台重建状态恢复未验证。

 ## 修复优先级

 1. P0: 数据持久化修复
 2. P0: 自动保存竞态
 3. P1: 中文化资源体系
 4. P1: Desktop UI 设计令牌
 5. P1: Android 导航重构
 6. P2: 隐藏问题排查
 7. P2: 测试覆盖
 8. P2: 发布准备

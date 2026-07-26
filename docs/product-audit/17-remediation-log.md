 # 修复日志

 ## 批次 1: P0 修复 - 2026-07-26

 ### 1.1 Web 设置持久化

 **问题**: webApp 未注入 SettingsRepository, 导致设置不持久。
 **修改**: webApp/src/webMain/kotlin/com/bedhere/app/main.kt
 - 添加 JsSettingsStorage 和 SettingsRepositoryImpl 的创建和注入
 **构建**: shared:compileKotlinJvm ✓, desktopApp:compileKotlin ✓

 ### 1.2 自动保存竞态修复

 **问题**: NoteEditorViewModel 的 saveAndContinue 静默吞异常, 无保存队列串行化, 无加载竞态防护。
 **修改**: shared/src/commonMain/kotlin/com/notecraft/presentation/note/NoteEditorViewModel.kt
 - 添加 loadEpoch 机制 (bump/isCurrent), 防止异步加载结果覆盖新选中笔记
 - 添加 saveQueue (Job 链), 串行化所有保存请求
 - saveAndContinue 不再静默吞异常, 改为设置 Error 状态
 - 自动保存延迟从 1500ms 改为 800ms (匹配原项目)
 - clearEditor 和 onCleared 同时取消 saveQueue

 ### 1.3 中文化资源体系

 **问题**: 所有 UI 文本为硬编码英文。
 **修改**: 
 - 新增 shared/src/commonMain/composeResources/values/strings.xml (英文)
 - 新增 shared/src/commonMain/composeResources/values-zh/strings.xml (简体中文)
 - 新增 shared/src/commonMain/kotlin/com/notecraft/util/Strings.kt (字符串常量辅助对象)
 - 修改 NoteApp.kt: 使用 Strings.xxx 替换所有硬编码英文
 - 修改 SettingsScreen.kt: 使用 Strings.xxx 替换部分硬编码英文

 **注意**: composeResources 文件已创建, 但生成的 Res.string 访问器为 internal, 
 当前使用 Strings.kt 辅助对象。后续可通过 Res.string + stringResource() 完全切换。

 ## 待处理

 ### P0
 - Android 返回键处理
 - Android 进程重建数据恢复

 ### P1
 - Desktop 托盘/快捷键/磁贴集成
 - Android 手机导航重构 (列表→详情)
 - Markdown 工具栏
 - 右键菜单
 - 导入导出 UI 集成
 - 错误消息本地化

 ### P2
 - 设计令牌系统
 - Desktop UI 视觉还原
 - 可访问性
 - 测试覆盖
 - README/LICENCE/发布准备

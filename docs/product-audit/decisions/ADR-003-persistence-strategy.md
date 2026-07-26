 # ADR-003: 持久化策略

 **问题**: 如何实现跨平台数据持久化，确保保存/退出/恢复可靠。
 **候选方案**:
 1. 平台特定文件存储（JVM文件/Android文件/Web localStorage）
 2. 统一 SQLite (SQLDelight)
 3. 统一多平台键值存储
 **最终方案**: 方案 1 - 保留现有 NoteStorage 接口 + 平台实现
 **选择原因**: 
 - 原项目使用 JSON 文件，文件存储最接近原始行为
 - 平台实现已经完成(JvmStorage/AndroidStorage/JsStorage)
 - 不需要额外依赖
 **代价**: 各平台存储行为有细微差异（路径、配额）
 **三端影响**: Desktop→文件系统, Android→内部存储, Web→localStorage
 **修复**: 需要将 NoteApp.kt 从 InMemory 切换到真实平台实现

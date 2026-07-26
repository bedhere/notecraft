 # ADR-004: 本地化策略

 **问题**: 如何实现跨平台 UI 本地化，简体中文默认，保留英文。
 **候选方案**:
 1. Compose Multiplatform Resources (composeResources)
 2. 自定义 Strings 辅助对象
 3. 第三方 i18n 库
 **最终方案**: 方案 1 + 方案 2 混合
 **选择原因**: composeResources 提供官方跨平台资源管理，但生成的 Res 访问器为 internal; Strings 辅助对象提供编译安全的字符串引用，可随时切换到 Res.string
 **代价**: 当前在 Strings 和 Res 之间有间接层
 **三端影响**: 统一资源定义，所有平台共享

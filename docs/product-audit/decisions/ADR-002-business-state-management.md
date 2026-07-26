 # ADR-002: 业务状态管理

 **问题**: 如何管理笔记列表、编辑器、设置等业务状态，确保跨平台一致性。
 **候选方案**:
 1. 使用 Compose State + StateFlow（当前方案）
 2. 使用 Redux/MVI 框架
 3. 使用 Kotlin Flow + sealed interface
 **最终方案**: 保留当前 StateFlow + ViewModel 模式，但修复竞态问题
 **选择原因**: 当前方案与 Compose Multiplatform 集成良好，ViewModels 生命周期感知
 **代价**: ViewModel 职责需要拆分
 **三端影响**: AndroidX Lifecycle ViewModel 在 desktop/web 通过 androidx-lifecycle 支持

 # ADR-006: Android 导航

 **问题**: Android 端当前强制使用 Desktop 三栏布局，不适合手机屏幕。
 **候选方案**:
 1. 手机使用单页导航（列表→详情），平板使用双栏
 2. 手机使用 Bottom Navigation 多标签
 3. 使用 Navigation Compose 管理路由
 **最终方案**: 方案 1 - 手机单页导航 + 平板双栏
 **选择原因**: 与原项目 Desktop 三栏布局一致但适配移动端
 **代价**: 需要实现自适应布局检测（窗口宽度）
 **三端影响**: commonMain 实现布局逻辑，Android 端使用系统返回键

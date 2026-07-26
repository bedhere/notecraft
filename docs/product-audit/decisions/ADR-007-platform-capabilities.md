 # ADR-007: 平台能力声明

 **问题**: Desktop、Web、Android 三端共享业务逻辑但平台能力不同。
 **候选方案**:
 1. 统一 expect/actual 声明所有平台能力
 2. 按平台选择性编译
 3. 运行时能力检测
 **最终方案**: 方案 1 + 3 - expect/actual + 运行时检测
 **选择原因**: 编译时保证接口完整性，运行时自适应降级
 **代价**: 每个新平台能力需要在 expect 中声明
 **三端影响**:
 - Desktop: 托盘、快捷键、磁贴、文件系统、窗口管理
 - Web: localStorage、浏览器文件对话框、网页快捷键
 - Android: 内部存储、Activity Result API、分享、系统返回键

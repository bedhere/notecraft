 # Android 可用性审计

 ## 当前状态

 Android 端当前使用与 Desktop 相同的三栏布局（列表 + 编辑器），不适合手机屏幕。

 ## 问题清单

 | ID | 问题 | 严重级别 |
 |----|------|---------|
 | AND-01 | 手机显示三栏布局，屏幕宽度不足 | P1 |
 | AND-02 | 列表宽度 260dp 固定，不适合手机标准布局 | P1 |
 | AND-03 | 无返回键处理，系统返回直接退出应用 | P0 |
 | AND-04 | 软键盘弹出可能遮挡编辑区域 | P1 |
 | AND-05 | 输入法组合输入（中文）未测试 | P1 |
 | AND-06 | 旋转屏幕不保存编辑状态 | P1 |
 | AND-07 | Activity 重建后数据丢失 | P0 |
 | AND-08 | 进程被杀死后数据丢失 | P0 |
 | AND-09 | 深色模式未在 Android 上验证 | P1 |
 | AND-10 | 状态栏适配未处理 | P2 |
 | AND-11 | 点击区域可能过小（48dp 准则） | P2 |
 | AND-12 | 无分享功能 | P2 |
 | AND-13 | 文件导入导出未在 Android 上完整验证 | P1 |
 | AND-14 | 平板/横屏无双栏自适应 | P1 |
 | AND-15 | 删除笔记后导航状态不正确 | P1 |
 | AND-16 | Activity Context 可能泄漏到 commonMain | P0 |
 | AND-17 | 空笔记列表时无合理的空状态布局 | P2 |

 ## 目标布局

 ### 手机（竖屏）

 笔记列表页（全屏）
 → 点击笔记
 → 笔记编辑页（全屏，含返回按钮）
 → 系统返回或顶部返回 → 保存并回到列表

 ### 平板 / 横屏

 左侧笔记列表 + 右侧编辑详情（双栏）
 分隔线可调

 ## 要求

 1. commonMain 不导入 android.content / Activity / Context
 2. ViewModel 使用 AndroidX Lifecycle ViewModel
 3. 状态恢复使用 rememberSaveable + SavedStateHandle
 4. 导航使用 AndroidX Navigation Compose 或手写状态机
 5. 软键盘使用 imePadding() + WindowInsets
 6. 文件选择使用 ActivityResultContracts

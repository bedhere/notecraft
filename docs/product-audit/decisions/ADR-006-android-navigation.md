# ADR-006: Android 导航 (已实装, 2026-07-27)

## 问题

Android 平台使用与 Desktop 相同的三栏 Row 布局，不适合手机小屏幕。

## 候选方案

1. 在现有 NoteApp 中增加 isMobile 参数 — 代码耦合高
2. 独立 MobileNoteApp Composable (commonMain) — 职责清晰
3. MobileNoteApp 放在 androidMain — 失去跨平台性

## 最终方案

方案 2: 独立 MobileNoteApp Composable (commonMain)
文件: MobileNoteApp.kt

## 选择原因

1. commonMain 保持跨平台，Desktop/Web 不受影响
2. 窄屏手机: AnimatedContent 实现滑动导航
3. 宽屏/平板: Row 双栏布局 (35%/65%)
4. 系统返回: 通过 backSignal Int 参数从平台传递

## 实施细节

手机 (screenWidthDp < 600):
- 全屏列表页 + AnimatedContent 切换到全屏编辑器
- 编辑器 TopAppBar 有返回箭头
- backSignal 递增 → LaunchedEffect 检测 → 保存并返回列表 / 退出

平板 (screenWidthDp >= 600):
- Row: 列表(35%) + 编辑器(65%)
- 无页面切换
- backSignal 直接触发退出

## 三端影响

| 平台 | 入口 Composable | 影响 |
|------|----------------|------|
| Desktop | NoteApp.kt (不变) | 无影响 |
| Web | NoteApp.kt (不变) | 无影响 |
| Android | MobileNoteApp.kt (新) | 完整适配移动导航 |

## 代价

- 新增约 400 行 commonMain 代码
- 与 NoteApp.kt 部分渲染逻辑重复
- 需要 Android 传入 isWideScreen + backSignal

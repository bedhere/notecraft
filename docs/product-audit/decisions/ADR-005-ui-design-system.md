# ADR-005: UI 设计系统

## 问题

Notecraft 需要统一的 UI 设计语言以在 Desktop、Web、Android 三端保持视觉一致性，同时允许平台适配。

## 候选方案

### 方案 A: Material 3 默认主题
- 使用 Compose Material 3 默认色板和字体
- 优点：开箱即用，代码最少
- 缺点：与 Floral Notepaper 视觉差异大，缺乏品牌识别

### 方案 B: 自建设计令牌系统 + Material 3
- 创建 AppColors / AppTypography / AppSpacing 等独立令牌
- 组装成 MaterialTheme
- 优点：兼顾品牌一致性和 Material 兼容性
- 缺点：需要维护额外代码

### 方案 C: 完全自建 Composables（不使用 Material）
- 每个组件从零构建
- 优点：完全控制
- 缺点：工作量大，失去 Material 的 accessibility 和交互保障

## 最终方案

方案 B: 自建设计令牌 + Material 3

## 选择原因

1. 与 Floral Notepaper 的品牌视觉对齐（绿色主色 + 暖白背景）
2. 利用 Material 3 成熟的组件库（按钮、对话框、TextField 等）
3. 设计令牌可作为三端共享的单一视觉真相源
4. 可缩放字体系统通过 AppTypography.toMaterial3() 实现
5. 成本合理，与现有代码架构兼容

## 代价

- 新增 4 个文件 (AppColors / AppTypography / AppSpacing)
- 需要开发者了解令牌命名规范
- 某些 Material 组件无法完全覆盖自定义（如抽屉动画）

## 三端影响

| 平台 | 影响 |
|------|------|
| Desktop | 完全共享 commonMain 主题代码 |
| Web | 完全共享 commonMain 主题代码 |
| Android | 完全共享 commonMain 主题代码；Android 特有状态栏适配额外处理 |

## 令牌体系

AppColors:
- primaryLight/Dark, onPrimary, primaryContainer
- surface, background, onSurface
- outline, surfaceVariant
- semantic (error, secondary, tertiary)
- editor-specific (selection highlight, line highlight)

AppTypography:
- display (28sp, 24sp)
- heading (20sp, 18sp, 16sp)
- body (16sp, 14sp, 12sp)
- label (14sp, 12sp, 11sp)
- code (13sp monospace)
- toMaterial3(fontSize) 缩放方法

AppSpacing:
- 4dp grid (xs:2, sm:4, md:8, lg:12, xl:16, xxl:20, xxxl:24, section:32)
- component (sidebarWidth:260dp, settingsWidth:300dp)
- editor (editorPadding:16dp, searchFieldHeight:46dp)

Theme.kt:
- LightColors / DarkColors (引用 AppColors)
- AppShapes (4dp / 6dp / 8dp)
- NotecraftTheme(darkTheme, fontSize) Composable

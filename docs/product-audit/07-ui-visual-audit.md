# UI 视觉审计 — Floral Notepaper vs Notecraft

**审计日期**: 2026-07-27
**参考项目**: Floral Notepaper (commit 408536bad5f8949031ea2e0c4835a4c5659f2fec)
**当前项目**: Notecraft (KMP + Compose Multiplatform)

## 设计语言总览

| 维度 | Floral Notepaper | Notecraft (当前) | 差异 |
|------|-----------------|-----------------|------|
| 框架 | Tailwind CSS 4 + CSS 变量 | Material 3 + Compose Theme | 平台差异 |
| 颜色 | 绿色主色调 + 暖白背景 | 绿色主色调 + 暖白背景 | 基本一致 |
| 字体 | System UI 字体栈 | System Default | 基本一致 |
| 圆角 | 较小圆角(~4px) | 4dp/6dp/8dp | 一致 |
| 阴影 | Tailwind shadow-sm | Material 3 elevation | 一致 |
| 图标 | lucide-react SVG 图标 | 文本字符替代 | 待改进 |

## 色彩体系

### 已提取的设计色值

参照 Floral Notepaper 的 Tailwind 配置和实际渲染色值：

`
浅色主题:
  主色:     #5B8C5A (notecraft 绿)
  背景:     #FEFCF5 (暖白)
  表面:     #FEFCF5
  文本:     #2C2C2C
  次要文本: #6B7280
  边框:     #D4C9B8
  选中行:   #D9EAD3 (主色容器)

深色主题:
  主色:     #7DB07A
  背景:     #121212
  表面:     #1E1E1E
  文本:     #E4E0D9
  边框:     #3E3E3E
  选中行:   #2D5A2C
`

### Notecraft 当前实现

✅ 已通过 AppColors.kt 定义完整的明暗色板
✅ 已通过 AppTypography.kt 定义可缩放字体系统
✅ 已通过 AppSpacing.kt 定义 4dp 网格间距系统

## 布局审计

| 组件 | 原项目 (Floral Notepaper) | Notecraft | 状态 |
|------|--------------------------|-----------|------|
| 侧栏宽度 | ~260px | 260dp | ✅ |
| 设置面板 | 360px | 300dp | ✅ (略窄但合理) |
| 笔记列表 | 标题 + 预览 + 时间 | 标题 + 删除按钮 | ⚠️ 缺少预览和时间 |
| 编辑器工具栏 | B/I/H/—/•/1./<>/❝/∑/∫ | 无 | ❌ 待实现 |
| 视图模式切换 | Edit / Split / Preview (按钮) | SegmentedButton | ✅ |
| 保存状态 | 图标 + 文字 | 文字 | ⚠️ 缺少图标 |
| 搜索框 | 带图标的搜索输入 | OutlinedTextField | ✅ |
| 右键菜单 | ContextMenu 组件 | 无 | ❌ 待实现 |
| 删除确认 | 确认对话框 | AlertDialog | ✅ |
| 分隔条(Split) | 可拖拽 | 固定 VerticalDivider | ⚠️ 固定宽度 |

## 交互状态

| 状态 | 原项目 | Notecraft | 状态 |
|------|--------|-----------|------|
| Hover | background-color 轻微变化 | 无 (Desktop 平台无 hover 处理) | ⚠️ |
| Selected | primary-container 颜色 | primaryContainer | ✅ |
| Focus | outline ring | FocusRequester | ✅ |
| Pressed | scale(0.98) | 默认 Material ripple | ✅ |

## 设计令牌系统（已建立）

`
AppColors.kt       — 完整明暗色板 + 语义色
AppTypography.kt   — 可缩放字体系统 (基准 14sp)
AppSpacing.kt      — 4dp 网格系统 + 组件尺寸常量
Theme.kt           — Material 3 主题装配
`

## 待办项

1. 笔记列表项添加预览行和时间显示
2. 实现 Markdown 工具栏 (B/I/H/—/•/1./<>)
3. Desktop Hover 状态优化
4. 右键菜单实现
5. Split 模式可拖拽分隔条
6. 图标系统 (zebraz/app 使用 Material Icons 或 lucide 替代)

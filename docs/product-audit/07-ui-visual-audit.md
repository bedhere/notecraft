 # UI 视觉审计

 ## 当前 KMP UI 状态

 - 基础 Material 3 组件
 - 硬编码颜色、间距、字号
 - 无设计令牌系统
 - 与 Floral Notepaper 视觉差异显著

 ## Floral Notepaper 视觉特征

 | 元素 | 原项目特征 |
 |------|-----------|
 | 整体风格 | 柔和、暖色调、纸张质感 |
 | 背景色 | 暖白/米色 (Tailwind: paper/warm/cloud) |
 | 字体 | 无衬线中英文混合 (font-display, font-body) |
 | 标题字号 | 20px bold |
 | 正文字号 | 14px (可配置) |
 | 行高 | 1.9（编辑区） |
 | 侧栏宽度 | 280px，可拖拽调整 |
 | 笔记列表 | 简洁列表，选中高亮，hover 效果 |
 | 分类标签 | 大写字母 + 圆点 |
 | 编辑器 | 无边框 textarea，清爽 |
 | 分割线 | 暖色半透明 |
 | 工具栏 | 小图标按钮，hover 淡入 |
 | 设置面板 | 360px 侧栏，滑动展开 |
 | 状态栏 | 10px 字体，显示行号/格式/编码 |
 | 动画 | note-enter, view-fade, menu-enter 等 |
 | 右键菜单 | 弹出式，毛玻璃效果 |
 | 深色模式 | CSS 变量切换 |

 ## 设计令牌建议

 建议建立以下设计令牌体系（后续在 ADR-005 中详细定义）：

 - AppColors: 主色、背景、文字、状态色
 - AppTypography: 标题/正文/标签字号字重
 - AppSpacing: 间距比例系统
 - AppShapes: 圆角规范
 - AppElevation: 阴影层级
 - AppMotion: 动画时长与曲线
 - AppComponentDefaults: 组件默认值

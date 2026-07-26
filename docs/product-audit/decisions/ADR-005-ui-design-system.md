 # ADR-005: UI 设计系统

 **问题**: 如何统一三端视觉风格，Desktop 接近 Floral Notepaper。
 **候选方案**:
 1. 使用 Material 3 默认主题（当前方案）
 2. 自定义设计令牌 + Material 3 覆盖
 3. 完全自定义组件系统
 **最终方案**: 方案 2 - 自定义设计令牌 + Material 3 覆盖
 **选择原因**: Material 3 提供基础组件和可访问性，设计令牌确保视觉一致性
 **代价**: Desktop 需要额外样式覆盖以接近原项目
 **三端影响**: 共享设计令牌，Android 采用移动端自适应布局
 **设计令牌**: AppColors, AppTypography, AppSpacing, AppShapes, AppElevation

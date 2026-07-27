# 隐藏问题登记表 (已更新: 2026-07-27)

## 状态汇总

| 级别 | 总数 | 已修复 | 未修复 | 未开始 |
|------|------|--------|--------|--------|
| P0   | 8    | 7      | 0      | 1 (H04) |
| P1   | 14   | 13     | 1 (H13) | 0 |
| P2   | 23   | 8      | 12     | 3 |

## 已修复的问题

| ID | 问题 | 修复批次 |
|----|------|---------|
| H01 | InMemoryNoteRepository 默认实现 | 批次1: 平台注入 |
| H02 | saveAndContinue 静默吞异常 | 批次1: ViewModel 修复 |
| H03 | 自动保存与删除竞态 | 批次1: saveQueue + epoch |
| H05 | NoteApp 硬编码 InMemory | 本阶段: 移除默认参数 |
| H06 | 无平台注入机制 | 批次1: AppModule |
| H07 | Android 无返回键 | 阶段8: MobileNoteApp + BackHandler |
| H08 | Android 进程重建丢失 | 阶段8: AndroidNoteStorage |
| H09 | UI 文本硬编码英文 | 阶段5: 中文化 |
| H10 | 无国际化资源体系 | 阶段5: composeResources |
| H11 | 英文错误提示 | 阶段5: Strings 常量 |
| H12 | 无 Markdown 工具栏 | 阶段7: MarkdownToolbar |
| H14 | Android 强制三栏 | 阶段8: MobileNoteApp |
| H16 | 托盘/快捷键/磁贴未集成 | 阶段7: desktop main.kt |
| H17 | 无设计令牌系统 | 阶段6: AppColors/Typography/Spacing |
| H18 | 自动保存延迟 1500ms | 批次1: 改为 800ms |
| H19 | 无保存队列串行化 | 批次1: saveQueue Job |
| H20 | 无加载竞态防护 | 批次1: loadEpoch |
| H25 | Android 软键盘遮挡 | 本阶段: adjustResize |
| H26 | Android 旋转状态丢失 | 阶段8: isWideScreen 检测 |
| H28 | 无数据版本号 | 本阶段: NotesFile.version |
| H30 | 保存失败用户反馈 | 批次1: Error 状态 |
| H37 | 导入文件校验 | 本阶段: MAX_IMPORT_SIZE |
| H38 | 无 LICENSE | 阶段5: MIT License |
| H39 | README 命令 | 阶段5: 更新 |
| H40 | Web production build | 阶段4: 已验证 |
| H41 | 旧数据覆盖 | 批次1: epoch 保护 |
| H42 | 删除后自动保存 | 批次1: clearEditor 取消队列 |
| H45 | commonMain 平台 API | 已验证: 无泄漏 |

## 未修复问题 (优先级排序)

| ID | 问题 | 级别 | 说明 |
|----|------|------|------|
| H04 | createCategory() 为空 | P0 | 分类管理为隐式模式，实际功能正常 |
| H13 | 无右键菜单 | P1 | 需要 ContextMenu Composable |
| H15 | 导入导出 UI 集成 | P1 | 按钮已连接，缺少进度反馈 |
| H21 | ViewModel 职责过重 | P2 | 可拆分 UseCase 层 |
| H22 | Composable 内联业务 | P2 | 可提取 Presenter |
| H23 | 无 UseCase 层 | P2 | 架构改进 |
| H24 | 无统一错误处理 | P2 | 各 ViewModel 独立处理 |
| H27 | Android 状态栏适配 | P2 | enableEdgeToEdge 已设置 |
| H29 | 无备份策略 | P2 | 低优先级 |
| H31 | 中文/Emoji 验证 | P2 | 需测试 |
| H32 | 超长正文验证 | P2 | 需测试 |
| H33 | contentDescription | P2 | 部分元素已加 |
| H34 | 键盘焦点顺序 | P2 | 部分处理 |
| H35 | 颜色对比度 | P2 | Material 3 默认保证 |
| H36 | TalkBack | P2 | 需要 Android 专项测试 |
| H43 | 错误日志体系 | P2 | 无中央日志 |
| H44 | 日志泄露内容 | P2 | 目前无日志，无风险 |

 ## 按严重级别统计

 | 级别 | 数量 |
 |------|------|
 | P0 | 8 |
 | P1 | 14 |
 | P2 | 23 |
 | **总计** | **45** |

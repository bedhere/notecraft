 # 业务流映射 — KMP vs Floral Notepaper

 ## 1. 创建笔记

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 用户动作 | 点击 "+" 按钮 | 点击 "+ New Note" 按钮 |
 | 前端 | MainWindow.tsx → handleCreateNote | NoteApp.kt → NoteListViewModel.createNote() |
 | API 调用 | invoke("notes_create") | InMemoryNoteRepository.createNote() |
 | 后端实现 | Rust: 生成 UUID, 写入 JSON 文件 | Kotlin: 生成 ID, 存入内存 Map |
 | 数据存储 | {data_dir}/notes/{id}.json | 内存（重启丢失） |
 | 成功反馈 | 刷新列表，选中新笔记 | 刷新列表，选中新笔记 |
 | 失败反馈 | toast 错误消息 | error 状态显示 |

 **差异**: Notecraft 无真实持久化，重启后数据丢失。

 ## 2. 编辑笔记

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 用户动作 | 点击笔记, 编辑内容 | 点击笔记, 编辑内容 |
 | 状态管理 | React useState | StateFlow |
 | 自动保存 | 800ms 防抖, Promise 链串行化 | 1500ms 防抖, 协程 viewModelScope |
 | 保存实现 | updateNote Tauri command → Rust 写文件 | InMemoryNoteRepository.updateNote() |
 | 竞态防护 | loadEpoch (bump/isCurrent) + saveQueueRef | 无防护 |
 | 保存状态 | idle→dirty→saving→saved→error | Idle→Dirty→Saving→Saved→Error |
 | 字数和时间 | 更新时间显示 | 字数显示 |

 **差异**: Notecraft 自动保存延迟更大(1500ms vs 800ms), 无竞态防护, 数据不持久。

 ## 3. 删除笔记

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 用户动作 | 右键/按钮 + 确认 | 点击 "x" 按钮 + AlertDialog |
 | 确认方式 | 右键菜单 → Delete | AlertDialog |
 | 后端行为 | Rust: 移至回收站 (trash crate) | 从内存 Map 移除 |
 | 删除后处理 | 更新列表, 选中下一项 | 更新列表, 选中第一项 |
 | 竞态风险 | 保存队列完成后才删除 | 删除后自动保存可能重新创建 |

 **差异**: Notecraft 无回收站支持, 存在删除后自动保存重新创建的竞态。

 ## 4. 搜索

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 用户动作 | 输入搜索框 | 输入搜索框 |
 | 实现 | 前端 useMemo 过滤 | ViewModel setSearchQuery → filterNotes |
 | 过滤范围 | 标题 + 预览 | NoteUtils.filterNotes |
 | 性能 | 内存过滤 | 内存过滤 |

 **差异**: 基本一致。

 ## 5. Markdown

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 渲染引擎 | react-markdown + rehype-katex | 自定义 MarkdownParser + MarkdownRenderer |
 | GFM 支持 | 完整 (remark-gfm) | 自实现，可能不完整 |
 | LaTeX 支持 | 完整 (rehype-katex) | 无 |
 | 视图模式 | Edit / Split / Preview | Edit / Split / Preview |
 | 工具栏 | B/I/H/—/•/1./<>/❝/∑/∫ | 无 |
 | 滚动同步 | 块偏移量测量同步 | 无 |
 | 图片支持 | 粘贴/拖入图片 | 无 |

 **差异**: Notecraft 的自实现 Markdown 渲染可能缺少 GFM 完整性和 LaTeX 支持。

 ## 6. 设置

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 配置项 | 主题/字体/缩进/视图/语言/目录/自动保存/HTML | 主题/字体/缩进/自动保存 |
 | 存储位置 | config.json | InMemorySettingsStorage |
 | 广播机制 | config-changed 事件 | 直接状态更新 |

 **差异**: Notecraft 设置少、不持久。

 ## 7. 导入导出

 | 步骤 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 导入 UI | Tauri 文件对话框 | FileDialogService (平台实现存在) |
 | 导出 UI | Tauri 保存对话框 | FileDialogService (平台实现存在) |
 | 格式 | .md 文件 | .md 文件 |
 | 外部文件 | 支持直接编辑 | 无 |

 **差异**: 接口已定义但 UI 层未正确集成 FileDialogService。

 ## 8. Desktop 专属功能

 | 功能 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 系统托盘 | Tauri tray-icon | 尝试实现 (TrayManager.kt) 但未集成 |
 | 全局快捷键 | tauri-plugin-global-shortcut | 尝试实现 (ShortcutManager.kt) 但未集成 |
 | 磁贴模式 | 独立 Tauri 窗口 | 尝试实现 (TileManager.kt) 但未集成 |
 | 窗口状态保存 | config.json | 未实现 |

 ## 9. Web 专属

 | 功能 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 持久化 | N/A (桌面应用) | JsStorage (localStorage) |
 | 刷新恢复 | N/A | 未集成 |

 ## 10. Android 专属

 | 功能 | Floral Notepaper | Notecraft (KMP) |
 |------|-----------------|-----------------|
 | 导航 | N/A (桌面应用) | 三栏布局不适合手机 |
 | 返回键 | N/A | 未处理 |
 | 生命周期 | N/A | 未处理 |
 | 状态恢复 | N/A | 未处理 |
 | 持久化 | N/A | AndroidStorage 已实现但未集成 |
 | 分享 | N/A | 未实现 |
 | 文件选择 | N/A | AndroidFileDialogService 已实现 |

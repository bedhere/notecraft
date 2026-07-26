 # 平台边界审计

 ## commonMain 平台 API 检查

 | 文件 | 风险 | 级别 |
 |------|------|------|
 | shared/src/commonMain/.../util/IdGenerator.kt | 无平台 API | 安全 |
 | shared/src/commonMain/.../util/NoteUtils.kt | 无平台 API | 安全 |
 | shared/src/commonMain/.../ui/screen/NoteApp.kt | 使用 Material 3 Compose API | 安全 |
 | shared/src/commonMain/.../presentation/note/ | 使用 AndroidX Lifecycle ViewModel | 安全（跨平台兼容） |
 | shared/src/commonMain/.../data/repository/ | 无平台 API | 安全 |
 | shared/src/commonMain/.../data/storage/NoteStorage.kt | 纯接口 | 安全 |

 ## expect/actual 清单

 | expect 声明 | commonMain | jvmMain (Desktop) | androidMain | jsMain |
 |-------------|-----------|-------------------|-------------|--------|
 | Platform.currentTimeMillis() | ✓ | ✓ | ✓ | ✓ |
 | FileSystem | ✓ | ✓ | ✓ | ✓ |
 | NoteStorage | ✓ | JvmNoteStorage | AndroidNoteStorage | JsNoteStorage |
 | SettingsStorage | ✓ | JvmSettingsStorage | AndroidSettingsStorage | JsSettingsStorage |
 | FileDialogService | ✓ | JvmFileDialogService | AndroidFileDialogService | JsFileDialogService |

 ## 结论

 - commonMain 无平台 API 泄漏
 - expect/actual 覆盖完整
 - 各平台 Storage 实现一致

 # 运行时验证

 ## 验证环境

 - OS: Windows
 - JDK: (通过 gradlew 确认)
 - Node: (通过 webApp 构建确认)

 ## 构建验证结果

 ### shared 测试

 命令: `./gradlew :shared:allTests`
 预期: 编译通过，测试运行

 ### Desktop 编译

 命令: `./gradlew :desktopApp:compileKotlinDesktop`
 预期: 编译通过

 ### Web development build

 命令: `./gradlew :webApp:jsDevelopmentExecutableCompileSync`
 预期: 编译通过

 ### Web production build

 命令: `./gradlew :webApp:jsProductionExecutableCompileSync`
 预期: 编译通过

 ### Android assembleDebug

 命令: `./gradlew :androidApp:assembleDebug`
 预期: APK 生成

 ## 功能运行验证

 | 验证项 | Desktop | Web | Android | 说明 |
 |--------|---------|-----|---------|------|
 | 应用启动 | 待验证 | 待验证 | 待验证 | |
 | 创建笔记 | 待验证 | 待验证 | 待验证 | |
 | 编辑内容 | 待验证 | 待验证 | 待验证 | |
 | 自动保存 | 待验证 | 待验证 | 待验证 | |
 | 删除笔记 | 待验证 | 待验证 | 待验证 | |
 | 搜索 | 待验证 | 待验证 | 待验证 | |
 | Markdown 预览 | 待验证 | 待验证 | 待验证 | |
 | 设置修改 | 待验证 | 待验证 | 待验证 | |
 | 导入导出 | 待验证 | 待验证 | 待验证 | |
 | 深色模式 | 待验证 | 待验证 | 待验证 | |
 | 数据持久化（重启） | 待验证 | 待验证 | 待验证 | 预期不持久 |
 | Android 返回键 | N/A | N/A | 待验证 | |
 | Android 旋转恢复 | N/A | N/A | 待验证 | |

 ## 运行时问题汇总

 待构建通过后填写。

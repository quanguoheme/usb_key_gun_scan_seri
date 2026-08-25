
更新日志:
# Android app 工程 版本迁移

## 背景:   
 - H:\debug_app\111_user_sdk\app_touchscreen_test 是通过 gradle-9.4.1 编译,可以编译成功
 - 当前项目是 通过Android studio 3.6.2 编译 ,可以编译成功,版本比较老,
 - 项目功能不要改变,只是改变  
 - 不要烧录 ,只要可以编译通过即可
 - PS C:\Users\jason> java -version
openjdk version "17.0.4.1" 2022-08-12
OpenJDK Runtime Environment Temurin-17.0.4.1+1 (build 17.0.4.1+1)
OpenJDK 64-Bit Server VM Temurin-17.0.4.1+1 (build 17.0.4.1+1, mixed mode, sharing)

## 需求: 
 - 参考 H:\debug_app\111_user_sdk\app_touchscreen_test 工程实现 ,把当前  Android app  项目改成  gradle-9.4.1 编译.
 
- **构建体系**：升级至 Gradle 9.4.1 + AGP 9.2.1，采用 Kotlin DSL (`.gradle.kts`) + Version Catalog (`libs.versions.toml`)。
- **依赖与适配**：迁移到 AndroidX (`androidx.appcompat`)，配置 `namespace`，保留 SO 原生库支持与现有业务逻辑。
- **SDK兼容**：`minSdk = 29`，`compileSdk = 34/36`，目标 JDK 17。

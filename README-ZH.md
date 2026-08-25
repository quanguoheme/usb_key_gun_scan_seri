# USB Key Gun Scan Serial (Android App 工程迁移更新日志)

[English](README.md) | [中文](README-ZH.md)

---

## 1. 项目概述
本项目为 Android 平台下的 USB 条码扫描枪 / CDC 串口通信示例工程（`usb_key_gun_scan_seri`），用于实现通过 `UsbDevice` 访问 CDC USB 扫码设备、JNI 串口原生通信及按键扫码功能。

---

## 2. 迁移背景与目标

- **历史背景**：原工程基于旧版 Android Studio 3.6.2 及早期 Gradle / Support 依赖库构建，构建工具链陈旧。
- **迁移目标**：
  - 参考现代构建范式，全面迁移至 **Gradle 9.4.1** 构建体系。
  - 保证工程构建通过（无需烧录测试，确保 Gradle 顺利完成编译与打包）。
  - 保持业务功能、JNI 原生库（`.so`）与 USB CDC 设备交互逻辑完全不变。

---

## 3. 环境要求

- **JDK 版本**：OpenJDK 17 (推荐 OpenJDK 17.0.4.1 / Eclipse Temurin 17 或以上)
- **Gradle 版本**：9.4.1 (通过项目内置 `gradlew` 驱动)
- **Android Gradle Plugin (AGP)**：9.2.1
- **支持架构**：`minSdk = 29` (Android 10.0+), `targetSdk = 34`, `compileSdk = 36`

---

## 4. 核心变更记录 (Changelog)

### 4.1 构建体系重构 (Build System Modernization)
- **升级构建工具链**：
  - Gradle 升级至 `9.4.1`。
  - Android Gradle Plugin (AGP) 升级至 `9.2.1`。
- **迁移至 Kotlin DSL**：
  - 将所有 Groovy 构建脚本 (`build.gradle`, `settings.gradle`) 重构为 Kotlin DSL 格式 (`build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`)。
- **引入 Version Catalog (版本目录)**：
  - 新增 `gradle/libs.versions.toml`，集中化管理插件版本与依赖版本。

### 4.2 依赖与框架适配 (Dependencies & AndroidX)
- **AndroidX 迁移**：
  - 移除已废弃的 Android Support 库，迁移至 `androidx.appcompat:appcompat:1.6.1`。
- **命名空间规范 (Namespace)**：
  - 显式配置 `namespace = "com.example.scan_module_add_key_demo"`，符合新版 AGP 强制要求。
- **JDK 17 兼容性配置**：
  - 配置 `JavaVersion.VERSION_17` 作为源代码与目标字节码兼容级别。

### 4.3 原生库与设备通信 (JNI & USB Communication)
- **JNI 原生库支持**：
  - 在 `sourceSets` 中正确配置 `jniLibs.directories.add("libs")`，确保 JNI `.so` 动态链接库正确打包进 APK。
- **USB CDC 设备访问适配**：
  - 支持通过 Android `UsbDevice` 接口与 CDC USB 串口扫码枪设备进行数据通信。

---

## 5. 构建与验证

### 编译 APK
在项目根目录下执行以下命令完成 Debug 编译：

```bash
# Windows PowerShell / CMD
.\gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

编译生成的 APK 文件位于：
`app/build/outputs/apk/debug/app-debug.apk`

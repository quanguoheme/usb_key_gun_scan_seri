# USB Key Gun Scan Serial (Android App Project Migration Changelog)

[English](README.md) | [中文](README-ZH.md)

---

## 1. Project Overview
This project is an Android sample application (`usb_key_gun_scan_seri`) designed for USB barcode scanner / CDC serial communication. It implements USB CDC barcode scanner access via Android's `UsbDevice` API, native JNI serial communication, and hardware key-triggered scanning.

---

## 2. Migration Background & Objectives

- **Legacy Background**: The original project was built using legacy Android Studio 3.6.2 and outdated Gradle / Android Support libraries.
- **Migration Objectives**:
  - Migrate the entire build infrastructure to **Gradle 9.4.1** and modern AGP conventions.
  - Ensure project builds and packages successfully without errors (compilation verification only, flashing is not required).
  - Retain existing business logic, JNI native dynamic libraries (`.so`), and USB CDC communication workflows completely unchanged.

---

## 3. Environment Requirements

- **JDK Version**: OpenJDK 17 (OpenJDK 17.0.4.1 / Eclipse Temurin 17 or higher recommended)
- **Gradle Version**: 9.4.1 (driven via project wrapper `./gradlew`)
- **Android Gradle Plugin (AGP)**: 9.2.1
- **Target Platform**: `minSdk = 29` (Android 10.0+), `targetSdk = 34`, `compileSdk = 36`

---

## 4. Changelog & Key Modifications

### 4.1 Build System Modernization
- **Toolchain Upgrade**:
  - Upgraded Gradle wrapper to `9.4.1`.
  - Upgraded Android Gradle Plugin (AGP) to `9.2.1`.
- **Kotlin DSL Migration**:
  - Replaced legacy Groovy build files (`build.gradle`, `settings.gradle`) with Kotlin DSL equivalents (`build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`).
- **Version Catalog Adoption**:
  - Introduced `gradle/libs.versions.toml` for centralized plugin and dependency version management.

### 4.2 Dependencies & Framework Adaptation
- **AndroidX Migration**:
  - Replaced deprecated Android Support dependencies with `androidx.appcompat:appcompat:1.6.1`.
- **Namespace Configuration**:
  - Explicitly defined `namespace = "com.example.scan_module_add_key_demo"` in accordance with AGP requirements.
- **JDK 17 Compatibility**:
  - Configured `JavaVersion.VERSION_17` for both Java compile and target compatibility.

### 4.3 Native Libraries & Device Communication
- **JNI Native Library Support**:
  - Configured `sourceSets { getByName("main") { jniLibs.directories.add("libs") } }` to ensure prebuilt `.so` files are properly packaged into the APK.
- **USB CDC Device Communication**:
  - Preserved Android `UsbDevice` interface logic for interacting with USB CDC serial barcode scanners.

---

## 5. Build & Verification

### Build APK
Run the following command in the project root directory to assemble the Debug APK:

```bash
# Windows PowerShell / CMD
.\gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

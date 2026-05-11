# uav_defense_android_app

Java + Android 14（API 34）项目基础模板，已包含 Codespaces 云端编译配置。

## 环境要求

- JDK 17
- Android SDK Platform 34
- Android Build-Tools 34.0.0

## 本地构建 APK

1. 安装 Android SDK 并确保包含 `platforms;android-34` 和 `build-tools;34.0.0`
2. 在项目根目录执行：

```bash
./gradlew :app:assembleDebug
```

生成的 APK 位于：

`/home/runner/work/uav_defense_android_app/uav_defense_android_app/app/build/outputs/apk/debug/app-debug.apk`

## Codespaces 云端编译

仓库已提供 `.devcontainer/devcontainer.json`，打开 Codespaces 后会自动执行 Android SDK 安装脚本。

在 Codespaces 终端执行：

```bash
./gradlew :app:assembleDebug
```

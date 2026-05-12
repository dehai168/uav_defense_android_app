# uav_defense_android_app

Java + Android 12（API 31）项目基础模板，已包含 Codespaces 云端编译配置。

## 环境要求

- JDK 17
- Android SDK Platform 31
- Android Build-Tools 31.0.0

## 高德地图 API Key 配置

本应用使用高德在线地图 Android SDK（3D 地图），地图功能需要配置有效的 API Key：

1. 前往 [高德开放平台](https://lbs.amap.com/) 注册并创建 Android 应用，获取 API Key。
2. 打开 `app/src/main/res/values/strings.xml`，将占位符替换为您的 Key：

```xml
<string name="amap_key">您的高德地图API_KEY</string>
```

> 未配置有效 Key 时，地图区域将显示空白或显示"鉴权失败"提示，其他功能不受影响。

## 本地构建 APK

1. 安装 Android SDK 并确保包含 `platforms;android-31` 和 `build-tools;31.0.0`
2. 将固定签名文件放到仓库路径 `app/signing/release.jks`，并保持该 JKS 长期不变（不要在构建时重新生成）
3. 在项目根目录执行：

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

> 执行 release 构建（例如 `:app:assembleRelease`）时，如果 `app/signing/release.jks` 缺失，构建会直接失败并提示补齐固定 JKS 文件。

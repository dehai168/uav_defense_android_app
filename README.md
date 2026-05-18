# ai_native_voice_translation_app

本仓库已调整为 **AI Native Voice Translation App** 的 Android MVP 工程骨架，目标是提供一个可在 **GitHub Codespaces** 中完成编译的本地离线语音互翻译项目基础。

## 项目目标

- 本地离线
- 开源
- 安卓端
- 实时语音互翻译

## MVP 功能定义（第一阶段）

用户预先设定：

- 本地语言
- 目标语言

示例：中文 ↔ 英文

交互流程：

```text
按住说话
↓
录音
↓
松开
↓
语音转文字（ASR）
↓
机器翻译（MT）
↓
文字转语音（TTS）
↓
自动播放翻译结果
```

## 当前工程内置内容

- Jetpack Compose UI MVP 页面与语言配置
- `AudioRecord` 录音骨架（16000Hz / 单声道 / PCM 16bit）
- whisper.cpp / HY-MT / llama.cpp 的 Kotlin 占位实现接口
- Android `TextToSpeech` 自动播报封装
- `cpp/whisper`、`cpp/llama`、`cpp/hy_mt`、`cpp/jni` 目录预留
- `docs/mvp_requirements.md` 中保留需求框架文档

> 当前仓库提供的是 **可编译的 MVP 技术框架**。真实离线 ASR / MT 引擎与模型文件（如 whisper-small、HY-MT GGUF INT4）需在后续阶段接入。

## 推荐整体架构

```text
Android UI (Compose)
        ↓
Audio Layer (AudioRecord/Oboe)(采样率：16000\声道：单声道\PCM：16bit)
        ↓
ASR Engine (whisper.cpp)(whisper-small)
        ↓
Translation Engine (HY-MT + llama.cpp)(HY-MT1.5-1.8B GGUF INT4)
        ↓
TTS Engine (Android TextToSpeech)
        ↓
Audio Playback
```

## 线程架构

```text
UI线程
    ↓
录音线程
    ↓
ASR线程
    ↓
翻译线程
    ↓
TTS线程
```

## 目录结构

```text
app/
    src/main/java/com/ai/nativevoicetranslation/
        ui/
        audio/
        asr/
        translator/
        tts/
        nativebridge/
        viewmodel/
        model/
cpp/
    whisper/
    llama/
    hy_mt/
    jni/
docs/
    mvp_requirements.md
```

## 本地构建

要求：

- JDK 17
- Android SDK Platform 34
- Android Build-Tools 34.0.0

在仓库根目录执行：

```bash
./gradlew :app:assembleDebug
```

## GitHub Codespaces

仓库提供了 `.devcontainer/devcontainer.json` 与 Android SDK 安装脚本。Codespaces 创建完成后，可直接执行：

```bash
./gradlew :app:assembleDebug
```

安装脚本会准备：

- platform-tools
- platforms;android-34
- build-tools;34.0.0
- cmake;3.22.1
- ndk;26.3.11579264

## 已知限制

- 当前无法在此任务环境中直接创建新的 GitHub 私有仓库或修改仓库可见性。
- 当前实现以 **项目骨架 / 技术框架 / 可编译 MVP** 为目标，不包含真实模型文件。

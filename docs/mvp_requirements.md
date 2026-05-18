# 本地离线语音互翻译 Android App 技术框架（MVP 方案）

## 项目目标

- 本地离线
- 开源
- 安卓端
- 实时语音互翻译

## MVP 功能定义（第一阶段）

- 用户预先设定本地语言与目标语言
- 交互流程：按住说话 → 录音 → 松开 → 语音转文字（ASR）→ 机器翻译（MT）→ 文字转语音（TTS）→ 自动播放翻译结果

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

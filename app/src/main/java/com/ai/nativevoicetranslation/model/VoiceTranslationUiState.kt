package com.ai.nativevoicetranslation.model

data class VoiceTranslationUiState(
    val sourceLanguage: LanguageOption = LanguageOptions.Chinese,
    val targetLanguage: LanguageOption = LanguageOptions.English,
    val stage: PipelineStage = PipelineStage.Idle,
    val sourceText: String = "",
    val translatedText: String = "",
    val statusMessage: String = "按住说话，松开后自动执行 ASR → MT → TTS。",
    val nativeRuntimeSummary: String = "JNI 占位桥接已就绪，后续可接入 whisper.cpp / llama.cpp / HY-MT。",
    val architectureNotes: List<String> = listOf(
        "UI: Jetpack Compose",
        "Audio: AudioRecord 16kHz / mono / PCM 16bit",
        "ASR: whisper.cpp（当前为占位实现）",
        "MT: HY-MT + llama.cpp（当前为占位实现）",
        "TTS: Android TextToSpeech",
        "Native: C++ / JNI / CMake 目录已预留"
    ),
    val errorMessage: String? = null,
    val isPermissionGranted: Boolean = false,
    val isBusy: Boolean = false
)

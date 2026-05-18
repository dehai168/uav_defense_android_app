package com.ai.nativevoicetranslation.asr

import com.ai.nativevoicetranslation.model.AudioCapture
import com.ai.nativevoicetranslation.model.LanguageOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class WhisperAsrEngine : AsrEngine {
    override suspend fun transcribe(audioCapture: AudioCapture, language: LanguageOption): String = withContext(Dispatchers.Default) {
        if (audioCapture.pcmData.isEmpty()) {
            return@withContext ""
        }
        val durationMs = max(1, audioCapture.pcmData.size / 32)
        when (language.code) {
            "zh" -> "你好，这是离线语音翻译 MVP 的示例转写（约 ${durationMs}ms 音频）"
            "es" -> "Hola, esta es una transcripción de demostración sin conexión (${durationMs} ms)"
            else -> "Hello, this is an offline voice translation MVP transcript (${durationMs} ms audio)"
        }
    }
}

package com.ai.nativevoicetranslation.asr

import com.ai.nativevoicetranslation.model.AudioCapture
import com.ai.nativevoicetranslation.model.LanguageOption

interface AsrEngine {
    suspend fun transcribe(audioCapture: AudioCapture, language: LanguageOption): String
}

package com.ai.nativevoicetranslation.tts

import com.ai.nativevoicetranslation.model.LanguageOption

interface SpeechSynthesizer {
    suspend fun speak(text: String, language: LanguageOption)
    fun shutdown()
}

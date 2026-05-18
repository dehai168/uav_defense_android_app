package com.ai.nativevoicetranslation.translator

import com.ai.nativevoicetranslation.model.LanguageOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HyMtTranslator : Translator {
    override suspend fun translate(
        text: String,
        sourceLanguage: LanguageOption,
        targetLanguage: LanguageOption
    ): String = withContext(Dispatchers.Default) {
        if (text.isBlank()) {
            return@withContext ""
        }
        val normalized = text.trim()
        when {
            sourceLanguage.code == "zh" && targetLanguage.code == "en" && normalized.contains("你好") -> {
                "Hello, this is the MVP offline translation result."
            }
            sourceLanguage.code == "en" && targetLanguage.code == "zh" && normalized.contains("Hello", ignoreCase = true) -> {
                "你好，这是 MVP 离线翻译结果。"
            }
            sourceLanguage.code == "zh" && targetLanguage.code == "es" -> {
                "Hola, este es el resultado de traducción offline del MVP."
            }
            else -> {
                "[${sourceLanguage.label} → ${targetLanguage.label}] $normalized"
            }
        }
    }
}

package com.ai.nativevoicetranslation.translator

import com.ai.nativevoicetranslation.model.LanguageOption

interface Translator {
    suspend fun translate(text: String, sourceLanguage: LanguageOption, targetLanguage: LanguageOption): String
}

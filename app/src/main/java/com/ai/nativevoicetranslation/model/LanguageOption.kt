package com.ai.nativevoicetranslation.model

import java.util.Locale

data class LanguageOption(
    val code: String,
    val label: String,
    val locale: Locale
)

object LanguageOptions {
    val Chinese = LanguageOption("zh", "中文", Locale.SIMPLIFIED_CHINESE)
    val English = LanguageOption("en", "English", Locale.ENGLISH)
    val Spanish = LanguageOption("es", "Español", Locale("es", "ES"))

    val supported = listOf(Chinese, English, Spanish)
}

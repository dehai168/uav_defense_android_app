package com.ai.nativevoicetranslation.model

data class AudioCapture(
    val pcmData: ByteArray,
    val sampleRate: Int = 16_000,
    val channelCount: Int = 1,
    val bitsPerSample: Int = 16
)

package com.ai.nativevoicetranslation.model

enum class PipelineStage {
    Idle,
    Recording,
    Transcribing,
    Translating,
    Speaking,
    Error
}

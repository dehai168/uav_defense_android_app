package com.ai.nativevoicetranslation.nativebridge

object AiNativeRuntimeBridge {
    private val nativeLoaded: Boolean = runCatching {
        System.loadLibrary("voice_translation_runtime")
        true
    }.getOrDefault(false)

    external fun nativeDescribeRuntime(): String

    fun describeRuntime(): String {
        if (!nativeLoaded) {
            return "voice_translation_runtime 未加载；当前使用 Kotlin 占位实现，可在 cpp/ 目录接入真实引擎。"
        }
        return runCatching { nativeDescribeRuntime() }
            .getOrDefault("JNI 已加载，但 nativeDescribeRuntime 暂未返回有效描述。")
    }
}

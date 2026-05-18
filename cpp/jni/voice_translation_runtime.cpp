#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ai_nativevoicetranslation_nativebridge_AiNativeRuntimeBridge_nativeDescribeRuntime(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF(
            "JNI runtime bridge is available. Plug whisper.cpp, llama.cpp and HY-MT sources into cpp/."
    );
}

cmake_minimum_required(VERSION 3.22.1)
project(voice_translation_runtime)

add_library(
    voice_translation_runtime
    SHARED
    jni/voice_translation_runtime.cpp
)

find_library(log-lib log)
target_link_libraries(voice_translation_runtime ${log-lib})

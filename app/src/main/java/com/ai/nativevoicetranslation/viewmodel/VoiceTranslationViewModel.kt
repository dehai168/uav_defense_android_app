package com.ai.nativevoicetranslation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.nativevoicetranslation.asr.WhisperAsrEngine
import com.ai.nativevoicetranslation.audio.PcmAudioRecorder
import com.ai.nativevoicetranslation.model.LanguageOption
import com.ai.nativevoicetranslation.model.LanguageOptions
import com.ai.nativevoicetranslation.model.PipelineStage
import com.ai.nativevoicetranslation.model.VoiceTranslationUiState
import com.ai.nativevoicetranslation.nativebridge.AiNativeRuntimeBridge
import com.ai.nativevoicetranslation.translator.HyMtTranslator
import com.ai.nativevoicetranslation.tts.AndroidTtsSpeaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceTranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val recorder = PcmAudioRecorder()
    private val asrEngine = WhisperAsrEngine()
    private val translator = HyMtTranslator()
    private val speaker = AndroidTtsSpeaker(application)

    private val _uiState = MutableStateFlow(
        VoiceTranslationUiState(
            isPermissionGranted = false,
            nativeRuntimeSummary = AiNativeRuntimeBridge.describeRuntime()
        )
    )
    val uiState: StateFlow<VoiceTranslationUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isPermissionGranted = granted,
                statusMessage = if (granted) {
                    "麦克风权限已授权，可开始离线语音互翻译流程。"
                } else {
                    "请先授予麦克风权限，再执行按住说话。"
                },
                errorMessage = if (granted) null else "缺少录音权限。"
            )
        }
    }

    fun updateSourceLanguage(language: LanguageOption) {
        _uiState.update { it.copy(sourceLanguage = language) }
    }

    fun updateTargetLanguage(language: LanguageOption) {
        _uiState.update { it.copy(targetLanguage = language) }
    }

    fun swapLanguages() {
        _uiState.update {
            it.copy(
                sourceLanguage = it.targetLanguage,
                targetLanguage = it.sourceLanguage
            )
        }
    }

    fun startRecording() {
        val state = _uiState.value
        if (!state.isPermissionGranted || state.isBusy) return
        recorder.start().onSuccess {
            _uiState.update {
                it.copy(
                    stage = PipelineStage.Recording,
                    statusMessage = "录音中：采样率 16000 / 单声道 / PCM 16bit",
                    errorMessage = null,
                    isBusy = true
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    stage = PipelineStage.Error,
                    statusMessage = "录音启动失败。",
                    errorMessage = throwable.message,
                    isBusy = false
                )
            }
        }
    }

    fun stopRecordingAndProcess() {
        if (_uiState.value.stage != PipelineStage.Recording) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stage = PipelineStage.Transcribing,
                    statusMessage = "正在执行 whisper.cpp 风格 ASR 占位流程..."
                )
            }
            recorder.stop().onSuccess { audioCapture ->
                val sourceLanguage = _uiState.value.sourceLanguage
                val targetLanguage = _uiState.value.targetLanguage
                val transcript = asrEngine.transcribe(audioCapture, sourceLanguage)
                _uiState.update {
                    it.copy(
                        sourceText = transcript,
                        stage = PipelineStage.Translating,
                        statusMessage = "正在执行 HY-MT + llama.cpp 风格翻译占位流程..."
                    )
                }
                val translated = translator.translate(transcript, sourceLanguage, targetLanguage)
                _uiState.update {
                    it.copy(
                        translatedText = translated,
                        stage = PipelineStage.Speaking,
                        statusMessage = "正在调用 Android TextToSpeech 自动播报翻译结果..."
                    )
                }
                speaker.speak(translated, targetLanguage)
                _uiState.update {
                    it.copy(
                        stage = PipelineStage.Idle,
                        statusMessage = "流程完成，可再次按住说话。",
                        errorMessage = null,
                        isBusy = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        stage = PipelineStage.Error,
                        statusMessage = "处理失败，请重试。",
                        errorMessage = throwable.message,
                        isBusy = false
                    )
                }
            }
        }
    }

    fun resetConversation() {
        _uiState.update {
            it.copy(
                sourceText = "",
                translatedText = "",
                stage = PipelineStage.Idle,
                statusMessage = "已清空本次会话，等待新的语音输入。",
                errorMessage = null,
                isBusy = false
            )
        }
    }

    fun shutdown() {
        speaker.shutdown()
    }

    override fun onCleared() {
        shutdown()
        super.onCleared()
    }

    val supportedLanguages: List<LanguageOption>
        get() = LanguageOptions.supported
}

package com.ai.nativevoicetranslation.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.ai.nativevoicetranslation.model.LanguageOption
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AndroidTtsSpeaker(context: Context) : SpeechSynthesizer {
    private val appContext = context.applicationContext
    private val initResult = CompletableDeferred<Int>()
    private val tts = TextToSpeech(appContext) { status ->
        if (!initResult.isCompleted) {
            initResult.complete(status)
        }
    }

    override suspend fun speak(text: String, language: LanguageOption) {
        if (text.isBlank()) return
        val status = initResult.await()
        if (status != TextToSpeech.SUCCESS) return
        withContext(Dispatchers.Main) {
            tts.language = language.locale
            val utteranceId = UUID.randomUUID().toString()
            val completion = CompletableDeferred<Unit>()
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    completion.complete(Unit)
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    completion.complete(Unit)
                }
            })
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            completion.await()
        }
    }

    override fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

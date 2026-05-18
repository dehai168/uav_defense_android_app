package com.ai.nativevoicetranslation.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.ai.nativevoicetranslation.model.AudioCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PcmAudioRecorder {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputStream: ByteArrayOutputStream? = null

    fun start(): Result<Unit> = runCatching {
        check(audioRecord == null) { "Recorder is already running." }
        val sampleRate = 16_000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        require(minBufferSize > 0) { "Unable to determine AudioRecord buffer size." }

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize * 2
        )
        require(recorder.state == AudioRecord.STATE_INITIALIZED) { "AudioRecord initialization failed." }

        outputStream = ByteArrayOutputStream()
        audioRecord = recorder
        recorder.startRecording()
        recordingJob = scope.launch {
            val buffer = ByteArray(minBufferSize)
            while (isActive) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    outputStream?.write(buffer, 0, read)
                }
            }
        }
    }

    suspend fun stop(): Result<AudioCapture> = runCatching {
        val recorder = audioRecord ?: error("Recorder is not running.")
        withContext(Dispatchers.IO) {
            recordingJob?.cancelAndJoin()
            runCatching { recorder.stop() }
            recorder.release()
            audioRecord = null
            recordingJob = null
            val bytes = outputStream?.toByteArray().orEmpty()
            outputStream?.close()
            outputStream = null
            AudioCapture(pcmData = bytes)
        }
    }
}

package com.limb.diagnostics.engine

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlin.math.sin
import kotlin.math.sqrt

class AudioDiagnosticEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var activeAudioTrack: AudioTrack? = null

    fun playTone(frequencyHz: Double = 440.0, durationMs: Int = 1500, streamType: Int = AudioManager.STREAM_MUSIC) {
        stopTone()
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val dVal = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / frequencyHz))
            // Apply soft ramp-in and ramp-out to avoid pop/clicks
            val envelope = when {
                i < 2000 -> i / 2000.0
                i > numSamples - 2000 -> (numSamples - i) / 2000.0
                else -> 1.0
            }
            generatedSnd[i] = (dVal * 32767 * 0.7 * envelope).toInt().toShort()
        }

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(numSamples * 2)

        val usage = if (streamType == AudioManager.STREAM_VOICE_CALL) {
            AudioAttributes.USAGE_VOICE_COMMUNICATION
        } else {
            AudioAttributes.USAGE_MEDIA
        }

        val contentType = if (streamType == AudioManager.STREAM_VOICE_CALL) {
            AudioAttributes.CONTENT_TYPE_SPEECH
        } else {
            AudioAttributes.CONTENT_TYPE_MUSIC
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(generatedSnd, 0, numSamples)
        track.play()
        activeAudioTrack = track
    }

    fun stopTone() {
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null
    }

    fun triggerVibration(pattern: String = "standard") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.let {
                when (pattern) {
                    "gentle" -> it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    "heavy" -> it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    "pulse" -> it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), -1))
                    else -> it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(150)
                }
            }
        }
    }

    fun observeMicrophoneAmplitude(): Flow<Float> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            close()
            return@callbackFlow
        }

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording()
                val buffer = ShortArray(bufferSize)

                var isRunning = true
                val thread = Thread {
                    while (isRunning && audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val read = audioRecord.read(buffer, 0, bufferSize)
                        if (read > 0) {
                            var sum = 0.0
                            for (i in 0 until read) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = sqrt(sum / read)
                            val normalized = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                            trySend(normalized)
                        }
                        Thread.sleep(50)
                    }
                }
                thread.start()

                awaitClose {
                    isRunning = false
                    try {
                        audioRecord.stop()
                        audioRecord.release()
                    } catch (_: Exception) {}
                }
            } else {
                close()
            }
        } catch (e: Exception) {
            close(e)
        }
    }

    fun checkAudioHardware(): List<DiagnosticTest> {
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Vibration hardware check
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        val hasVibrator = vibrator?.hasVibrator() ?: false

        tests.add(
            DiagnosticTest(
                id = "audio_vibration_hw",
                category = TestCategory.AUDIO,
                name = "Vibration Motor",
                description = "Tactile haptic linear actuator / ERM motor",
                requiresInteraction = false,
                status = if (hasVibrator) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasVibrator) "Haptic motor detected and operational." else "No vibration hardware detected.",
                metrics = listOf(
                    DiagnosticMetric("Haptics", if (hasVibrator) "Available" else "Not Present", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasVibrator
            )
        )

        // 2. Headphone / Audio Output Detection
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val connectedTypes = devices.map { device ->
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset (3.5mm)"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                AudioDeviceInfo.TYPE_USB_HEADSET -> "USB-C Audio"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Audio"
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Loudspeaker"
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
                else -> "Audio Sink"
            }
        }.distinct()

        tests.add(
            DiagnosticTest(
                id = "audio_headphone_detection",
                category = TestCategory.AUDIO,
                name = "Headphone & Audio Output",
                description = "Detects connected audio endpoints and jacks",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Active audio endpoints: ${connectedTypes.joinToString(", ")}.",
                metrics = listOf(
                    DiagnosticMetric("Endpoints", "${connectedTypes.size} detected", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }
}

package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import com.example.data.AlertSoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

object BuzzerManager {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var activeTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays either the sensual voice ("Batterie déchargée") or the classic beep according to settings.
     */
    fun playAlert(
        context: Context,
        alertType: AlertSoundType = AlertSoundType.SENSUAL_VOICE,
        durationMs: Int = 800,
        frequencyHz: Int = 880,
        vibrate: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        stop()

        when (alertType) {
            AlertSoundType.SENSUAL_VOICE -> {
                playSensualVoice(context, vibrate, onComplete)
            }
            AlertSoundType.BEEP -> {
                playLongBeep(context, durationMs, frequencyHz, vibrate, onComplete)
            }
        }
    }

    /**
     * Plays the sensual female voice saying "Batterie déchargée"
     */
    fun playSensualVoice(
        context: Context,
        vibrate: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        stop()

        playJob = scope.launch(Dispatchers.Main) {
            try {
                _isPlaying.value = true

                if (vibrate) {
                    triggerSensualVibration(context)
                }

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    val afd = context.resources.openRawResourceFd(R.raw.voice_batterie_dechargee)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    prepare()
                }
                mediaPlayer = player

                player.setOnCompletionListener {
                    stop()
                    onComplete?.invoke()
                }
                player.setOnErrorListener { _, _, _ ->
                    stop()
                    onComplete?.invoke()
                    true
                }

                player.start()
            } catch (e: Exception) {
                e.printStackTrace()
                stop()
                onComplete?.invoke()
            }
        }
    }

    /**
     * Plays a beep sound according to user configuration.
     */
    fun playLongBeep(
        context: Context,
        durationMs: Int = 800,
        frequencyHz: Int = 880,
        vibrate: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        stop()

        playJob = scope.launch {
            try {
                _isPlaying.value = true

                if (vibrate) {
                    triggerVibration(context, durationMs)
                }

                playToneTrack(
                    durationMs = durationMs,
                    frequencyHz = frequencyHz
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPlaying.value = false
                onComplete?.invoke()
            }
        }
    }

    private fun playToneTrack(durationMs: Int, frequencyHz: Int) {
        val sampleRate = 44100
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        val rampSamples = (0.05 * sampleRate).toInt().coerceAtMost(numSamples / 4)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val angle = 2.0 * Math.PI * frequencyHz.toDouble() * t
            var sample = sin(angle)

            if (i < rampSamples) {
                val envelope = i.toDouble() / rampSamples
                sample *= envelope
            } else if (i > numSamples - rampSamples) {
                val envelope = (numSamples - i).toDouble() / rampSamples
                sample *= envelope
            }

            val scaledSample = (sample * Short.MAX_VALUE * 0.95).toInt()
            buffer[i] = scaledSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val bufferSize = buffer.size * 2

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        activeTrack = track
        track.write(buffer, 0, buffer.size)
        track.play()

        val waitTimeMs = (durationMs + 100).toLong()
        val step = 100L
        var elapsed = 0L
        while (elapsed < waitTimeMs && _isPlaying.value) {
            Thread.sleep(step)
            elapsed += step
        }

        try {
            track.stop()
            track.release()
        } catch (_: Exception) {}
        activeTrack = null
    }

    private fun triggerSensualVibration(context: Context) {
        try {
            val pattern = longArrayOf(0, 180, 120, 260)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator?.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(300)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerVibration(context: Context, durationMs: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = longArrayOf(0, 300, 150, (durationMs - 450).coerceAtLeast(200).toLong())
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 300, 150, (durationMs - 450).coerceAtLeast(200).toLong())
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs.toLong())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            activeTrack?.stop()
            activeTrack?.release()
        } catch (_: Exception) {}
        activeTrack = null
        _isPlaying.value = false
    }
}

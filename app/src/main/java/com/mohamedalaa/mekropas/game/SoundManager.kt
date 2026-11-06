package com.mohamedalaa.mekropas.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.mohamedalaa.mekropas.R

/**
 * High-performance audio manager using SoundPool for fast low-latency gameplay SFX
 * and MediaPlayer for looping Menofia background music.
 */
class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    private var soundHornId: Int = 0
    private var soundCrashId: Int = 0
    private var soundCoinId: Int = 0
    private var soundPassengerStopId: Int = 0

    private var isLoaded: Boolean = false
    var isMuted: Boolean = false

    init {
        initAudio()
    }

    private fun initAudio() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                }
            }

            // Load SFX from raw resources
            soundHornId = soundPool?.load(context, R.raw.horn, 1) ?: 0
            soundCrashId = soundPool?.load(context, R.raw.crash, 1) ?: 0
            soundCoinId = soundPool?.load(context, R.raw.coin, 1) ?: 0
            soundPassengerStopId = soundPool?.load(context, R.raw.passenger_stop, 1) ?: 0

            // Background Music
            mediaPlayer = MediaPlayer.create(context, R.raw.bg_music)?.apply {
                isLooping = true
                setVolume(0.35f, 0.35f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundManager", e)
        }
    }

    fun playHorn() {
        if (!isMuted && soundHornId != 0) {
            soundPool?.play(soundHornId, 0.9f, 0.9f, 1, 0, 1.0f)
        }
    }

    fun playCrash() {
        if (!isMuted && soundCrashId != 0) {
            soundPool?.play(soundCrashId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playCoin() {
        if (!isMuted && soundCoinId != 0) {
            soundPool?.play(soundCoinId, 0.85f, 0.85f, 1, 0, 1.0f)
        }
    }

    fun playPassengerStop() {
        if (!isMuted && soundPassengerStopId != 0) {
            soundPool?.play(soundPassengerStopId, 0.95f, 0.95f, 1, 0, 1.0f)
        }
    }

    fun startMusic() {
        if (!isMuted && mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting background music", e)
            }
        }
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing background music", e)
            }
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundManager", e)
        }
    }

    companion object {
        private const val TAG = "SoundManager"
    }
}

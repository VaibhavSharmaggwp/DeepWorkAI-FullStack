package com.example.deepworkai.utils

import android.content.Context
import android.media.MediaPlayer

class AmbientAudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying = false
        private set
    var currentSoundResId: Int? = null
        private set
    var volume = 0.5f // 0.0 to 1.0
        set(value) {
            field = value
            mediaPlayer?.setVolume(value, value)
        }

    fun playSound(soundResId: Int) {
        if (currentSoundResId == soundResId && isPlaying) {
            return // Already playing this sound
        }

        mediaPlayer?.release()
        
        mediaPlayer = MediaPlayer.create(context, soundResId).apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
        
        currentSoundResId = soundResId
        isPlaying = true
    }

    fun pause() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    fun resume() {
        if (!isPlaying && currentSoundResId != null) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stopAndRelease() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        currentSoundResId = null
    }
}

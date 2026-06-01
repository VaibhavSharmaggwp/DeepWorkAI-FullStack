package com.example.deepworkai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.deepworkai.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    private var isRunning = false
    private var isPaused = false
    private var seconds = 0
    private var maxSeconds = 3600
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_MAX_SECONDS = "EXTRA_MAX_SECONDS"

        private const val NOTIFICATION_CHANNEL_ID = "focus_timer_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Yeh function tab call hota hai jab hum service ko start, pause, ya stop karne ka intent bhejte hain.
        // Intent ke action ke base par yeh decide karta hai ki konsa function call karna hai.
        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    maxSeconds = it.getIntExtra(EXTRA_MAX_SECONDS, 3600)
                    startTimer()
                }
                ACTION_PAUSE -> {
                    pauseTimer()
                }
                ACTION_RESUME -> {
                    resumeTimer()
                }
                ACTION_STOP -> {
                    stopTimer()
                }
            }
        }
        return START_NOT_STICKY // Iska matlab hai agar system service kill kar de, toh use apne aap wapas start mat karna.
    }

    private fun startTimer() {
        // Yeh function timer aur foreground notification start karta hai.
        if (isRunning) return
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification()) // Notification dikhaane ke liye
        isRunning = true
        isPaused = false
        
        timerJob = serviceScope.launch {
            // Yeh loop continuously chalta hai jab tak session chal raha hai
            while (isRunning && seconds < maxSeconds) {
                if (!isPaused) {
                    delay(1000) // 1 second ka delay
                    seconds++
                    updateNotification() // Notification me time update karta hai
                    FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning) // UI ke liye StateFlow update karta hai
                } else {
                    delay(500) // Agar pause hai toh sirf wait karta hai taaki CPU waste na ho
                }
            }
            if (seconds >= maxSeconds) {
                // Agar session time khatam ho gaya
                isRunning = false
                isPaused = true
                FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
                stopForeground(true) // Notification hata deta hai
                stopSelf() // Service bandh kar deta hai
            }
        }
    }

    private fun pauseTimer() {
        // Session ko pause karta hai. State update hoti hai jisse timer ruk jata hai.
        isPaused = true
        FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
        updateNotification()
    }

    private fun resumeTimer() {
        // Paused session ko wapas resume karta hai.
        isPaused = false
        FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
        updateNotification()
    }

    private fun stopTimer() {
        // Timer ko permanently stop karta hai aur resources/service ko release kar deta hai.
        isRunning = false
        timerJob?.cancel() // Coroutine ko stop karta hai
        seconds = 0
        FocusTimerManager.updateTimerState(0, maxSeconds, false, false)
        stopForeground(true)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }
        
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val timeStr = String.format("%02d:%02d", minutes, remainingSeconds)

        val statusText = if (isPaused) "Paused - $timeStr" else "Focusing - $timeStr"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("DeepWork AI Session Active")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play) // Replace with app icon later if needed
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Focus Timer Channel",
                NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't ring/vibrate every second
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}

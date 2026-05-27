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
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (isRunning) return
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        isRunning = true
        isPaused = false
        
        timerJob = serviceScope.launch {
            while (isRunning && seconds < maxSeconds) {
                if (!isPaused) {
                    delay(1000)
                    seconds++
                    updateNotification()
                    FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
                } else {
                    delay(500)
                }
            }
            if (seconds >= maxSeconds) {
                // Timer finished natively
                isRunning = false
                isPaused = true
                FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun pauseTimer() {
        isPaused = true
        FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
        updateNotification()
    }

    private fun resumeTimer() {
        isPaused = false
        FocusTimerManager.updateTimerState(seconds, maxSeconds, isPaused, isRunning)
        updateNotification()
    }

    private fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
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

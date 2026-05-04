package com.example.service

import com.example.db.DatabaseFactory.dbQuery
import com.example.models.Users
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationService(private val scope: CoroutineScope) {
    
    fun start() {
        scope.launch {
            while (isActive) {
                checkAndSendNotifications()
                delay(60000) // Check every minute
            }
        }
    }

    private suspend fun checkAndSendNotifications() = dbQuery {
        val now = LocalTime.now()
        val currentTimeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"))
        
        val usersToNotify = Users.selectAll().filter { 
            it[Users.notificationsEnabled] && it[Users.notificationTime] == currentTimeStr
        }

        for (user in usersToNotify) {
            val email = user[Users.email]
            val type = user[Users.notificationType]
            
            if (type == "email") {
                sendEmail(email)
            } else {
                sendPushNotification(email)
            }
        }
    }

    private fun sendEmail(email: String) {
        println("""
            
            [EMAIL SERVICE] Sending Daily Reminder to: $email
            Subject: Time to Focus! 🚀
            Body: Hello! This is your daily reminder from DeepWork AI to start your focus session.
            Keep up the great work!
            
        """.trimIndent())
    }

    private fun sendPushNotification(email: String) {
        println("[PUSH SERVICE] Triggering App Notification for: $email")
        // In a real app, you'd use FCM (Firebase Cloud Messaging) here
    }
}

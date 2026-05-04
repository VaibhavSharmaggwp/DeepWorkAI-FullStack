package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val googleId: String? = null,
    val isVerified: Boolean = false,
    val imageUrl: String? = null,
    val focusScore: Int = 0,
    val darkMode: Boolean = true,
    val behavioralTracking: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationType: String = "notification",
    val notificationTime: String = "09:00"
)
package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val imageUrl: String? = null,
    val focusScore: Int = 0,
    val darkMode: Boolean = true,
    val behavioralTracking: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationType: String = "notification",
    val notificationTime: String = "09:00"
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String
)
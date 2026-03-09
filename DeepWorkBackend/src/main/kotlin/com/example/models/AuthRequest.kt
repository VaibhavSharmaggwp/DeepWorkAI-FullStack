package com.example.models
import kotlinx.serialization.Serializable

@Serializable

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String
)
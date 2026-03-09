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
    val fullName: String
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String
)
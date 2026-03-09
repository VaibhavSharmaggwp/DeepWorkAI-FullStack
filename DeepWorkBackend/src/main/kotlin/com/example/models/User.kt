package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val googleId: String? = null,
    val isVerified: Boolean = false
)
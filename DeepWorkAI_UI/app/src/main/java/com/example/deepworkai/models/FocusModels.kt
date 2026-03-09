package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class FocusSession(
    val id: String,
    val userId: String,
    val startTime: String,
    val endTime: String? = null,
    val focusScore: Int = 0,
    val distractions: Int = 0,
    val cognitiveLoad: String = "Low"
)

@Serializable
data class StartSessionRequest(val userId: String)

@Serializable
data class EndSessionRequest(
    val sessionId: String,
    val distractions: Int
)

@Serializable
data class EndSessionResponse(
    val session: FocusSession, // this is database now
    val burnoutRisk: String // // The AI prediction ("Low", "Medium", "High")
)
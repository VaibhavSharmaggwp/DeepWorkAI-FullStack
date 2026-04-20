package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class SaveSessionRequest(
    val userId: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val distractions: Int,
    val stabilityScore: Int,
    val avgDeepBlock: Int,
    val cognitiveLoad: String
)

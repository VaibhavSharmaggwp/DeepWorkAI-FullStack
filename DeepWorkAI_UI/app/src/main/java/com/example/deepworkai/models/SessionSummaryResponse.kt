package com.example.deepworkai.models

data class SessionSummaryResponse(
    val stabilityScore: Int,
    val durationMin: Int,
    val distractions: Int,
    val distractionLevel: String, // e.g., "Low"
    val avgDeepBlock: Int,
    val cognitiveLoad: String
)
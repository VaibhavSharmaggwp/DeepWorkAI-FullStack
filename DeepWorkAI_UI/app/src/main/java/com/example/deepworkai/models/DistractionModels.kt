package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class DistractionApp(
    val appName: String,
    val usageTime: Int
)

@Serializable
data class SessionDistractions(
    val sessionTitle: String,
    val date: String,
    val startTime: String,
    val apps: List<DistractionApp>
)

@Serializable
data class DistractionInsightsResponse(
    val sessions: List<SessionDistractions>,
    val recommendation: String
)

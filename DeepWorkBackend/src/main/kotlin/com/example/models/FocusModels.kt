package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class FocusSession(
    val id: String,
    val userId: String,
    val startTime: String,
    val endTime:String?= null,
    val focusScore:Int = 0,
    val distractions:Int = 0,
    val cognitiveLoad: String = "Low"
)


@Serializable
data class StartSessionRequest(val userId: String)


@Serializable
data class EndSessionRequest(
    val sessionId: String,
    val distractions: Int,
    val distractedApps: List<DistractionApp>? = null
)

@Serializable
data class EndSessionResponse(
    val session: FocusSession,
    val burnoutRisk: String
)

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
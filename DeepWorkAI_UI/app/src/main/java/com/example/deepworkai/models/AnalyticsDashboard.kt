package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsDashboard(
    val weeklyScores: List<Int>,
    val weeklyDeepMinutes: List<Int> = emptyList(),
    val totalDeepMinutes: Int,
    val contextSwitches: Int,
    val heatmap: List<Int>,
    val todayScore: Int,
    val trend: String,
    val cognitivePeakInsight: String = "Your brain enters flow state fastest between 9:00 AM and 11:30 AM.",
    val consistencyInsight: String = "Focus consistency improved by 21% compared to last week.",
    val switchesInsight: String = "You switched apps 42 times during your last session."
)
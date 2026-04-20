package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyAnalytics(
    val focusScore: Int,
    val scoreChange: String, // eg 12%
    val dailyScores: List<Int>,
    val deepWorkHours: Double,
    val distractionHeatmap: List<Int>,
    val cognitivePeak: String,
    val consistencyGain: String,
    val contextSwitches: Int
)
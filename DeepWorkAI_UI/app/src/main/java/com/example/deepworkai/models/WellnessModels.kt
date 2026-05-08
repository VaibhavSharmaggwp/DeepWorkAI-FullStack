package com.example.deepworkai.models

import kotlinx.serialization.Serializable

@Serializable
data class WellnessLog(
    val userId: String,
    val date: String,
    val sleepHours: Int,
    val hydrationLevel: Int,
    val meditated: Boolean,
    val exercise: Boolean
)

@Serializable
data class UpdateWellnessRequest(
    val sleepHours: Int? = null,
    val hydrationLevel: Int? = null,
    val meditated: Boolean? = null,
    val exercise: Boolean? = null
)

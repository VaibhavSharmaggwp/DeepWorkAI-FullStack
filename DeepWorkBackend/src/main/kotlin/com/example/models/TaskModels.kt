package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val category: String, // Deep or Shallow
    val status: String,   // Pending, In-Progress, Completed
    val estimatedMinutes: Int,
    val actualMinutes: Int,
    val createdAt: String,
    val completedAt: String? = null
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val estimatedMinutes: Int = 30
)

@Serializable
data class UpdateTaskStatusRequest(
    val status: String
)

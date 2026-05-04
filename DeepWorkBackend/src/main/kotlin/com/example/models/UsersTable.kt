package com.example.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val fullName = varchar("full_name", 255)
    val googleId = varchar("google_id", 255).nullable().uniqueIndex()
    val isVerified = bool("is_verified").default(false)
    val imageUrl = text("image_url").nullable()
    val focusScore = integer("focus_score").default(0)
    val darkMode = bool("dark_mode").default(true)
    val behavioralTracking = bool("behavioral_tracking").default(false)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val notificationType = varchar("notification_type", 20).default("notification")
    val notificationTime = varchar("notification_time", 10).default("09:00")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
package com.example.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime


object DailyAnalyticsTable: Table("daily_analytics") {
    val userId = uuid("user_id")
    val statDate = date("stat_date")
    val avgFocusScore = integer("avg_focus_score")
    val totalDeepMinutes = integer("total_deep_minutes")
    val contextSwitches = integer("context_switches")
    val distractionHeatmap = text("distraction_heatmap")

    override val primaryKey = PrimaryKey(userId, statDate)

}

object FocusSessionsTable : Table("focus_sessions") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val startTime = datetime("start_time")
    val endTime = datetime("end_time").nullable()
    val focusScore = integer("focus_score").default(0)
    val durationMinutes = integer("duration_minutes").default(0)
    val distractions = integer("distractions").default(0)
    val sessionNumber = integer("session_number").default(1)
    val burnoutRisk = varchar("burnout_risk", 50).nullable()
    val cognitiveLoad = varchar("cognitive_load", 50).nullable()
    val taskId = uuid("task_id").nullable()
    val sessionName = varchar("session_name", 255).nullable()
    val tags = varchar("tags", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

object SessionHistoryTable: Table("session_history"){
    val id = uuid("id")
    val userId = uuid("user_id")
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val durationMinutes = integer("duration_minutes")
    val distractionsCount = integer("distractions_count")
    val focusStability = integer("focus_stability")
    val avgDeepBlock = integer("avg_deep_block")
    val cognitiveLoadStatus = varchar("cognitive_load_status", 20)
    val taskId = uuid("task_id").nullable()

    override val primaryKey = PrimaryKey(id)
}

object TasksTable : Table("tasks") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val category = varchar("category", 50).default("Shallow") // Deep or Shallow
    val status = varchar("status", 50).default("Pending") // Pending, In-Progress, Completed
    val estimatedMinutes = integer("estimated_minutes").default(30)
    val actualMinutes = integer("actual_minutes").default(0)
    val createdAt = datetime("created_at")
    val completedAt = datetime("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object WellnessTable : Table("wellness_logs") {
    val userId = uuid("user_id")
    val logDate = date("log_date")
    val sleepHours = integer("sleep_hours").default(0)
    val hydrationLevel = integer("hydration_level").default(0)
    val meditated = bool("meditated").default(false)
    val exercise = bool("exercise").default(false)

    override val primaryKey = PrimaryKey(userId, logDate)
}

object DistractionLogsTable: Table("distraction_logs") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 100)
    val appName = varchar("app_name", 100)
    val usageTime = integer("usage_time")
    val sessionId = varchar("session_id", 100).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object CognitiveChallengesTable : Table("cognitive_challenges") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val level = integer("level")
    val score = integer("score")
    val playedAt = datetime("played_at")

    override val primaryKey = PrimaryKey(id)
}
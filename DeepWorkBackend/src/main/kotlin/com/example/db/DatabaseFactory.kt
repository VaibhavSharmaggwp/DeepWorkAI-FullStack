package com.example.db

import com.example.models.FocusSession
import com.example.models.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.*

object DatabaseFactory{
    fun init(){
        // These credentials connect Ktor to the PostgreSQL you installed
        val driverClassName = "org.postgresql.Driver"
        val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5433/deepwork_db"
        val user = System.getenv("DATABASE_USER") ?: "postgres"
        val password = System.getenv("DATABASE_PASSWORD") ?: ""
        
        println("DatabaseFactory: Using JDBC URL: $jdbcUrl")
        
        println("DatabaseFactory: Connecting to $jdbcUrl as $user")
        
        try {
            val database = Database.connect(jdbcUrl, driverClassName, user, password)

            transaction(database){
                SchemaUtils.create(Users, FocusSessionsTable, DailyAnalyticsTable, SessionHistoryTable, DistractionLogsTable)
                println("DatabaseFactory: Tables 'users', 'focus_sessions', 'daily_analytics', 'session_history', 'distraction_logs' verified/created")
            }
            println("DatabaseFactory: Connection successful")
        } catch (e: Exception) {
            println("DatabaseFactory: CONNECTION FAILED: ${e.message}")
            e.printStackTrace()
        }
    }

    // This helper makes sure database operations don't "freeze" your server
    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    suspend fun startFocusSession(userId: UUID): FocusSession? = dbQuery {
        val sessionId = UUID.randomUUID()
        val startTime = LocalDateTime.now()

        val result = FocusSessionsTable.insert {
            it[id] = sessionId
            it[FocusSessionsTable.userId] = userId
            it[FocusSessionsTable.startTime] = startTime
        }

        if (result.insertedCount > 0) {
            FocusSession(
                id = sessionId.toString(),
                userId = userId.toString(),
                startTime = startTime.toString()
            )
        } else null
    }

    suspend fun endFocusSession(sessionId: String, distractions: Int, targetDurationMinutes: Int = 25): FocusSession? = dbQuery {
        val sId = UUID.fromString(sessionId)
        val endTime = LocalDateTime.now()

        // 1. Get the session to calculate score
        val existingSession = FocusSessionsTable.select { FocusSessionsTable.id eq sId }.singleOrNull()

        if (existingSession != null) {
            val startTime = existingSession[FocusSessionsTable.startTime]

            // 2. Focus Score Logic:
            // Base score starts at 100, reduced by 5 points per distraction
            val baseScore = (100 - (distractions * 5)).coerceIn(0, 100)
            
            // Completion Penalty: If finished early, multiply by completion ratio
            val actualDuration = java.time.Duration.between(startTime, endTime).toMinutes()
            val targetMinutes = targetDurationMinutes.coerceAtLeast(1)
            val completionRatio = (actualDuration.toDouble() / targetMinutes.toDouble()).coerceIn(0.0, 1.0)
            
            // Final score accounts for both distractions and time commitment
            val calculatedScore = (baseScore * completionRatio).toInt().coerceIn(0, 100)

            // 3. Update the row
            val updated = FocusSessionsTable.update({ FocusSessionsTable.id eq sId }) {
                it[FocusSessionsTable.endTime] = endTime
                it[FocusSessionsTable.distractions] = distractions
                it[FocusSessionsTable.focusScore] = calculatedScore
                it[FocusSessionsTable.durationMinutes] = actualDuration.toInt()
            }

            if (updated > 0) {
                println("DatabaseFactory: Session $sessionId ended successfully with score $calculatedScore")
                FocusSession(
                    id = sessionId,
                    userId = existingSession[FocusSessionsTable.userId].toString(),
                    startTime = startTime.toString(),
                    endTime = endTime.toString(),
                    focusScore = calculatedScore,
                    distractions = distractions
                )
            } else null
        } else null
    }

    suspend fun getShortHistory(userId: UUID): List<FocusSession> = dbQuery {
        FocusSessionsTable
            .select { FocusSessionsTable.userId eq userId }
            .orderBy(FocusSessionsTable.startTime, SortOrder.DESC)
            .limit(10)
            .map {
                FocusSession(
                    id = it[FocusSessionsTable.id].toString(),
                    userId = it[FocusSessionsTable.userId].toString(),
                    startTime = it[FocusSessionsTable.startTime].toString(),
                    endTime = it[FocusSessionsTable.endTime]?.toString(),
                    focusScore = it[FocusSessionsTable.focusScore],
                    distractions = it[FocusSessionsTable.distractions],
                    cognitiveLoad = it[FocusSessionsTable.cognitiveLoad] ?: "Low"
                )
            }
    }

    suspend fun insertDistractions(sessionId: String, userId: String, apps: List<com.example.models.DistractionApp>) = dbQuery {
        for (app in apps) {
            DistractionLogsTable.insert {
                it[DistractionLogsTable.userId] = userId
                it[DistractionLogsTable.appName] = app.appName
                it[DistractionLogsTable.usageTime] = app.usageTime
                it[DistractionLogsTable.sessionId] = sessionId
                it[DistractionLogsTable.createdAt] = LocalDateTime.now()
            }
        }
    }

    suspend fun getDistractionsList(userId: UUID): List<com.example.models.SessionDistractions> = dbQuery {
        val distLogs = DistractionLogsTable
            .select { DistractionLogsTable.userId eq userId.toString() }
            .orderBy(DistractionLogsTable.createdAt to SortOrder.DESC)
            
        val grouped = distLogs.groupBy { it[DistractionLogsTable.sessionId] }
        
        val resultList = mutableListOf<com.example.models.SessionDistractions>()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        var counter = 1
        for ((sessId, rows) in grouped) {
            if (counter > 15) break
            if (sessId == null) continue // Skip logs without a session
            
            val firstRow = rows.first()
            
            // Fetch session info separately to avoid complex join issues with UUID/String mismatch
            val sessionInfo = try {
                FocusSessionsTable.select { FocusSessionsTable.id eq UUID.fromString(sessId) }.singleOrNull()
            } catch (e: Exception) { null }
            
            val startTime = sessionInfo?.get(FocusSessionsTable.startTime) ?: firstRow[DistractionLogsTable.createdAt]
            val dateStr = startTime.format(dateFormatter)
            val timeStr = startTime.format(timeFormatter)
            
            // Combine duplicate apps in same session
            val appsMap = mutableMapOf<String, Int>()
            for (r in rows) {
                val appNm = r[DistractionLogsTable.appName]
                appsMap[appNm] = appsMap.getOrDefault(appNm, 0) + r[DistractionLogsTable.usageTime]
            }
            
            val appsList = appsMap.map { (k, v) ->
                com.example.models.DistractionApp(appName = k, usageTime = v)
            }.sortedByDescending { it.usageTime }
            
            resultList.add(
                com.example.models.SessionDistractions(
                    sessionTitle = "Session $timeStr",
                    date = dateStr,
                    startTime = timeStr,
                    apps = appsList
                )
            )
            counter++
        }
        resultList
    }
}
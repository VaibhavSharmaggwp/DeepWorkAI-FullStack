package com.example.repository



import com.example.db.DailyAnalyticsTable
import com.example.db.DatabaseFactory.dbQuery
import com.example.db.FocusSessionsTable
import com.example.db.SessionHistoryTable
import com.example.models.AnalyticsDashboard
import com.example.models.FocusSession
import com.example.models.SaveSessionRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.util.*

@Serializable
data class HistoryEntry(
    val focus_score: Int,
    val start_time: String,
    val distractions: Int
)

class FocusRepository {

    suspend fun saveSessionAndUpdateAnalytics(
        userId: String,
        sessionId: String,
        score: Int,
        duration: Int,
        switches: Int,
        risk: String,
        distractionList: List<Int> = List(16) { 0 }
    ) = dbQuery {
        val uId = UUID.fromString(userId)
        val today = LocalDate.now()

        // 1. Update raw session inside focus_sessions (For ML History)
        FocusSessionsTable.update({ FocusSessionsTable.id eq UUID.fromString(sessionId) }) {
            it[FocusSessionsTable.burnoutRisk] = risk
        }

        // 2. Upsert Daily Summary (For Analytics Screen)
        val existing = DailyAnalyticsTable.select {
            (DailyAnalyticsTable.userId eq uId) and (DailyAnalyticsTable.statDate eq today)
        }.singleOrNull()

        if (existing == null) {
            DailyAnalyticsTable.insert {
                it[DailyAnalyticsTable.userId] = uId
                it[DailyAnalyticsTable.statDate] = today
                it[DailyAnalyticsTable.avgFocusScore] = score
                it[DailyAnalyticsTable.totalDeepMinutes] = duration
                it[DailyAnalyticsTable.contextSwitches] = switches
                it[DailyAnalyticsTable.distractionHeatmap] = distractionList.joinToString(",")
            }
        } else {
            DailyAnalyticsTable.update({
                (DailyAnalyticsTable.userId eq uId) and (DailyAnalyticsTable.statDate eq today)
            }) {
                // Moving average for score, sum for minutes and switches
                it[avgFocusScore] = (existing[DailyAnalyticsTable.avgFocusScore] + score) / 2
                it[totalDeepMinutes] = existing[DailyAnalyticsTable.totalDeepMinutes] + duration
                it[contextSwitches] = existing[DailyAnalyticsTable.contextSwitches] + switches
                
                // Fetch and update heatmap
                val heatmapString = existing[DailyAnalyticsTable.distractionHeatmap]
                val heatmapList = heatmapString.split(",").map { it.toInt() }
                
                val updatedHeatmap = heatmapList.zip(distractionList) { a, b -> a + b }
                it[distractionHeatmap] = updatedHeatmap.joinToString(",")
            }
        }
    }

    suspend fun getAnalytics(userId: String, date: LocalDate) = dbQuery {
        DailyAnalyticsTable.select {
            (DailyAnalyticsTable.userId eq UUID.fromString(userId)) and (DailyAnalyticsTable.statDate eq date)
        }.singleOrNull()?.let { row ->
            val heatmapString = row[DailyAnalyticsTable.distractionHeatmap]
            val heatmapList = heatmapString.split(",").map { it.toInt() }
            
            // You can return a model here, but for now we follow the user's fetch logic
            heatmapList
        }
    }

    suspend fun getDashboard(userId: String, days: Int = 7): AnalyticsDashboard = dbQuery {
        val uId = UUID.fromString(userId)
        val todayDate = LocalDate.now()

        // 1. Fetch History for Python AI Insights
        val history = FocusSessionsTable.select { FocusSessionsTable.userId eq uId }
            .limit(50) // Limit input to ML for performance
            .map {
                HistoryEntry(
                    focus_score = it[FocusSessionsTable.focusScore],
                    start_time = it[FocusSessionsTable.startTime].toString(),
                    distractions = it[FocusSessionsTable.distractions]
                )
            }

        // 2. Call Python Insight Engine
        val aiMap = getAIAnalyticsInsights(Json.encodeToString(history))

        // 3. Fetch last N days of entries for Charts
        val historyEntries = DailyAnalyticsTable
            .select { DailyAnalyticsTable.userId eq uId }
            .orderBy(DailyAnalyticsTable.statDate, SortOrder.DESC)
            .limit(days)
            .map { it }
            .reversed() // Order them from older -> newer

        val today = historyEntries.lastOrNull()

        AnalyticsDashboard(
            weeklyScores = historyEntries.map { it[DailyAnalyticsTable.avgFocusScore] },
            weeklyDeepMinutes = historyEntries.map { it[DailyAnalyticsTable.totalDeepMinutes] },
            totalDeepMinutes = today?.get(DailyAnalyticsTable.totalDeepMinutes) ?: 0,
            contextSwitches = today?.get(DailyAnalyticsTable.contextSwitches) ?: 0,
            heatmap = today?.get(DailyAnalyticsTable.distractionHeatmap)
                ?.split(",")?.mapNotNull { it.toIntOrNull() }?.takeIf { it.isNotEmpty() } ?: List(16) { 0 },
            todayScore = today?.get(DailyAnalyticsTable.avgFocusScore) ?: 0,
            trend = "+12%", // Keep trend static as per current UI expectation or move to ML later
            cognitivePeakInsight = aiMap["peak"] ?: "Your brain enters flow state fastest between 9:00 AM and 11:30 AM.",
            consistencyInsight = aiMap["consistency"] ?: "Focus consistency improved by 21% compared to last week.",
            switchesInsight = aiMap["switches"] ?: "You switched apps 42 times during your last session."
        )
    }

    private fun getAIAnalyticsInsights(historyJson: String): Map<String, String> {
        return try {
            val pythonPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\venv\\Scripts\\python.exe"
            val scriptPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\analytics_insights.py"

            val process = ProcessBuilder(pythonPath, scriptPath, historyJson).start()
            val result = process.inputStream.bufferedReader().readText().trim()

            // Use pure Kotlin serialization to guarantee safety and throw on blank strings
            Json.decodeFromString<Map<String, String>>(result)
        } catch (e: Exception) {
            mapOf(
                "peak" to "Your brain enters flow state fastest between 9:00 AM and 11:30 AM.",
                "consistency" to "Focus consistency improved by 21% compared to last week.",
                "switches" to "Data pending..."
            )
        }
    }

    suspend fun saveSessionToHistory(req: SaveSessionRequest) = dbQuery {
        SessionHistoryTable.insert {
            it[id] = UUID.randomUUID()
            it[userId] = UUID.fromString(req.userId)
            it[startTime] = java.time.LocalDateTime.parse(req.startTime)
            it[endTime] = java.time.LocalDateTime.now()
            it[durationMinutes] = req.durationMinutes
            it[distractionsCount] = req.distractions
            it[focusStability] = req.stabilityScore
            it[avgDeepBlock] = req.avgDeepBlock
            it[cognitiveLoadStatus] = req.cognitiveLoad
        }
    }

    suspend fun getUserSessionHistory(userId: String): List<FocusSession> = dbQuery {
        SessionHistoryTable.select { SessionHistoryTable.userId eq UUID.fromString(userId) }
            .orderBy(SessionHistoryTable.startTime, SortOrder.DESC) // Newest sessions first
            .map {
                FocusSession(
                    id = it[SessionHistoryTable.id].toString(),
                    userId = it[SessionHistoryTable.userId].toString(),
                    startTime = it[SessionHistoryTable.startTime].toString(),
                    endTime = it[SessionHistoryTable.endTime].toString(),
                    focusScore = it[SessionHistoryTable.focusStability],
                    distractions = it[SessionHistoryTable.distractionsCount],
                    cognitiveLoad = it[SessionHistoryTable.cognitiveLoadStatus]
                )
            }
    }
}
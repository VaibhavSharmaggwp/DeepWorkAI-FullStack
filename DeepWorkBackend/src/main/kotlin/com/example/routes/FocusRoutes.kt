package com.example.routes

import com.example.db.DatabaseFactory
import com.example.models.EndSessionRequest
import com.example.models.FocusSession
import com.example.models.StartSessionRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.focusRoutes() {
    route("/sessions") { // Everything inside this block starts with /sessions

        // POST /sessions/start
        post("/start") {
            try {
                val request = call.receive<StartSessionRequest>()
                val session = DatabaseFactory.startFocusSession(UUID.fromString(request.userId))
                if (session != null) {
                    call.respond(HttpStatusCode.Created, session)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Could not start session")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // POST /sessions/end
        post("/end") {
            try {
                val request = call.receive<EndSessionRequest>()
                val updatedSession = DatabaseFactory.endFocusSession(request.sessionId, request.distractions)

                if (updatedSession != null) {
                    // 🧠 DYNAMIC CALCULATIONS
                    val startDateTime = java.time.OffsetDateTime.parse(updatedSession.startTime)
                    val endDateTime = java.time.OffsetDateTime.now()
                    val actualDuration = java.time.Duration.between(startDateTime, endDateTime).toMinutes().toDouble()
                    val currentHour = endDateTime.hour

                    val riskLabel = getMLBurnoutPrediction(
                        duration = actualDuration,
                        hour = currentHour,
                        distractions = updatedSession.distractions,
                        score = updatedSession.focusScore
                    )

                    call.respond(HttpStatusCode.OK, com.example.models.EndSessionResponse(
                        session = updatedSession,
                        burnoutRisk = riskLabel
                    ))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Session not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // GET /sessions/history/{userId}
        get("/history/{userId}") {
            val userIdString = call.parameters["userId"]
            try {
                val userId = UUID.fromString(userIdString)
                val history: List<FocusSession> = DatabaseFactory.getShortHistory(userId)
                call.respond(HttpStatusCode.OK, history)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}

fun getMLBurnoutPrediction(duration: Double, hour: Int, distractions: Int, score: Int): String {
    return try {
        // Build the command to run your Python script
        val pythonPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\venv\\Scripts\\python.exe"
        val scriptPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\predict_for_ktor.py"

        val process = ProcessBuilder(
            pythonPath, scriptPath,
            duration.toString(), hour.toString(), distractions.toString(), score.toString()
        ).start()

        // Read the output (the number 0, 1, or 2)
        val result = process.inputStream.bufferedReader().readText().trim()

        when (result) {
            "0" -> "Low"
            "1" -> "Medium"
            "2" -> "High"
            else -> "Low"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Low" // Default to Low if AI fails
    }
}
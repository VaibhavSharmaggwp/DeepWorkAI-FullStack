package com.example.routes
import com.example.db.DatabaseFactory
import com.example.models.EndSessionRequest
import com.example.models.EndSessionResponse
import com.example.models.SaveSessionRequest
import com.example.models.StartSessionRequest
import com.example.repository.FocusRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

// Pass the repository as a parameter so both route sets can use it
fun Route.allRoutes(repository: FocusRepository) {

    // --- FOCUS SESSIONS ROUTES ---
    route("/sessions") {
        post("/start") {
            try {
                val request = call.receive<StartSessionRequest>()
                println("FocusRoutes: Starting session for user ${request.userId}")
                val session = DatabaseFactory.startFocusSession(UUID.fromString(request.userId))
                if (session != null) {
                    println("FocusRoutes: Session started with ID ${session.id}")
                    call.respond(HttpStatusCode.Created, session)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Could not start session")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        post("/end") {
            try {
                val request = call.receive<EndSessionRequest>()
                println("FocusRoutes: Ending session ${request.sessionId} with ${request.distractions} distractions")
                val updatedSession = DatabaseFactory.endFocusSession(request.sessionId, request.distractions)

                if (updatedSession != null) {
                    println("FocusRoutes: Session found, calculating risk and saving history...")
                    val startDateTime = java.time.LocalDateTime.parse(updatedSession.startTime)
                    val endDateTime = java.time.LocalDateTime.now()
                    val actualDuration = java.time.Duration.between(startDateTime, endDateTime).toMinutes().toInt()
                    val currentHour = endDateTime.hour

                    val riskLabel = getMLBurnoutPrediction(
                        duration = actualDuration.toDouble(),
                        hour = currentHour,
                        distractions = updatedSession.distractions,
                        score = updatedSession.focusScore
                    )

                    if (!request.distractedApps.isNullOrEmpty()) {
                        DatabaseFactory.insertDistractions(
                            sessionId = updatedSession.id,
                            userId = updatedSession.userId.toString(),
                            apps = request.distractedApps
                        )
                    }

                    // 🚀 CRITICAL: Update the Analytics table whenever a session ends!
                    repository.saveSessionAndUpdateAnalytics(
                        userId = updatedSession.userId.toString(),
                        sessionId = updatedSession.id,
                        score = updatedSession.focusScore,
                        duration = actualDuration,
                        switches = updatedSession.distractions,
                        risk = riskLabel
                    )

                    // 🚀 Save to History table as well 
                    repository.saveSessionToHistory(
                        SaveSessionRequest(
                            userId = updatedSession.userId.toString(),
                            startTime = updatedSession.startTime,
                            endTime = java.time.LocalDateTime.now().toString(),
                            durationMinutes = actualDuration,
                            distractions = updatedSession.distractions,
                            stabilityScore = updatedSession.focusScore,
                            avgDeepBlock = actualDuration, // Simple mapping
                            cognitiveLoad = updatedSession.cognitiveLoad
                        )
                    )


                    println("FocusRoutes: Session ${updatedSession.id} automatically saved to history for user ${updatedSession.userId}")

                    call.respond(HttpStatusCode.OK, EndSessionResponse(
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
    }

    // --- ANALYTICS ROUTES (Moved outside /sessions) ---
    route("/analytics") {
        get("/dashboard/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val period = call.request.queryParameters["period"] ?: "weekly"
            val limit = if (period.lowercase() == "monthly") 30 else 7
            
            println("FocusRoutes: Fetching $period dashboard for user $userId (limit=$limit)")
            try {
                val dashboard = repository.getDashboard(userId, limit)
                call.respond(dashboard)
            } catch (e: Exception) {
                println("FocusRoutes: ERROR fetching dashboard: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }

        get("/distractions/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val distractions = DatabaseFactory.getDistractionsList(UUID.fromString(userId))
                val appsJson = Json.encodeToString(distractions)
                
                // Call Python to get recommendation
                val recommendation = getMLDistractionRecommendation(appsJson)
                
                call.respond(HttpStatusCode.OK, com.example.models.DistractionInsightsResponse(distractions, recommendation))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}

// Keep your ML function outside
fun getMLBurnoutPrediction(duration: Double, hour: Int, distractions: Int, score: Int): String {
    return try {
        val pythonPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\venv\\Scripts\\python.exe"
        val scriptPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\predict_for_ktor.py"

        val process = ProcessBuilder(
            pythonPath, scriptPath,
            duration.toString(), hour.toString(), distractions.toString(), score.toString()
        ).start()

        val result = process.inputStream.bufferedReader().readText().trim()

        when (result) {
            "0" -> "Low"
            "1" -> "Medium"
            "2" -> "High"
            else -> "Low"
        }
    } catch (e: Exception) {
        "Low"
    }
}

fun getMLDistractionRecommendation(appsDataJson: String): String {
    return try {
        val pythonPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\venv\\Scripts\\python.exe"
        val scriptPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\get_ai_recommendation.py"

        val process = ProcessBuilder(
            pythonPath, scriptPath, appsDataJson
        ).start()

        val result = process.inputStream.bufferedReader().readText().trim()
        result.ifBlank {
            "Consider limiting your usage of these apps during focus sessions."
        }
    } catch (e: Exception) {
        "Reduce your time on distracting apps to stay more focused."
    }
}

fun Route.sessionHistoryRoutes(repository: FocusRepository) {
    route("/sessions") {
        post("/save"){
            try{
                // Receive request from android
                val request = call.receive<SaveSessionRequest>()
                // Save to PostgreSQL history table
                repository.saveSessionToHistory(request)

                // Respond 201 Created
                call.respond(HttpStatusCode.Created, "Session saved successfully to history")
            }catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }

        // Added this block to fetch user history based on the user's request
        get("/history/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("FocusRoutes: Fetching history for user $userId")
            try {
                val history = repository.getUserSessionHistory(userId)
                println("FocusRoutes: Found ${history.size} sessions for $userId")
                call.respond(history)
            } catch (e: Exception) {
                println("FocusRoutes: ERROR fetching history: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        // End of added history route

        // Added this block to export PDF report via report_gen.py
        get("/export/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val history = repository.getUserSessionHistory(userId)
                
                // Call Python to generate PDF
                val pythonPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\venv\\Scripts\\python.exe"
                val scriptPath = "C:\\Users\\srija\\Desktop\\MAJOR_PROJECT\\deepwork_ml\\report_gen.py"
                
                val process = ProcessBuilder(pythonPath, scriptPath, Json.encodeToString(history)).start()
                val fileName = process.inputStream.bufferedReader().readText().trim()
                
                val file = File(fileName)
                if (file.exists()) {
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "FocusReport.pdf").toString()
                    )
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "PDF file was not generated.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        // End of PDF export route
    }
}
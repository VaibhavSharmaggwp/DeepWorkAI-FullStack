package com.example.plugins

import com.example.db.DatabaseFactory.dbQuery
import com.example.models.User
import com.example.models.Users
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import com.example.db.FocusSessionsTable
import java.io.File
import java.util.*

fun Application.configureProfileRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/api/user/profile") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    if (userIdStr == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Missing user ID")
                        return@get
                    }

                    val user = dbQuery {
                        val row = Users.select { Users.id eq UUID.fromString(userIdStr) }.singleOrNull()
                        if (row == null) return@dbQuery null
                        
                        val avgScore = FocusSessionsTable
                            .slice(FocusSessionsTable.focusScore.avg())
                            .select { FocusSessionsTable.userId eq UUID.fromString(userIdStr) }
                            .map { it[FocusSessionsTable.focusScore.avg()] }
                            .singleOrNull()?.toDouble()?.toInt() ?: 0
                            
                        toUser(row, avgScore)
                    }

                    if (user != null) {
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                }

                post("/update") {
                    val principal = call.principal<JWTPrincipal>()
                    val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    if (userIdStr == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Missing user ID")
                        return@post
                    }

                    val updateRequest = call.receive<UserUpdateRequest>()
                    dbQuery {
                        Users.update({ Users.id eq UUID.fromString(userIdStr) }) {
                            updateRequest.darkMode?.let { v -> it[darkMode] = v }
                            updateRequest.notificationsEnabled?.let { v -> it[notificationsEnabled] = v }
                            updateRequest.notificationType?.let { v -> it[notificationType] = v }
                            updateRequest.notificationTime?.let { v -> it[notificationTime] = v }
                        }
                    }
                    call.respond(HttpStatusCode.OK, "Profile updated")
                }

                post("/upload-image") {
                    val principal = call.principal<JWTPrincipal>()
                    val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    if (userIdStr == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Missing user ID")
                        return@post
                    }

                    try {
                        val multipart = call.receiveMultipart()
                        var savedImageUrl: String? = null
                        val storageService = com.example.service.StorageService()

                        multipart.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                val bytes = part.streamProvider().readBytes()
                                val originalName = part.originalFileName ?: "profile.jpg"
                                savedImageUrl = storageService.saveProfileImage(bytes, userIdStr, originalName)
                            }
                            part.dispose()
                        }

                        if (savedImageUrl != null) {
                            dbQuery {
                                Users.update({ Users.id eq UUID.fromString(userIdStr) }) {
                                    it[imageUrl] = savedImageUrl!!
                                }
                            }
                            call.respond(mapOf("imageUrl" to savedImageUrl!!))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "No image file found in request")
                        }
                    } catch (e: Exception) {
                        println("Error uploading image: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Failed to process image: ${e.message}")
                    }
                }
 
                 post("/cognitive/record") {
                     val principal = call.principal<JWTPrincipal>()
                     val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                     if (userIdStr == null) {
                         call.respond(HttpStatusCode.Unauthorized, "Missing user ID")
                         return@post
                     }
 
                     val request = call.receive<CognitiveResultRequest>()
                     val newStreak = com.example.db.DatabaseFactory.recordCognitiveResult(
                         UUID.fromString(userIdStr),
                         request.level,
                         request.score
                     )
                     call.respond(mapOf("streak" to newStreak))
                 }
             }
         }
     }
 }

private fun toUser(row: org.jetbrains.exposed.sql.ResultRow, calculatedScore: Int): User = User(
    id = row[Users.id].toString(),
    email = row[Users.email],
    fullName = row[Users.fullName],
    googleId = row[Users.googleId],
    isVerified = row[Users.isVerified],
    imageUrl = row[Users.imageUrl],
    focusScore = calculatedScore,
    darkMode = row[Users.darkMode],
    behavioralTracking = row[Users.behavioralTracking],
    notificationsEnabled = row[Users.notificationsEnabled],
    notificationType = row[Users.notificationType],
    notificationTime = row[Users.notificationTime],
    cognitiveStreak = row[Users.cognitiveStreak]
)

@kotlinx.serialization.Serializable
data class UserUpdateRequest(
    val darkMode: Boolean? = null,
    val notificationsEnabled: Boolean? = null,
    val notificationType: String? = null,
    val notificationTime: String? = null
)

@kotlinx.serialization.Serializable
data class CognitiveResultRequest(val level: Int, val score: Int)

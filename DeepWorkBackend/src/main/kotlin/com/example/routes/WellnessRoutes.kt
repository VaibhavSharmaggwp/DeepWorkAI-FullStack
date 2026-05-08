package com.example.routes

import com.example.models.UpdateWellnessRequest
import com.example.repository.WellnessRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

fun Route.wellnessRoutes() {
    val repository = WellnessRepository()

    route("/wellness") {
        get("/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val dateStr = call.request.queryParameters["date"] ?: LocalDate.now().toString()
            val date = LocalDate.parse(dateStr)
            val log = repository.getDailyWellness(userId, date)
            call.respond(log ?: HttpStatusCode.NotFound)
        }

        post("/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val dateStr = call.request.queryParameters["date"] ?: LocalDate.now().toString()
            val date = LocalDate.parse(dateStr)
            val request = call.receive<UpdateWellnessRequest>()
            val success = repository.updateWellness(userId, date, request)
            if (success) {
                call.respond(HttpStatusCode.OK, "Wellness updated")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Could not update wellness")
            }
        }
    }
}

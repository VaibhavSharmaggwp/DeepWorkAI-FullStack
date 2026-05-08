package com.example.routes

import com.example.models.CreateTaskRequest
import com.example.models.UpdateTaskStatusRequest
import com.example.repository.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes() {
    val repository = TaskRepository()

    route("/tasks") {
        post("/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<CreateTaskRequest>()
            val task = repository.createTask(userId, request)
            if (task != null) {
                call.respond(HttpStatusCode.Created, task)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Could not create task")
            }
        }

        get("/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val tasks = repository.getUserTasks(userId)
            call.respond(tasks)
        }

        patch("/{taskId}/status") {
            val taskId = call.parameters["taskId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<UpdateTaskStatusRequest>()
            val success = repository.updateTaskStatus(taskId, request.status)
            if (success) {
                call.respond(HttpStatusCode.OK, "Task status updated")
            } else {
                call.respond(HttpStatusCode.NotFound, "Task not found")
            }
        }

        delete("/{taskId}") {
            val taskId = call.parameters["taskId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val success = repository.deleteTask(taskId)
            if (success) {
                call.respond(HttpStatusCode.OK, "Task deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Task not found")
            }
        }
    }
}

package com.example.deepworkai.network

import com.example.deepworkai.models.CreateTaskRequest
import com.example.deepworkai.models.Task
import com.example.deepworkai.models.UpdateTaskStatusRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TaskService {
    private val client = KtorClient.httpClient
    private val baseUrl = NetworkPreferences.backendUrl

    suspend fun createTask(userId: String, request: CreateTaskRequest): Task? {
        return try {
            val response = client.post("$baseUrl/tasks/$userId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserTasks(userId: String): List<Task> {
        return try {
            val response = client.get("$baseUrl/tasks/$userId")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Boolean {
        return try {
            val response = client.patch("$baseUrl/tasks/$taskId/status") {
                contentType(ContentType.Application.Json)
                setBody(UpdateTaskStatusRequest(status))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            val response = client.delete("$baseUrl/tasks/$taskId")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

package com.example.deepworkai.network

import com.example.deepworkai.models.UpdateWellnessRequest
import com.example.deepworkai.models.WellnessLog
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class WellnessService {
    private val client = KtorClient.httpClient
    private val baseUrl = NetworkPreferences.backendUrl

    suspend fun getWellness(userId: String, date: String): WellnessLog? {
        return try {
            val response = client.get("$baseUrl/wellness/$userId?date=$date")
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateWellness(userId: String, date: String, request: UpdateWellnessRequest): Boolean {
        return try {
            val response = client.post("$baseUrl/wellness/$userId?date=$date") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

package com.example.deepworkai.network

import com.example.deepworkai.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.example.deepworkai.BuildConfig

class FocusService {
    private val BASE_URL = "https://${BuildConfig.BACKEND_IP}/sessions"

    suspend fun startSession(userId: String): FocusSession? {
        return try {
            KtorClient.httpClient.post("$BASE_URL/start") {
                contentType(ContentType.Application.Json)
                setBody(StartSessionRequest(userId))
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // This is the ONLY endSession function you need now
    suspend fun endSession(sessionId: String, distractions: Int): EndSessionResponse? {
        return try {
            KtorClient.httpClient.post("$BASE_URL/end") {
                contentType(ContentType.Application.Json)
                setBody(EndSessionRequest(sessionId, distractions))
            }.body() // This now captures both session and burnoutRisk
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
package com.example.deepworkai.network

import com.example.deepworkai.models.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File

class ProfileService {
    private val client = KtorClient.httpClient

    suspend fun getProfile(): User? {
        return try {
            client.get("${NetworkPreferences.backendUrl}/api/user/profile").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(
        darkMode: Boolean? = null,
        behavioralTracking: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        notificationType: String? = null,
        notificationTime: String? = null
    ): Boolean {
        return try {
            val response = client.post("${NetworkPreferences.backendUrl}/api/user/profile/update") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "darkMode" to darkMode,
                    "behavioralTracking" to behavioralTracking,
                    "notificationsEnabled" to notificationsEnabled,
                    "notificationType" to notificationType,
                    "notificationTime" to notificationTime
                ))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadProfileImage(file: File): String? {
        return try {
            val response: Map<String, String> = client.post("${NetworkPreferences.backendUrl}/api/user/profile/upload-image") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("image", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                        })
                    }
                ))
            }.body()
            response["imageUrl"]
        } catch (e: Exception) {
            null
        }
    }
}

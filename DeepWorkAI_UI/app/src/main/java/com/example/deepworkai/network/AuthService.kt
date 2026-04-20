package com.example.deepworkai.network

import android.util.Log
import com.example.deepworkai.models.LoginRequest
import com.example.deepworkai.models.AuthResponse
import com.example.deepworkai.models.RegisterRequest
import com.example.deepworkai.BuildConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class AuthService {

    // URL is configured via NetworkPreferences to allow dynamic IP changes
    private val BASE_URL get() = NetworkPreferences.backendUrl

    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        Log.d("AuthService", "Attempting login for ${loginRequest.email}")
        return try {
            val httpResponse = KtorClient.httpClient.post("$BASE_URL/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            Log.d("AuthService", "Login status: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                Result.success(httpResponse.body<AuthResponse>())
            } else {
                val errorBody = httpResponse.bodyAsText()
                Result.failure(Exception(if (errorBody.isNotBlank()) errorBody else "Invalid credentials"))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Login error", e)
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<AuthResponse> {
        Log.d("AuthService", "Attempting registration for ${registerRequest.email}")
        return try {
            val httpResponse = KtorClient.httpClient.post("$BASE_URL/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(registerRequest)
            }
            Log.d("AuthService", "Registration status: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK || httpResponse.status == HttpStatusCode.Created) {
                Result.success(httpResponse.body<AuthResponse>())
            } else {
                val errorBody = httpResponse.bodyAsText()
                Log.e("AuthService", "Registration error: $errorBody")
                Result.failure(Exception(if (errorBody.isNotBlank()) errorBody else "Registration failed"))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Registration exception", e)
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        Log.d("AuthService", "Attempting Google login sync. URL: $BASE_URL/auth/google")
        return try {
            val httpResponse = KtorClient.httpClient.post("$BASE_URL/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idToken" to idToken))
            }
            
            Log.d("AuthService", "Backend response status: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                val authResponse = httpResponse.body<AuthResponse>()
                Log.d("AuthService", "Google login sync successful for ${authResponse.user.email}")
                Result.success(authResponse)
            } else {
                val errorBody = httpResponse.bodyAsText()
                Log.e("AuthService", "Backend error: $errorBody")
                Result.failure(Exception(if (errorBody.isNotBlank()) errorBody else "Google Login failed to sync with server"))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Connection failed. Is the Ktor backend running at $BASE_URL?", e)
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }
}
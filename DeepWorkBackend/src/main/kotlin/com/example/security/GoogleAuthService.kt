package com.example.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory


class GoogleAuthService{
    // Retrieve this from environment variables for better security
    private val webClientId = System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "434928802643-mtbc86frtt955jk322403jonfit83k7b.apps.googleusercontent.com"
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(webClientId))
        .build()

    fun verifyGoogleToken(idTokenString: String): GoogleIdToken.Payload?{
        return try {
            val idToken = verifier.verify(idTokenString)
            // If valid, return the payload containing user details
            idToken?.payload
        }catch (e: Exception){
            null
        }
    }
}
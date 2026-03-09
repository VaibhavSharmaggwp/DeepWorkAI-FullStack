package com.example.plugins

import com.example.models.AuthResponse
import com.example.models.LoginRequest
import com.example.models.RegisterRequest
import com.example.repository.UserRepository
import com.example.security.GoogleAuthService
import com.example.security.JwtService
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Passing JWT parameter
fun Route.authRoutes(
    userRepository: UserRepository,
    jwtService: JwtService,
    googleAuthService: GoogleAuthService
) {
    route("/auth") {

        // 1. Standard Registration
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val existingUser = userRepository.findUserByEmail(request.email)
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, "Email already registered")
                return@post
            }

            val user = userRepository.createUser(request.fullName, request.email, request.password)
            if (user != null) {
                val token = jwtService.generateToken(user)
                call.respond(HttpStatusCode.Created, AuthResponse(user, token))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create user")
            }
        }

        // 2. Standard Login
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userRepository.loginUser(request.email, request.password)

            if (user != null) {
                // In Phase 2, we will return a JWT Token here
                val token = jwtService.generateToken(user)
                call.respond(HttpStatusCode.OK, AuthResponse(user, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
            }
        }

        // 3. Google Login
        post("/google") {
            try {
                val googleRequest = call.receive<Map<String, String>>()
                val idToken = googleRequest["idToken"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing idToken")
                
                println("AuthRoutes: Received Google login request")

                val payload = googleAuthService.verifyGoogleToken(idToken)

                if (payload != null) {
                    val googleEmail = payload.email
                    val googleId = payload.subject // Unique Google ID
                    val name = payload["name"] as? String ?: "Google User"
                    
                    println("AuthRoutes: Verified token for $googleEmail")

                    // Find user or create a new one for this Google ID
                    val user = userRepository.findOrCreateGoogleUser(googleId, googleEmail, name)

                    if (user != null) {
                        println("AuthRoutes: Successfully synced user $googleEmail")
                        val token = jwtService.generateToken(user)
                        call.respond(HttpStatusCode.OK, AuthResponse(user, token))
                    } else {
                        println("AuthRoutes: Failed to sync user in repository")
                        call.respond(HttpStatusCode.InternalServerError, "Failed to sync user")
                    }
                } else {
                    println("AuthRoutes: Invalid Google Token payload is null")
                    call.respond(HttpStatusCode.Unauthorized, "Invalid Google Token")
                }
            } catch (e: Exception) {
                println("AuthRoutes: CRASH in google login: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error during Google Auth")
            }
        }
    }

    // Protected Routes
    // Protected Routes
    authenticate("auth-jwt") {
        get("/me") {
            // This only runs if the token is valid!
            val principal = call.principal<JWTPrincipal>()
            val email = principal?.payload?.getClaim("email")?.asString()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            call.respond(HttpStatusCode.OK, mapOf(
                "message" to "Welcome to your profile!",
                "email" to email,
                "userId" to userId
            ))
        }
    }
}
package com.example.plugins

import com.example.repository.UserRepository
import com.example.security.JwtService
import com.example.security.GoogleAuthService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
fun Application.configureRouting(
    userRepository: UserRepository,
    jwtService: JwtService,
    googleAuthService: GoogleAuthService
) {
    routing {
        // This calls the file we created in the previous step
        authRoutes(userRepository, jwtService, googleAuthService)

        get("/") {
            call.respondText("DeepWork AI Backend is Live!")
        }
    }
}
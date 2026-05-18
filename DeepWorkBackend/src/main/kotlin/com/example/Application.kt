package com.example

import com.example.db.DatabaseFactory
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.repository.FocusRepository
import com.example.repository.UserRepository
import com.example.routes.*
import com.example.security.GoogleAuthService
import com.example.security.JwtService
import io.ktor.server.http.content.*
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.engine.embeddedServer(
        io.ktor.server.netty.Netty, 
        port = 8080, 
        host = "0.0.0.0", 
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {

    // 1. Initialize your repositories and services
    val userRepository = UserRepository()
    val jwtService = JwtService()
    val googleAuthService = GoogleAuthService()

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    val repository = FocusRepository()
    DatabaseFactory.init()

    routing {
        allRoutes(repository)
        sessionHistoryRoutes(repository)
        taskRoutes()
        wellnessRoutes()
    }
    // 3. Configure Plugins (Make sure Serialization is here!)
    configureSerialization()

    // 4. Pass the jwtService into your routing
    // This solves the "No value passed for parameter" error
    configureRouting(userRepository, jwtService, googleAuthService)
    
    // Register new routes
    configureProfileRoutes()
    configureExportRoutes(jwtService)

    // Serve static files from the uploads directory
    routing {
        static("/uploads") {
            staticRootFolder = File(".")
            files("uploads")
        }
    }

    // Start Background Services
    val notificationService = com.example.service.NotificationService(this)
    notificationService.start()
}

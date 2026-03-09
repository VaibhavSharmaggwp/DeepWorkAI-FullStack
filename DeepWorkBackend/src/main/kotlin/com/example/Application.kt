package com.example

import com.example.db.DatabaseFactory
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.repository.UserRepository
import com.example.security.JwtService
import com.example.security.GoogleAuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    // 1. Initialize your repositories and services
    val userRepository = UserRepository()
    val jwtService = JwtService()
    val googleAuthService = GoogleAuthService()

    // 2. Initialize the Database
    DatabaseFactory.init()

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
    // 3. Configure Plugins (Make sure Serialization is here!)
    configureSerialization()

    // 4. Pass the jwtService into your routing
    // This solves the "No value passed for parameter" error
    configureRouting(userRepository, jwtService, googleAuthService)
}

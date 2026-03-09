package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.User
import java.util.*

class JwtService {
    private val secret = System.getenv("JWT_SECRET") ?: "default_secret_for_local_dev"
    private val issuer = "com.example.deepwork"
    private val audience = "deepwork_audience"
    private val validityInMs = 36_00_000 * 24L // Added 'L' for Long

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(user: User): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("email", user.email)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
}
package com.example.repository

import com.example.db.DatabaseFactory.dbQuery
import com.example.models.User
import com.example.models.Users
import com.example.security.PasswordConfig
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update


class UserRepository {

    // Helper function to convert a Database Row into our User data class
    private fun rowToUser(row: ResultRow) = User(
        id = row[Users.id].toString(),
        email = row[Users.email],
        fullName = row[Users.fullName],
        googleId = row[Users.googleId],
        isVerified = row[Users.isVerified]
    )

    suspend fun findUserByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map(::rowToUser)
            .singleOrNull()
    }


    suspend fun loginUser(email: String, password: String): User? = dbQuery {
        // 1. Search for the user by email
        val row = Users.select { Users.email eq email }.singleOrNull()

        if (row != null) {
            val storedHash = row[Users.passwordHash]
            // 2. Check if the password matches the hashed version in the DB
            if (storedHash != null && PasswordConfig.checkPassword(password, storedHash)) {
                return@dbQuery rowToUser(row)
            }
        }
        null // Return null if user not found or password incorrect
    }

    suspend fun createUser(fullName: String, email: String, password: String?): User? = dbQuery {
        val insertStatement = Users.insert {
            it[Users.fullName] = fullName
            it[Users.email] = email
            // We hash it here before it ever touches the database!
            it[Users.passwordHash] = password?.let { PasswordConfig.hashPassword(it) }
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::rowToUser)
    }

    suspend fun findOrCreateGoogleUser(googleId: String, email: String, fullName: String): User? = dbQuery {
        println("UserRepository: Syncing Google User - Email: $email, GoogleID: $googleId")
        
        // 1. Check if the user already exists by their Google ID
        val userByGoogleId = Users.select { Users.googleId eq googleId }.singleOrNull()
        if (userByGoogleId != null) {
            println("UserRepository: Found user by Google ID")
            return@dbQuery rowToUser(userByGoogleId)
        }

        // 2. Check if the user already exists by their Email (but might have been registered normally)
        val userByEmail = Users.select { Users.email eq email }.singleOrNull()
        if (userByEmail != null) {
            println("UserRepository: Found user by Email, updating with Google ID")
            Users.update({ Users.email eq email }) {
                it[Users.googleId] = googleId
                // Optionally update full name if it was just a placeholder
            }
            return@dbQuery Users.select { Users.email eq email }.map(::rowToUser).singleOrNull()
        }

        // 3. If not found at all, create a new record
        println("UserRepository: Creating new Google User")
        val insertStatement = Users.insert {
            it[Users.googleId] = googleId
            it[Users.email] = email
            it[Users.fullName] = fullName
            it[Users.passwordHash] = null // No password needed for Google users
        }

        insertStatement.resultedValues?.singleOrNull()?.let(::rowToUser)
    }
}
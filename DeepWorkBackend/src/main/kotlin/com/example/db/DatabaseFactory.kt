package com.example.db

import com.example.models.FocusSession
import com.example.models.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.util.*

object DatabaseFactory{
    fun init(){
        // These credentials connect Ktor to the PostgreSQL you installed
        val driverClassName = "org.postgresql.Driver"
        val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5433/deepwork_db"
        val user = System.getenv("DATABASE_USER") ?: "postgres"
        val password = System.getenv("DATABASE_PASSWORD") ?: ""
        
        println("DatabaseFactory: Using JDBC URL: $jdbcUrl")
        
        println("DatabaseFactory: Connecting to $jdbcUrl as $user")
        
        try {
            val database = Database.connect(jdbcUrl, driverClassName, user, password)

            transaction(database){
                SchemaUtils.create(Users)
                println("DatabaseFactory: Table 'users' verified/created")
            }
            println("DatabaseFactory: Connection successful")
        } catch (e: Exception) {
            println("DatabaseFactory: CONNECTION FAILED: ${e.message}")
            e.printStackTrace()
        }
    }

    // This helper makes sure database operations don't "freeze" your server
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    suspend fun startFocusSession(userId: UUID): FocusSession? = dbQuery {
        val sessionId = UUID.randomUUID()
        val startTime = OffsetDateTime.now()

        // Access the JDBC connection from the current Exposed transaction
        val connection = TransactionManager.current().connection.connection as java.sql.Connection

        val insertStatement = connection.prepareStatement(
            "INSERT INTO focus_sessions (id, user_id, start_time) VALUES (?, ?, ?)"
        )

        insertStatement.setObject(1, sessionId)
        insertStatement.setObject(2, userId)
        insertStatement.setObject(3, startTime)

        val result = insertStatement.executeUpdate()

        if (result > 0) {
            FocusSession(
                id = sessionId.toString(),
                userId = userId.toString(),
                startTime = startTime.toString()
            )
        } else null
    }
    suspend fun endFocusSession(sessionId: String, distractions: Int): FocusSession? = dbQuery {
        val endTime = OffsetDateTime.now()
        val connection = TransactionManager.current().connection.connection as java.sql.Connection

        // 1. First, we need to get the start_time to calculate the score
        val selectStatement = connection.prepareStatement(
            "SELECT start_time FROM focus_sessions WHERE id = ?"
        )
        selectStatement.setObject(1, UUID.fromString(sessionId))
        val resultSet = selectStatement.executeQuery()

        if (resultSet.next()) {
            val startTime = resultSet.getObject("start_time", OffsetDateTime::class.java)

            // 2. Simple Math: Score starts at 100 and drops per distraction
            // Later, we can make this much smarter with your ML model
            val durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes()
            val calculatedScore = if (durationMinutes > 0) {
                (100 - (distractions * 5)).coerceIn(0, 100)
            } else 100

            // 3. Update the row
            val updateStatement = connection.prepareStatement(
                "UPDATE focus_sessions SET end_time = ?, distractions = ?, focus_score = ? WHERE id = ?"
            )
            updateStatement.setObject(1, endTime)
            updateStatement.setInt(2, distractions)
            updateStatement.setInt(3, calculatedScore)
            updateStatement.setObject(4, UUID.fromString(sessionId))

            val result = updateStatement.executeUpdate()

            if (result > 0) {
                FocusSession(
                    id = sessionId,
                    userId = "", // We can leave this blank for the response
                    startTime = startTime.toString(),
                    endTime = endTime.toString(),
                    focusScore = calculatedScore,
                    distractions = distractions
                )
            } else null
        } else null
    }

    suspend fun getShortHistory(userId: UUID): List<FocusSession> = dbQuery {
        val connection = TransactionManager.current().connection.connection as java.sql.Connection
        val sessions = mutableListOf<FocusSession>()

        // Query to get the most recent sessions first
        val selectStatement = connection.prepareStatement(
            "SELECT * FROM focus_sessions WHERE user_id = ? ORDER BY start_time DESC LIMIT 10"
        )
        selectStatement.setObject(1, userId)
        val resultSet = selectStatement.executeQuery()

        while (resultSet.next()) {
            sessions.add(
                FocusSession(
                    id = resultSet.getString("id"),
                    userId = resultSet.getString("user_id"),
                    startTime = resultSet.getTimestamp("start_time").toInstant().toString(),
                    endTime = resultSet.getTimestamp("end_time")?.toInstant()?.toString(),
                    focusScore = resultSet.getInt("focus_score"),
                    distractions = resultSet.getInt("distractions"),
                    cognitiveLoad = resultSet.getString("cognitive_load")
                )
            )
        }
        sessions
    }
}
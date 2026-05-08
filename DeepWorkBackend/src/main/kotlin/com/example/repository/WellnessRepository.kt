package com.example.repository

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.WellnessTable
import com.example.models.UpdateWellnessRequest
import com.example.models.WellnessLog
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate
import java.util.*

class WellnessRepository {

    suspend fun getDailyWellness(userId: String, date: LocalDate): WellnessLog? = dbQuery {
        WellnessTable.select { (WellnessTable.userId eq UUID.fromString(userId)) and (WellnessTable.logDate eq date) }
            .map {
                WellnessLog(
                    userId = it[WellnessTable.userId].toString(),
                    date = it[WellnessTable.logDate].toString(),
                    sleepHours = it[WellnessTable.sleepHours],
                    hydrationLevel = it[WellnessTable.hydrationLevel],
                    meditated = it[WellnessTable.meditated],
                    exercise = it[WellnessTable.exercise]
                )
            }.singleOrNull() ?: WellnessLog(userId, date.toString(), 0, 0, false, false)
    }

    suspend fun updateWellness(userId: String, date: LocalDate, req: UpdateWellnessRequest): Boolean = dbQuery {
        val uId = UUID.fromString(userId)
        val exists = WellnessTable.select { (WellnessTable.userId eq uId) and (WellnessTable.logDate eq date) }.count() > 0
        
        if (exists) {
            WellnessTable.update({ (WellnessTable.userId eq uId) and (WellnessTable.logDate eq date) }) {
                req.sleepHours?.let { s -> it[sleepHours] = s }
                req.hydrationLevel?.let { h -> it[hydrationLevel] = h }
                req.meditated?.let { m -> it[meditated] = m }
                req.exercise?.let { e -> it[exercise] = e }
            } > 0
        } else {
            WellnessTable.insert {
                it[WellnessTable.userId] = uId
                it[WellnessTable.logDate] = date
                it[sleepHours] = req.sleepHours ?: 0
                it[hydrationLevel] = req.hydrationLevel ?: 0
                it[meditated] = req.meditated ?: false
                it[exercise] = req.exercise ?: false
            }.insertedCount > 0
        }
    }
}

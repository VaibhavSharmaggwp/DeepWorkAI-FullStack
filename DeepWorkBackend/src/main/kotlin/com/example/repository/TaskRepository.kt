package com.example.repository

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.TasksTable
import com.example.models.CreateTaskRequest
import com.example.models.Task
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.*

class TaskRepository {

    suspend fun createTask(userId: String, req: CreateTaskRequest): Task? = dbQuery {
        val taskId = UUID.randomUUID()
        val now = LocalDateTime.now()
        
        // Simple AI logic: If title contains "Code", "Research", "Design", "Write", it's DEEP
        val deepKeywords = listOf("code", "research", "design", "write", "study", "develop", "analyze")
        val category = if (deepKeywords.any { req.title.lowercase().contains(it) }) "Deep" else "Shallow"

        val result = TasksTable.insert {
            it[id] = taskId
            it[TasksTable.userId] = UUID.fromString(userId)
            it[title] = req.title
            it[description] = req.description
            it[TasksTable.category] = category
            it[status] = "Pending"
            it[estimatedMinutes] = req.estimatedMinutes
            it[createdAt] = now
        }

        if (result.insertedCount > 0) {
            Task(
                id = taskId.toString(),
                userId = userId,
                title = req.title,
                description = req.description,
                category = category,
                status = "Pending",
                estimatedMinutes = req.estimatedMinutes,
                actualMinutes = 0,
                createdAt = now.toString()
            )
        } else null
    }

    suspend fun getUserTasks(userId: String): List<Task> = dbQuery {
        TasksTable.select { TasksTable.userId eq UUID.fromString(userId) }
            .orderBy(TasksTable.createdAt, SortOrder.DESC)
            .map {
                Task(
                    id = it[TasksTable.id].toString(),
                    userId = it[TasksTable.userId].toString(),
                    title = it[TasksTable.title],
                    description = it[TasksTable.description],
                    category = it[TasksTable.category],
                    status = it[TasksTable.status],
                    estimatedMinutes = it[TasksTable.estimatedMinutes],
                    actualMinutes = it[TasksTable.actualMinutes],
                    createdAt = it[TasksTable.createdAt].toString(),
                    completedAt = it[TasksTable.completedAt]?.toString()
                )
            }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Boolean = dbQuery {
        val updateCount = TasksTable.update({ TasksTable.id eq UUID.fromString(taskId) }) {
            it[TasksTable.status] = status
            if (status == "Completed") {
                it[completedAt] = LocalDateTime.now()
            }
        }
        updateCount > 0
    }

    suspend fun deleteTask(taskId: String): Boolean = dbQuery {
        val deleteCount = TasksTable.deleteWhere { TasksTable.id eq UUID.fromString(taskId) }
        deleteCount > 0
    }
}

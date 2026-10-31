package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("isCompleted"),
        Index("dueDate"),
        Index("priority")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val dueDate: Long? = null,
    val dueTime: String? = null, // e.g. "09:00"
    val isCompleted: Boolean = false,
    val categoryId: Long? = null,
    val hasReminder: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

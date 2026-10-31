package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val pendingTaskCount: Flow<Int> = taskDao.getPendingTaskCount()
    val completedTaskCount: Flow<Int> = taskDao.getCompletedTaskCount()

    fun getTaskByIdFlow(id: Long): Flow<TaskEntity?> = taskDao.getTaskByIdFlow(id)

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    fun getTasksByCategory(categoryId: Long): Flow<List<TaskEntity>> = taskDao.getTasksByCategory(categoryId)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insert(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.update(task)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        taskDao.update(task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteById(id)

    suspend fun getAllTasksSync(): List<TaskEntity> = taskDao.getAllTasksSync()

    suspend fun insertAllTasks(tasks: List<TaskEntity>) = taskDao.insertAll(tasks)

    suspend fun deleteAllTasks() = taskDao.deleteAll()
}

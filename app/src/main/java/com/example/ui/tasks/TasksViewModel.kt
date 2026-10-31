package com.example.ui.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.ExportUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.NotesRepository
import com.example.data.repository.TaskRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilter {
    ALL,
    TODAY,
    UPCOMING,
    OVERDUE,
    COMPLETED
}

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val completionPercentage: Float = 0f
)

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter = _currentFilter.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.allTasks,
        notesRepository.allCategories,
        _currentFilter,
        _selectedCategoryId,
        _searchQuery
    ) { allTasks, categories, filter, categoryId, query ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfToday = cal.timeInMillis

        var filtered = allTasks.filter { task ->
            val matchesCat = categoryId == null || task.categoryId == categoryId
            val matchesQuery = query.isBlank() || task.title.contains(query, ignoreCase = true) || task.description.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }

        filtered = when (filter) {
            TaskFilter.ALL -> filtered
            TaskFilter.TODAY -> filtered.filter {
                !it.isCompleted && it.dueDate != null && it.dueDate >= startOfToday && it.dueDate <= endOfToday
            }
            TaskFilter.UPCOMING -> filtered.filter {
                !it.isCompleted && it.dueDate != null && it.dueDate > endOfToday
            }
            TaskFilter.OVERDUE -> filtered.filter {
                !it.isCompleted && it.dueDate != null && it.dueDate < startOfToday
            }
            TaskFilter.COMPLETED -> filtered.filter { it.isCompleted }
        }

        val total = allTasks.size
        val completed = allTasks.count { it.isCompleted }
        val pending = total - completed
        val percent = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        TasksUiState(
            tasks = filtered,
            categories = categories,
            currentFilter = filter,
            selectedCategoryId = categoryId,
            searchQuery = query,
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            completionPercentage = percent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun setCategory(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveTask(
        context: Context,
        id: Long = 0L,
        title: String,
        description: String,
        priority: String,
        dueDate: Long?,
        dueTime: String?,
        categoryId: Long?,
        hasReminder: Boolean
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                isCompleted = false,
                categoryId = categoryId,
                hasReminder = hasReminder,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val insertedId = if (id == 0L) {
                taskRepository.insertTask(task)
            } else {
                taskRepository.updateTask(task)
                id
            }

            if (hasReminder && dueDate != null) {
                NotificationHelper.scheduleTaskReminder(context, insertedId, title, dueDate)
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun exportTasksTxt(context: Context) {
        viewModelScope.launch {
            val allTasks = taskRepository.getAllTasksSync()
            val file = ExportUtils.exportTasksAsTxt(context, allTasks)
            ExportUtils.shareFile(context, file, "text/plain")
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val notesRepository: NotesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(taskRepository, notesRepository) as T
        }
    }
}

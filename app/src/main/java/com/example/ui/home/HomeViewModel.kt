package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.JournalRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeStats(
    val totalNotes: Int = 0,
    val pendingTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalJournals: Int = 0
)

data class HomeUiState(
    val greeting: String = "Hello",
    val formattedDate: String = "",
    val stats: HomeStats = HomeStats(),
    val pinnedNotes: List<NoteEntity> = emptyList(),
    val recentNotes: List<NoteEntity> = emptyList(),
    val todayJournal: JournalEntity? = null,
    val pendingTasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<NoteEntity> = emptyList()
)

class HomeViewModel(
    private val notesRepository: NotesRepository,
    private val journalRepository: JournalRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val uiState: StateFlow<HomeUiState> = combine(
        notesRepository.allActiveNotes,
        notesRepository.pinnedNotes,
        journalRepository.getJournalsByDate(todayDateStr),
        taskRepository.pendingTasks,
        taskRepository.completedTaskCount,
        journalRepository.journalCount
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val activeNotes = args[0] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val pinned = args[1] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val todayJournals = args[2] as List<JournalEntity>
        @Suppress("UNCHECKED_CAST")
        val pendingTasks = args[3] as List<TaskEntity>
        val completedTasksCount = args[4] as Int
        val journalCount = args[5] as Int

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        val formattedDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        HomeUiState(
            greeting = greeting,
            formattedDate = formattedDate,
            stats = HomeStats(
                totalNotes = activeNotes.size,
                pendingTasks = pendingTasks.size,
                completedTasks = completedTasksCount,
                totalJournals = journalCount
            ),
            pinnedNotes = pinned,
            recentNotes = activeNotes.take(5),
            todayJournal = todayJournals.firstOrNull(),
            pendingTasks = pendingTasks.take(4)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    class Factory(
        private val notesRepository: NotesRepository,
        private val journalRepository: JournalRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(notesRepository, journalRepository, taskRepository) as T
        }
    }
}

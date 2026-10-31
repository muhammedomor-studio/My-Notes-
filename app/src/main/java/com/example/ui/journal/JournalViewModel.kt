package com.example.ui.journal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.ExportUtils
import com.example.data.local.entity.JournalEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.JournalRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class JournalUiState(
    val selectedDate: String = "",
    val formattedDisplayDate: String = "",
    val entriesForDate: List<JournalEntity> = emptyList(),
    val allEntries: List<JournalEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<JournalEntity> = emptyList(),
    val isTimelineView: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val reminderTime: String = "20:00"
)

class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isTimelineView = MutableStateFlow(false)
    val isTimelineView = _isTimelineView.asStateFlow()

    val uiState: StateFlow<JournalUiState> = combine(
        _selectedDate,
        journalRepository.allJournals,
        _searchQuery,
        _isTimelineView,
        preferences.isJournalReminderEnabled,
        preferences.journalReminderTime
    ) { args ->
        val date = args[0] as String
        @Suppress("UNCHECKED_CAST")
        val allJournals = args[1] as List<JournalEntity>
        val query = args[2] as String
        val isTimeline = args[3] as Boolean
        val reminderEnabled = args[4] as Boolean
        val reminderTime = args[5] as String

        val parsedDate = try {
            dateFormat.parse(date) ?: Date()
        } catch (_: Exception) {
            Date()
        }
        val formattedDisplay = displayFormat.format(parsedDate)

        val entriesForDate = allJournals.filter { it.entryDate == date }

        val searchResults = if (query.isBlank()) emptyList() else {
            allJournals.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.tags.contains(query, ignoreCase = true) ||
                        (it.mood != null && it.mood.contains(query, ignoreCase = true))
            }
        }

        JournalUiState(
            selectedDate = date,
            formattedDisplayDate = formattedDisplay,
            entriesForDate = entriesForDate,
            allEntries = allJournals,
            searchQuery = query,
            searchResults = searchResults,
            isTimelineView = isTimeline,
            isReminderEnabled = reminderEnabled,
            reminderTime = reminderTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState()
    )

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun goToToday() {
        _selectedDate.value = dateFormat.format(Date())
    }

    fun goToPreviousDay() {
        changeDayBy(-1)
    }

    fun goToNextDay() {
        changeDayBy(1)
    }

    private fun changeDayBy(days: Int) {
        val current = try {
            dateFormat.parse(_selectedDate.value) ?: Date()
        } catch (_: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_YEAR, days)
        }
        _selectedDate.value = dateFormat.format(cal.time)
    }

    fun toggleViewMode() {
        _isTimelineView.value = !_isTimelineView.value
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveJournalEntry(
        id: Long = 0L,
        title: String,
        content: String,
        mood: String?,
        date: String,
        tags: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                journalRepository.insertJournal(
                    JournalEntity(
                        title = title.trim(),
                        content = content.trim(),
                        mood = mood,
                        entryDate = date,
                        tags = tags.trim(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                val existing = journalRepository.getJournalById(id)
                if (existing != null) {
                    journalRepository.updateJournal(
                        existing.copy(
                            title = title.trim(),
                            content = content.trim(),
                            mood = mood,
                            entryDate = date,
                            tags = tags.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun deleteJournal(journal: JournalEntity) {
        viewModelScope.launch {
            journalRepository.deleteJournal(journal)
        }
    }

    fun toggleDailyReminder(context: Context, enabled: Boolean, time: String) {
        viewModelScope.launch {
            preferences.setJournalReminder(enabled, time)
            if (enabled) {
                val parts = time.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                NotificationHelper.scheduleDailyJournalReminder(context, hour, minute)
            } else {
                NotificationHelper.cancelJournalReminder(context)
            }
        }
    }

    fun exportJournalTxt(context: Context, journal: JournalEntity) {
        val file = ExportUtils.exportJournalAsTxt(context, journal)
        ExportUtils.shareFile(context, file, "text/plain")
    }

    fun exportJournalPdf(context: Context, journal: JournalEntity) {
        val file = ExportUtils.exportJournalAsPdf(context, journal)
        ExportUtils.shareFile(context, file, "application/pdf")
    }

    class Factory(
        private val journalRepository: JournalRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(journalRepository, preferences) as T
        }
    }
}

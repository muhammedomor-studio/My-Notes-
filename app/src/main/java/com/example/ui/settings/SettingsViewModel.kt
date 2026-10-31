package com.example.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupData
import com.example.data.backup.BackupManager
import com.example.data.backup.ExportUtils
import com.example.data.preferences.AppPreferences
import com.example.data.repository.JournalRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.TaskRepository
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val isPinEnabled: Boolean = false,
    val autoLockTimeout: Long = 0L,
    val defaultNotesView: String = "GRID",
    val defaultNotesSort: String = "RECENT_UPDATED",
    val defaultNoteColor: String = "#FFFFFF",
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val notesRepository: NotesRepository,
    private val journalRepository: JournalRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.themeMode,
        preferences.isPinEnabled,
        preferences.autoLockTimeout,
        preferences.defaultNotesView,
        preferences.defaultNotesSort,
        preferences.defaultNoteColor,
        _statusMessage
    ) { args ->
        SettingsUiState(
            themeMode = args[0] as String,
            isPinEnabled = args[1] as Boolean,
            autoLockTimeout = args[2] as Long,
            defaultNotesView = args[3] as String,
            defaultNotesSort = args[4] as String,
            defaultNoteColor = args[5] as String,
            statusMessage = args[6] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            preferences.setThemeMode(theme)
        }
    }

    fun setAutoLockTimeout(timeout: Long) {
        viewModelScope.launch {
            preferences.setAutoLockTimeout(timeout)
        }
    }

    fun setDefaultNotesView(view: String) {
        viewModelScope.launch {
            preferences.setDefaultNotesView(view)
        }
    }

    fun setDefaultNotesSort(sort: String) {
        viewModelScope.launch {
            preferences.setDefaultNotesSort(sort)
        }
    }

    fun setDefaultNoteColor(color: String) {
        viewModelScope.launch {
            preferences.setDefaultNoteColor(color)
        }
    }

    fun setupNewPin(pin: String) {
        viewModelScope.launch {
            val salt = SecurityManager.generateSalt()
            val hash = SecurityManager.hashPin(pin, salt)
            preferences.setPin(hash, salt)
            _statusMessage.value = "PIN protection successfully enabled"
        }
    }

    fun disablePin(enteredPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = SecurityManager.verifyPin(enteredPin, preferences)
            if (valid) {
                preferences.disablePin()
                _statusMessage.value = "PIN protection disabled"
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun createAndShareBackup(context: Context) {
        viewModelScope.launch {
            try {
                val json = BackupManager.createBackupJson(
                    notesRepository,
                    journalRepository,
                    taskRepository
                )
                val file = BackupManager.exportBackupFile(context, json)
                ExportUtils.shareFile(context, file, "application/json")
                _statusMessage.value = "Backup created successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to create backup: ${e.message}"
            }
        }
    }

    fun validateAndRestoreBackup(
        jsonString: String,
        onSuccess: (BackupData) -> Unit,
        onError: (String) -> Unit
    ) {
        val result = BackupManager.parseAndValidateBackup(jsonString)
        result.fold(
            onSuccess = { data -> onSuccess(data) },
            onFailure = { error -> onError(error.message ?: "Invalid backup file format") }
        )
    }

    fun applyBackup(data: BackupData) {
        viewModelScope.launch {
            try {
                BackupManager.restoreBackup(data, notesRepository, journalRepository, taskRepository)
                _statusMessage.value = "Backup restored successfully (${data.notes.size} notes, ${data.journals.size} entries, ${data.tasks.size} tasks)"
            } catch (e: Exception) {
                _statusMessage.value = "Restore failed: ${e.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            notesRepository.deleteAllData()
            journalRepository.deleteAllJournals()
            taskRepository.deleteAllTasks()
            _statusMessage.value = "All data cleared successfully"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    class Factory(
        private val preferences: AppPreferences,
        private val notesRepository: NotesRepository,
        private val journalRepository: JournalRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferences, notesRepository, journalRepository, taskRepository) as T
        }
    }
}

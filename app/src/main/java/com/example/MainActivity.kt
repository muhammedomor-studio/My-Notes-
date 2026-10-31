package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.navigation.MainApp
import com.example.ui.home.HomeViewModel
import com.example.ui.journal.JournalViewModel
import com.example.ui.lock.LockViewModel
import com.example.ui.notes.NotesViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.tasks.TasksViewModel
import com.example.ui.theme.MyNotesTheme

class MainActivity : ComponentActivity() {

    private val app by lazy { application as MyNotesApplication }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(app.notesRepository, app.journalRepository, app.taskRepository)
    }

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModel.Factory(app.notesRepository, app.preferences)
    }

    private val journalViewModel: JournalViewModel by viewModels {
        JournalViewModel.Factory(app.journalRepository, app.preferences)
    }

    private val tasksViewModel: TasksViewModel by viewModels {
        TasksViewModel.Factory(app.taskRepository, app.notesRepository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(app.preferences, app.notesRepository, app.journalRepository, app.taskRepository)
    }

    private val lockViewModel: LockViewModel by viewModels {
        LockViewModel.Factory(app.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = when (settingsState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyNotesTheme(darkTheme = isDarkTheme) {
                MainApp(
                    homeViewModel = homeViewModel,
                    notesViewModel = notesViewModel,
                    journalViewModel = journalViewModel,
                    tasksViewModel = tasksViewModel,
                    settingsViewModel = settingsViewModel,
                    lockViewModel = lockViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lockViewModel.onAppResume()
    }

    override fun onPause() {
        super.onPause()
        lockViewModel.onAppPause()
    }
}

package com.example.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.NoteEntity
import com.example.ui.home.HomeDashboardScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.journal.JournalScreen
import com.example.ui.journal.JournalViewModel
import com.example.ui.lock.LockScreen
import com.example.ui.lock.LockViewModel
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.notes.NotesViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.tasks.TasksScreen
import com.example.ui.tasks.TasksViewModel

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    NOTES("Notes", Icons.Filled.Description, Icons.Outlined.Description),
    JOURNAL("Journal", Icons.Filled.Book, Icons.Outlined.Book),
    TASKS("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainApp(
    homeViewModel: HomeViewModel,
    notesViewModel: NotesViewModel,
    journalViewModel: JournalViewModel,
    tasksViewModel: TasksViewModel,
    settingsViewModel: SettingsViewModel,
    lockViewModel: LockViewModel,
    modifier: Modifier = Modifier
) {
    val lockState by lockViewModel.uiState.collectAsStateWithLifecycle()

    if (lockState.isLocked) {
        LockScreen(
            uiState = lockState,
            onDigitEntered = lockViewModel::onDigitEntered,
            onBackspace = lockViewModel::onBackspace,
            onClear = lockViewModel::onClear,
            onToggleShowPin = lockViewModel::toggleShowPin,
            onVerifyExplicit = lockViewModel::verifyExplicit
        )
        return
    }

    var currentTab by remember { mutableStateOf(MainDestination.HOME) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var isEditingNoteOpen by remember { mutableStateOf(false) }

    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val notesUiState by notesViewModel.uiState.collectAsStateWithLifecycle()
    val journalUiState by journalViewModel.uiState.collectAsStateWithLifecycle()
    val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    if (isEditingNoteOpen) {
        BackHandler {
            isEditingNoteOpen = false
            editingNote = null
        }

        NoteEditorScreen(
            note = editingNote,
            categories = notesUiState.categories,
            onSave = { id, title, content, catId, color, tags, isPinned, isFav ->
                notesViewModel.saveNote(id, title, content, catId, color, tags, isPinned, isFav)
                isEditingNoteOpen = false
                editingNote = null
            },
            onDelete = { note ->
                notesViewModel.deleteNote(note)
                isEditingNoteOpen = false
                editingNote = null
            },
            onShare = { note -> notesViewModel.shareNote(context, note) },
            onExportTxt = { note -> notesViewModel.exportNoteTxt(context, note) },
            onExportPdf = { note -> notesViewModel.exportNotePdf(context, note) },
            onBack = {
                isEditingNoteOpen = false
                editingNote = null
            }
        )
        return
    }

    // Secondary tab back navigation
    if (currentTab != MainDestination.HOME) {
        BackHandler {
            currentTab = MainDestination.HOME
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MainDestination.entries.forEach { destination ->
                    val isSelected = currentTab == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { target ->
                when (target) {
                    MainDestination.HOME -> HomeDashboardScreen(
                        uiState = homeUiState,
                        onSearchChange = homeViewModel::onSearchQueryChanged,
                        onCreateNoteClick = {
                            editingNote = null
                            isEditingNoteOpen = true
                        },
                        onCreateJournalClick = {
                            currentTab = MainDestination.JOURNAL
                        },
                        onCreateTaskClick = {
                            currentTab = MainDestination.TASKS
                        },
                        onNoteClick = { note ->
                            editingNote = note
                            isEditingNoteOpen = true
                        },
                        onJournalClick = {
                            currentTab = MainDestination.JOURNAL
                        },
                        onTaskClick = {
                            currentTab = MainDestination.TASKS
                        },
                        onViewAllNotes = {
                            currentTab = MainDestination.NOTES
                        },
                        onViewAllTasks = {
                            currentTab = MainDestination.TASKS
                        }
                    )
                    MainDestination.NOTES -> NotesScreen(
                        uiState = notesUiState,
                        onSearchChange = notesViewModel::onSearchQueryChange,
                        onCategorySelect = notesViewModel::onCategorySelect,
                        onToggleViewMode = notesViewModel::toggleViewMode,
                        onSetSortOrder = notesViewModel::setSortOrder,
                        onToggleFavoritesFilter = notesViewModel::toggleFavoritesFilter,
                        onToggleArchivedFilter = notesViewModel::toggleArchivedFilter,
                        onNoteClick = { note ->
                            editingNote = note
                            isEditingNoteOpen = true
                        },
                        onCreateNoteClick = {
                            editingNote = null
                            isEditingNoteOpen = true
                        },
                        onTogglePin = notesViewModel::togglePin,
                        onToggleFavorite = notesViewModel::toggleFavorite,
                        onToggleArchive = notesViewModel::toggleArchive,
                        onDuplicateNote = notesViewModel::duplicateNote,
                        onDeleteNote = notesViewModel::deleteNote,
                        onShareNote = notesViewModel::shareNote,
                        onExportNoteTxt = notesViewModel::exportNoteTxt,
                        onExportNotePdf = notesViewModel::exportNotePdf,
                        onAddCategory = notesViewModel::addCategory,
                        onDeleteCategory = notesViewModel::deleteCategory
                    )
                    MainDestination.JOURNAL -> JournalScreen(
                        uiState = journalUiState,
                        onDateSelect = journalViewModel::setSelectedDate,
                        onGoToToday = journalViewModel::goToToday,
                        onPreviousDay = journalViewModel::goToPreviousDay,
                        onNextDay = journalViewModel::goToNextDay,
                        onToggleViewMode = journalViewModel::toggleViewMode,
                        onSearchChange = journalViewModel::onSearchQueryChange,
                        onSaveJournal = journalViewModel::saveJournalEntry,
                        onDeleteJournal = journalViewModel::deleteJournal,
                        onToggleDailyReminder = journalViewModel::toggleDailyReminder,
                        onExportTxt = journalViewModel::exportJournalTxt,
                        onExportPdf = journalViewModel::exportJournalPdf
                    )
                    MainDestination.TASKS -> TasksScreen(
                        uiState = tasksUiState,
                        onFilterSelect = tasksViewModel::setFilter,
                        onCategorySelect = tasksViewModel::setCategory,
                        onSearchChange = tasksViewModel::onSearchQueryChange,
                        onToggleTask = tasksViewModel::toggleTask,
                        onSaveTask = tasksViewModel::saveTask,
                        onDeleteTask = tasksViewModel::deleteTask,
                        onExportTasksTxt = tasksViewModel::exportTasksTxt
                    )
                    MainDestination.SETTINGS -> SettingsScreen(
                        uiState = settingsUiState,
                        onSetThemeMode = settingsViewModel::setThemeMode,
                        onSetAutoLockTimeout = settingsViewModel::setAutoLockTimeout,
                        onSetDefaultNotesView = settingsViewModel::setDefaultNotesView,
                        onSetDefaultNotesSort = settingsViewModel::setDefaultNotesSort,
                        onSetDefaultNoteColor = settingsViewModel::setDefaultNoteColor,
                        onSetupPin = settingsViewModel::setupNewPin,
                        onDisablePin = settingsViewModel::disablePin,
                        onCreateBackup = settingsViewModel::createAndShareBackup,
                        onValidateAndRestoreBackup = settingsViewModel::validateAndRestoreBackup,
                        onApplyBackup = settingsViewModel::applyBackup,
                        onClearAllData = settingsViewModel::clearAllData,
                        onClearStatusMessage = settingsViewModel::clearStatusMessage
                    )
                }
            }
        }
    }
}

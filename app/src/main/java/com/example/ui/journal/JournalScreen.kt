package com.example.ui.journal

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JournalEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FormatToolbar
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val MOOD_LIST = listOf(
    "Happy" to "😊",
    "Excited" to "🤩",
    "Calm" to "😌",
    "Neutral" to "😐",
    "Sad" to "😔",
    "Anxious" to "😰",
    "Tired" to "🥱",
    "Grateful" to "🙏"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    uiState: JournalUiState,
    onDateSelect: (String) -> Unit,
    onGoToToday: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToggleViewMode: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSaveJournal: (id: Long, title: String, content: String, mood: String?, date: String, tags: String) -> Unit,
    onDeleteJournal: (JournalEntity) -> Unit,
    onToggleDailyReminder: (Context, Boolean, String) -> Unit,
    onExportTxt: (Context, JournalEntity) -> Unit,
    onExportPdf: (Context, JournalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingJournal by remember { mutableStateOf<JournalEntity?>(null) }
    var isEditorOpen by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isTimelineView) "Journal Timeline" else "Daily Diary",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Daily reminder button
                    IconButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.testTag("journal_reminder_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isReminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Daily Reminder",
                            tint = if (uiState.isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Calendar Picker Button
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }

                    // Toggle Timeline / Day View
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (uiState.isTimelineView) Icons.Default.CalendarMonth else Icons.Default.Timeline,
                            contentDescription = "Toggle Timeline"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_search_input"),
                placeholder = { Text("Search journals by title, mood, content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date Navigation Strip (shown when not in timeline view)
            if (!uiState.isTimelineView && uiState.searchQuery.isBlank()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPreviousDay) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showDatePickerDialog = true }
                        ) {
                            Text(
                                text = uiState.formattedDisplayDate,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to pick date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onGoToToday) {
                                Text("Today", fontSize = 12.sp)
                            }
                            IconButton(onClick = onNextDay) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Entries List
            val displayedEntries = if (uiState.searchQuery.isNotBlank()) {
                uiState.searchResults
            } else if (uiState.isTimelineView) {
                uiState.allEntries
            } else {
                uiState.entriesForDate
            }

            if (displayedEntries.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Book,
                    title = if (uiState.searchQuery.isNotBlank()) "No journal entries found" else "No entry for this day",
                    subtitle = if (uiState.searchQuery.isNotBlank()) "Try searching for a different keyword or mood" else "Capture today's highlights, thoughts, or feelings.",
                    buttonText = if (uiState.searchQuery.isBlank()) "Write Entry" else null,
                    onButtonClick = {
                        editingJournal = null
                        isEditorOpen = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedEntries, key = { it.id }) { journal ->
                        JournalEntryCard(
                            journal = journal,
                            onEdit = {
                                editingJournal = journal
                                isEditorOpen = true
                            },
                            onDelete = { onDeleteJournal(journal) },
                            onExportTxt = { onExportTxt(context, journal) },
                            onExportPdf = { onExportPdf(context, journal) }
                        )
                    }
                }
            }
        }

        // FAB to add journal entry
        FloatingActionButton(
            onClick = {
                editingJournal = null
                isEditorOpen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .testTag("create_journal_fab"),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Journal Entry")
        }

        // Calendar Date Picker Dialog
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            onDateSelect(selected)
                        }
                        showDatePickerDialog = false
                    }) {
                        Text("Select")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Journal Entry Editor Dialog
        if (isEditorOpen) {
            JournalEditorDialog(
                initialJournal = editingJournal,
                defaultDate = uiState.selectedDate,
                onDismiss = { isEditorOpen = false },
                onSave = { id, title, content, mood, date, tags ->
                    onSaveJournal(id, title, content, mood, date, tags)
                    isEditorOpen = false
                }
            )
        }

        // Daily Reminder Dialog
        if (showReminderDialog) {
            var reminderEnabled by remember { mutableStateOf(uiState.isReminderEnabled) }
            var reminderTime by remember { mutableStateOf(uiState.reminderTime) }

            AlertDialog(
                onDismissRequest = { showReminderDialog = false },
                title = { Text("Daily Writing Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable daily notification", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it }
                            )
                        }

                        if (reminderEnabled) {
                            OutlinedTextField(
                                value = reminderTime,
                                onValueChange = { reminderTime = it },
                                label = { Text("Time (24h e.g. 20:00)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onToggleDailyReminder(context, reminderEnabled, reminderTime)
                        showReminderDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReminderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    journal: JournalEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExportTxt: () -> Unit,
    onExportPdf: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_card_${journal.id}"),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (journal.mood != null) {
                        val emoji = MOOD_LIST.find { it.first == journal.mood }?.second ?: "😊"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = journal.title.ifBlank { "Daily Entry" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = journal.entryDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row {
                    IconButton(onClick = onExportPdf, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = journal.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (journal.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    journal.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEditorDialog(
    initialJournal: JournalEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, content: String, mood: String?, date: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf(initialJournal?.title ?: "") }
    var content by remember { mutableStateOf(initialJournal?.content ?: "") }
    var selectedMood by remember { mutableStateOf(initialJournal?.mood ?: "Happy") }
    var tags by remember { mutableStateOf(initialJournal?.tags ?: "") }
    var date by remember { mutableStateOf(initialJournal?.entryDate ?: defaultDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialJournal != null) "Edit Journal Entry" else "New Journal Entry") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_title_input")
                )

                // Mood Selector
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MOOD_LIST.forEach { (moodName, emoji) ->
                        val isSelected = selectedMood == moodName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = moodName },
                            label = { Text("$emoji $moodName", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (e.g. gratitude, travel, goals)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Format toolbar
                FormatToolbar(
                    onInsertText = { prefix, suffix ->
                        content = if (content.isEmpty()) "$prefix$suffix" else "$content\n$prefix$suffix"
                    }
                )

                // Content
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("journal_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank() || title.isNotBlank()) {
                        onSave(initialJournal?.id ?: 0L, title, content, selectedMood, date, tags)
                    }
                },
                modifier = Modifier.testTag("save_journal_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

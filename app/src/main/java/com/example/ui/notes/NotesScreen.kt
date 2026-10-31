package com.example.ui.notes

import android.content.Context
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    uiState: NotesUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onToggleViewMode: () -> Unit,
    onSetSortOrder: (NotesSort) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onToggleArchivedFilter: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onCreateNoteClick: () -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onToggleArchive: (NoteEntity) -> Unit,
    onDuplicateNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onShareNote: (Context, NoteEntity) -> Unit,
    onExportNoteTxt: (Context, NoteEntity) -> Unit,
    onExportNotePdf: (Context, NoteEntity) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf("#4361EE") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title & Action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isArchivedView) "Archived Notes" else if (uiState.isFavoritesView) "Favorites" else "All Notes",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sort Menu Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_notes_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Updated") },
                                onClick = { onSetSortOrder(NotesSort.RECENT_UPDATED); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently Created") },
                                onClick = { onSetSortOrder(NotesSort.RECENT_CREATED); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical (A-Z)") },
                                onClick = { onSetSortOrder(NotesSort.ALPHABETICAL); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = { onSetSortOrder(NotesSort.OLDEST); showSortMenu = false }
                            )
                        }
                    }

                    // Grid / List toggle
                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View"
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
                    .testTag("notes_search_input"),
                placeholder = { Text("Search by title, content, or tag...") },
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

            // Categories & Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedCategoryId == null && !uiState.isFavoritesView && !uiState.isArchivedView,
                    onClick = {
                        onCategorySelect(null)
                        if (uiState.isFavoritesView) onToggleFavoritesFilter()
                        if (uiState.isArchivedView) onToggleArchivedFilter()
                    },
                    label = { Text("All") },
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = uiState.isFavoritesView,
                    onClick = onToggleFavoritesFilter,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (uiState.isFavoritesView) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text("Favorites") },
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = uiState.isArchivedView,
                    onClick = onToggleArchivedFilter,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Archived") },
                    shape = RoundedCornerShape(12.dp)
                )

                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { onCategorySelect(category.id) },
                        label = { Text(category.name) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Add Category button chip
                FilterChip(
                    selected = false,
                    onClick = { showAddCategoryDialog = true },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp)) },
                    label = { Text("Add Category") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes List or Grid
            if (uiState.notes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = if (uiState.searchQuery.isNotBlank()) "No matching notes" else "No notes here",
                    subtitle = if (uiState.searchQuery.isNotBlank()) "Try searching for a different keyword or tag" else "Tap the '+' button to start writing your first note.",
                    buttonText = if (uiState.searchQuery.isBlank()) "Create Note" else null,
                    onButtonClick = onCreateNoteClick
                )
            } else if (uiState.isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            categories = uiState.categories,
                            onClick = { onNoteClick(note) },
                            onTogglePin = { onTogglePin(note) },
                            onToggleFavorite = { onToggleFavorite(note) },
                            onToggleArchive = { onToggleArchive(note) },
                            onDuplicate = { onDuplicateNote(note) },
                            onDelete = { onDeleteNote(note) },
                            onShare = { onShareNote(context, note) },
                            onExportPdf = { onExportNotePdf(context, note) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        NoteListItem(
                            note = note,
                            categories = uiState.categories,
                            onClick = { onNoteClick(note) },
                            onTogglePin = { onTogglePin(note) },
                            onToggleFavorite = { onToggleFavorite(note) },
                            onToggleArchive = { onToggleArchive(note) },
                            onDuplicate = { onDuplicateNote(note) },
                            onDelete = { onDeleteNote(note) },
                            onShare = { onShareNote(context, note) },
                            onExportPdf = { onExportNotePdf(context, note) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateNoteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .testTag("create_note_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Note")
        }

        // Add Category Dialog
        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("New Category") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onAddCategory(newCategoryName, newCategoryColor)
                                newCategoryName = ""
                                showAddCategoryDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    categories: List<CategoryEntity>,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val category = categories.find { it.id == note.categoryId }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(note.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.surface
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("note_card_${note.id}"),
        backgroundColor = bgColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = note.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (note.isPinned) "Unpin" else "Pin") },
                                onClick = { onTogglePin(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (note.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                                onClick = { onToggleFavorite(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { onDuplicate(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (note.isArchived) "Unarchive" else "Archive") },
                                onClick = { onToggleArchive(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = { onShare(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as PDF") },
                                onClick = { onExportPdf(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = { onDelete(); showMenu = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                if (category != null) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(note.updatedAt))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.5f)
                    )

                    Row {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (note.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = Color.Red,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(
    note: NoteEntity,
    categories: List<CategoryEntity>,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val category = categories.find { it.id == note.categoryId }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(note.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.surface
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_list_item_${note.id}"),
        backgroundColor = bgColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (note.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color.Red,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (category != null) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.updatedAt))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Black.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (note.isPinned) "Unpin" else "Pin") },
                        onClick = { onTogglePin(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (note.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                        onClick = { onToggleFavorite(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { onDuplicate(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (note.isArchived) "Unarchive" else "Archive") },
                        onClick = { onToggleArchive(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { onShare(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as PDF") },
                        onClick = { onExportPdf(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
        }
    }
}

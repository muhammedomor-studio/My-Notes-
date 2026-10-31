package com.example.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.FormatToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: NoteEntity?,
    categories: List<CategoryEntity>,
    onSave: (id: Long, title: String, content: String, categoryId: Long?, color: String, tags: String, isPinned: Boolean, isFavorite: Boolean) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onShare: (NoteEntity) -> Unit,
    onExportTxt: (NoteEntity) -> Unit,
    onExportPdf: (NoteEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var categoryId by remember { mutableStateOf(note?.categoryId) }
    var color by remember { mutableStateOf(note?.color ?: "#FFFFFF") }
    var tags by remember { mutableStateOf(note?.tags ?: "") }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    var isFavorite by remember { mutableStateOf(note?.isFavorite ?: false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.surface
    }

    val wordCount = if (content.isBlank()) 0 else content.trim().split(Regex("\\s+")).size
    val charCount = content.length

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("note_editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pin toggle
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    // Favorite toggle
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    if (note != null) {
                        IconButton(onClick = { onShare(note.copy(title = title, content = content)) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.Black.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = { onExportPdf(note.copy(title = title, content = content, tags = tags)) }) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = Color.Black.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = { onDelete(note); onBack() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                onSave(note?.id ?: 0L, title, content, categoryId, color, tags, isPinned, isFavorite)
                            }
                            onBack()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Save")
                    }
                }
            }

            // Category selector & Color palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    val currentCatName = categories.find { it.id == categoryId }?.name ?: "No Category"
                    FilterChip(
                        selected = categoryId != null,
                        onClick = { categoryMenuExpanded = true },
                        label = { Text(currentCatName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                categoryId = null
                                categoryMenuExpanded = false
                            }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    categoryId = cat.id
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                ColorPickerRow(
                    selectedColor = color,
                    onColorSelected = { color = it }
                )
            }

            // Tags input
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                placeholder = { Text("Tags (comma separated, e.g. work, ideas)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title input
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Title",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Format toolbar
            FormatToolbar(
                onInsertText = { prefix, suffix ->
                    content = if (content.isEmpty()) "$prefix$suffix" else "$content\n$prefix$suffix"
                }
            )

            // Content text field
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start writing your note here...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("note_content_input"),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black.copy(alpha = 0.9f)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Word and char counter footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val updatedDate = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(note?.updatedAt ?: System.currentTimeMillis()))
                Text(
                    text = "Updated: $updatedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
                Text(
                    text = "$wordCount words · $charCount chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    }
}

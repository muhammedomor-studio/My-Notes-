package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FormatToolbar(
    onInsertText: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val buttonColors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledTonalIconButton(
            onClick = { onInsertText("**", "**") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = { onInsertText("*", "*") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = { onInsertText("## ", "") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.Title, contentDescription = "Heading", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = { onInsertText("• ", "") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = { onInsertText("[ ] ", "") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.Checklist, contentDescription = "Checkbox", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = { onInsertText("> ", "") },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(18.dp))
        }

        FilledTonalIconButton(
            onClick = {
                val time = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
                onInsertText("[$time] ", "")
            },
            modifier = Modifier.size(38.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Timestamp", modifier = Modifier.size(18.dp))
        }
    }
}

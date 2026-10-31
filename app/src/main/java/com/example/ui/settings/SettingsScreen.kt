package com.example.ui.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupData
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.GlassCard

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSetThemeMode: (String) -> Unit,
    onSetAutoLockTimeout: (Long) -> Unit,
    onSetDefaultNotesView: (String) -> Unit,
    onSetDefaultNotesSort: (String) -> Unit,
    onSetDefaultNoteColor: (String) -> Unit,
    onSetupPin: (String) -> Unit,
    onDisablePin: (String, (Boolean) -> Unit) -> Unit,
    onCreateBackup: (Context) -> Unit,
    onValidateAndRestoreBackup: (String, (BackupData) -> Unit, (String) -> Unit) -> Unit,
    onApplyBackup: (BackupData) -> Unit,
    onClearAllData: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreDialog by remember { mutableStateOf<BackupData?>(null) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearStatusMessage()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. APPEARANCE
        item {
            SettingsSectionTitle("Appearance")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "System",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = uiState.themeMode == "SYSTEM",
                            onClick = { onSetThemeMode("SYSTEM") }
                        )
                        ThemeOptionChip(
                            label = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = uiState.themeMode == "LIGHT",
                            onClick = { onSetThemeMode("LIGHT") }
                        )
                        ThemeOptionChip(
                            label = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = uiState.themeMode == "DARK",
                            onClick = { onSetThemeMode("DARK") }
                        )
                    }
                }
            }
        }

        // 2. SECURITY & PRIVACY
        item {
            SettingsSectionTitle("Security & Privacy")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PIN Lock Protection",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.isPinEnabled) "App is protected with a 4–6 digit PIN" else "Require PIN code to open app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = uiState.isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinSetupDialog = true
                                } else {
                                    showDisablePinDialog = true
                                }
                            },
                            modifier = Modifier.testTag("pin_lock_toggle")
                        )
                    }

                    if (uiState.isPinEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Lock Timeout",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = uiState.autoLockTimeout == 0L,
                                    onClick = { onSetAutoLockTimeout(0L) },
                                    label = { Text("Immediately", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = uiState.autoLockTimeout == 60000L,
                                    onClick = { onSetAutoLockTimeout(60000L) },
                                    label = { Text("1 min", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = uiState.autoLockTimeout == 300000L,
                                    onClick = { onSetAutoLockTimeout(300000L) },
                                    label = { Text("5 min", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { showPinSetupDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Change PIN")
                        }
                    }
                }
            }
        }

        // 3. NOTES PREFERENCES
        item {
            SettingsSectionTitle("Notes Preferences")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Default View Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = uiState.defaultNotesView == "GRID",
                                onClick = { onSetDefaultNotesView("GRID") },
                                label = { Text("Grid") }
                            )
                            FilterChip(
                                selected = uiState.defaultNotesView == "LIST",
                                onClick = { onSetDefaultNotesView("LIST") },
                                label = { Text("List") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Default Sorting",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = uiState.defaultNotesSort == "RECENT_UPDATED",
                                onClick = { onSetDefaultNotesSort("RECENT_UPDATED") },
                                label = { Text("Updated", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = uiState.defaultNotesSort == "RECENT_CREATED",
                                onClick = { onSetDefaultNotesSort("RECENT_CREATED") },
                                label = { Text("Created", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Default Note Background",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorPickerRow(
                        selectedColor = uiState.defaultNoteColor,
                        onColorSelected = onSetDefaultNoteColor
                    )
                }
            }
        }

        // 4. DATA MANAGEMENT & BACKUP
        item {
            SettingsSectionTitle("Data & Backup")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsActionRow(
                        title = "Create Full Backup",
                        subtitle = "Export all notes, journals, tasks to JSON",
                        icon = Icons.Default.Download,
                        onClick = { onCreateBackup(context) },
                        testTag = "create_backup_button"
                    )

                    SettingsActionRow(
                        title = "Restore from Backup",
                        subtitle = "Import data from a backup JSON file",
                        icon = Icons.Default.Upload,
                        onClick = { showRestoreDialog = true },
                        testTag = "restore_backup_button"
                    )

                    SettingsActionRow(
                        title = "Clear All Data",
                        subtitle = "Permanently erase all notes and tasks on device",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showClearDataDialog = true },
                        isDestructive = true,
                        testTag = "clear_all_data_button"
                    )
                }
            }
        }

        // 5. ABOUT
        item {
            SettingsSectionTitle("About")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsActionRow(
                        title = "My Notes",
                        subtitle = "Version 1.0 (Build 1) · 100% Offline & Private",
                        icon = Icons.Default.Info,
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinSetupDialog) {
        var pin1 by remember { mutableStateOf("") }
        var pin2 by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text("Set Up Application PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Important: Forgotten PINs cannot be recovered since data is kept strictly on your local device without any remote accounts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedTextField(
                        value = pin1,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin1 = it },
                        label = { Text("Enter 4–6 digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pin2,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin2 = it },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pin1.length < 4) {
                        error = "PIN must be at least 4 digits"
                    } else if (pin1 != pin2) {
                        error = "PINs do not match"
                    } else {
                        onSetupPin(pin1)
                        showPinSetupDialog = false
                    }
                }) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Disable PIN Dialog
    if (showDisablePinDialog) {
        var enteredPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDisablePinDialog = false },
            title = { Text("Disable PIN Protection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Please enter your current PIN to confirm removal.")
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) enteredPin = it },
                        label = { Text("Current PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onDisablePin(enteredPin) { success ->
                        if (success) {
                            showDisablePinDialog = false
                        } else {
                            error = "Incorrect PIN"
                        }
                    }
                }) {
                    Text("Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisablePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        var jsonText by remember { mutableStateOf("") }
        var parseError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Data from Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste your backup JSON text below:")
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        label = { Text("Backup JSON") }
                    )
                    if (parseError != null) {
                        Text(text = parseError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onValidateAndRestoreBackup(
                        jsonText,
                        { data ->
                            showRestoreDialog = false
                            showConfirmRestoreDialog = data
                        },
                        { err -> parseError = err }
                    )
                }) {
                    Text("Validate & Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Restore Dialog
    showConfirmRestoreDialog?.let { data ->
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = null },
            title = { Text("Confirm Restore") },
            text = {
                Text(
                    "Found:\n• ${data.notes.size} Notes\n• ${data.journals.size} Journal entries\n• ${data.tasks.size} Tasks\n\nDo you want to import this data into your app?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    onApplyBackup(data)
                    showConfirmRestoreDialog = null
                }) {
                    Text("Import Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = {
                Text(
                    "This will permanently delete all notes, diaries, and tasks from your device. This operation cannot be undone unless you created a backup.",
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About My Notes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("My Notes is an offline-first, native Android application engineered for complete privacy, speed, and reliability.")
                    Text("• Zero tracking, ads, or analytics")
                    Text("• 100% on-device SQLite & DataStore storage")
                    Text("• Secure salted cryptographic PIN protection")
                    Text("• Complete TXT, PDF, and JSON offline export")
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ThemeOptionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDestructive) Color.Red else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

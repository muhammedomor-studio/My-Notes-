package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_notes_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        val KEY_AUTO_LOCK_TIMEOUT = longPreferencesKey("auto_lock_timeout") // in millis
        val KEY_DEFAULT_NOTES_VIEW = stringPreferencesKey("default_notes_view") // GRID, LIST
        val KEY_DEFAULT_NOTES_SORT = stringPreferencesKey("default_notes_sort") // RECENT_UPDATED, RECENT_CREATED, ALPHABETICAL, OLDEST
        val KEY_DEFAULT_NOTE_COLOR = stringPreferencesKey("default_note_color")
        val KEY_JOURNAL_REMINDER = booleanPreferencesKey("journal_reminder")
        val KEY_JOURNAL_REMINDER_TIME = stringPreferencesKey("journal_reminder_time")
        val KEY_LAST_BACKGROUND_TIME = longPreferencesKey("last_background_time")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "SYSTEM"
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_ENABLED] ?: false
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_HASH]
    }

    val pinSalt: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_SALT]
    }

    val autoLockTimeout: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_LOCK_TIMEOUT] ?: 0L // 0 = immediately
    }

    val defaultNotesView: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_NOTES_VIEW] ?: "GRID"
    }

    val defaultNotesSort: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_NOTES_SORT] ?: "RECENT_UPDATED"
    }

    val defaultNoteColor: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_NOTE_COLOR] ?: "#FFFFFF"
    }

    val isJournalReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_JOURNAL_REMINDER] ?: false
    }

    val journalReminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_JOURNAL_REMINDER_TIME] ?: "20:00"
    }

    suspend fun setThemeMode(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setPin(hash: String, salt: String) {
        context.dataStore.edit {
            it[KEY_PIN_HASH] = hash
            it[KEY_PIN_SALT] = salt
            it[KEY_PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[KEY_PIN_ENABLED] = false
            it.remove(KEY_PIN_HASH)
            it.remove(KEY_PIN_SALT)
        }
    }

    suspend fun setAutoLockTimeout(timeoutMillis: Long) {
        context.dataStore.edit { it[KEY_AUTO_LOCK_TIMEOUT] = timeoutMillis }
    }

    suspend fun setDefaultNotesView(view: String) {
        context.dataStore.edit { it[KEY_DEFAULT_NOTES_VIEW] = view }
    }

    suspend fun setDefaultNotesSort(sort: String) {
        context.dataStore.edit { it[KEY_DEFAULT_NOTES_SORT] = sort }
    }

    suspend fun setDefaultNoteColor(color: String) {
        context.dataStore.edit { it[KEY_DEFAULT_NOTE_COLOR] = color }
    }

    suspend fun setJournalReminder(enabled: Boolean, time: String) {
        context.dataStore.edit {
            it[KEY_JOURNAL_REMINDER] = enabled
            it[KEY_JOURNAL_REMINDER_TIME] = time
        }
    }

    suspend fun setLastBackgroundTime(timestamp: Long) {
        context.dataStore.edit { it[KEY_LAST_BACKGROUND_TIME] = timestamp }
    }

    val lastBackgroundTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_BACKGROUND_TIME] ?: 0L
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}

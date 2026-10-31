package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.AppPreferences
import com.example.data.repository.JournalRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.TaskRepository
import com.example.notifications.NotificationHelper

class MyNotesApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val notesRepository by lazy { NotesRepository(database.noteDao(), database.categoryDao()) }
    val journalRepository by lazy { JournalRepository(database.journalDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val preferences by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}

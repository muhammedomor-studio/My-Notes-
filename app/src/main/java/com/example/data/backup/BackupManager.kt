package com.example.data.backup

import android.content.Context
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.JournalRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.TaskRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val version: Int,
    val createdAt: Long,
    val notes: List<NoteEntity>,
    val categories: List<CategoryEntity>,
    val journals: List<JournalEntity>,
    val tasks: List<TaskEntity>
)

object BackupManager {

    private const val BACKUP_VERSION = 1

    suspend fun createBackupJson(
        notesRepository: NotesRepository,
        journalRepository: JournalRepository,
        taskRepository: TaskRepository
    ): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("createdAt", System.currentTimeMillis())
        root.put("appName", "My Notes")

        // Categories
        val categories = notesRepository.getAllCategoriesSync()
        val catArray = JSONArray()
        for (cat in categories) {
            val cObj = JSONObject()
            cObj.put("id", cat.id)
            cObj.put("name", cat.name)
            cObj.put("color", cat.color)
            cObj.put("createdAt", cat.createdAt)
            catArray.put(cObj)
        }
        root.put("categories", catArray)

        // Notes
        val notes = notesRepository.getAllNotesSync()
        val noteArray = JSONArray()
        for (note in notes) {
            val nObj = JSONObject()
            nObj.put("id", note.id)
            nObj.put("title", note.title)
            nObj.put("content", note.content)
            if (note.categoryId != null) nObj.put("categoryId", note.categoryId)
            nObj.put("color", note.color)
            nObj.put("isPinned", note.isPinned)
            nObj.put("isFavorite", note.isFavorite)
            nObj.put("isArchived", note.isArchived)
            nObj.put("tags", note.tags)
            nObj.put("createdAt", note.createdAt)
            nObj.put("updatedAt", note.updatedAt)
            noteArray.put(nObj)
        }
        root.put("notes", noteArray)

        // Journals
        val journals = journalRepository.getAllJournalsSync()
        val jArray = JSONArray()
        for (j in journals) {
            val jObj = JSONObject()
            jObj.put("id", j.id)
            jObj.put("title", j.title)
            jObj.put("content", j.content)
            if (j.mood != null) jObj.put("mood", j.mood)
            jObj.put("entryDate", j.entryDate)
            jObj.put("tags", j.tags)
            jObj.put("createdAt", j.createdAt)
            jObj.put("updatedAt", j.updatedAt)
            jArray.put(jObj)
        }
        root.put("journals", jArray)

        // Tasks
        val tasks = taskRepository.getAllTasksSync()
        val tArray = JSONArray()
        for (t in tasks) {
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("title", t.title)
            tObj.put("description", t.description)
            tObj.put("priority", t.priority)
            if (t.dueDate != null) tObj.put("dueDate", t.dueDate)
            if (t.dueTime != null) tObj.put("dueTime", t.dueTime)
            tObj.put("isCompleted", t.isCompleted)
            if (t.categoryId != null) tObj.put("categoryId", t.categoryId)
            tObj.put("hasReminder", t.hasReminder)
            tObj.put("createdAt", t.createdAt)
            tObj.put("updatedAt", t.updatedAt)
            tArray.put(tObj)
        }
        root.put("tasks", tArray)

        return root.toString(2)
    }

    fun exportBackupFile(context: Context, jsonString: String): File {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "MyNotes_Backup_$dateStr.json")
        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun parseAndValidateBackup(jsonString: String): Result<BackupData> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("version")) {
                return Result.failure(IllegalArgumentException("Invalid backup: Missing version field"))
            }

            val version = root.optInt("version", 1)
            val createdAt = root.optLong("createdAt", System.currentTimeMillis())

            val categories = mutableListOf<CategoryEntity>()
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until catArray.length()) {
                val obj = catArray.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "Untitled"),
                        color = obj.optString("color", "#4361EE"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val notes = mutableListOf<NoteEntity>()
            val noteArray = root.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until noteArray.length()) {
                val obj = noteArray.getJSONObject(i)
                notes.add(
                    NoteEntity(
                        id = 0L, // Reset ID for clean insertion without conflict
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        categoryId = if (obj.has("categoryId")) obj.getLong("categoryId") else null,
                        color = obj.optString("color", "#FFFFFF"),
                        isPinned = obj.optBoolean("isPinned", false),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isArchived = obj.optBoolean("isArchived", false),
                        tags = obj.optString("tags", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val journals = mutableListOf<JournalEntity>()
            val jArray = root.optJSONArray("journals") ?: JSONArray()
            for (i in 0 until jArray.length()) {
                val obj = jArray.getJSONObject(i)
                journals.add(
                    JournalEntity(
                        id = 0L,
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        mood = if (obj.has("mood")) obj.getString("mood") else null,
                        entryDate = obj.optString("entryDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())),
                        tags = obj.optString("tags", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val tasks = mutableListOf<TaskEntity>()
            val tArray = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tArray.length()) {
                val obj = tArray.getJSONObject(i)
                tasks.add(
                    TaskEntity(
                        id = 0L,
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        priority = obj.optString("priority", "MEDIUM"),
                        dueDate = if (obj.has("dueDate")) obj.getLong("dueDate") else null,
                        dueTime = if (obj.has("dueTime")) obj.getString("dueTime") else null,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        categoryId = if (obj.has("categoryId")) obj.getLong("categoryId") else null,
                        hasReminder = obj.optBoolean("hasReminder", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            Result.success(
                BackupData(
                    version = version,
                    createdAt = createdAt,
                    notes = notes,
                    categories = categories,
                    journals = journals,
                    tasks = tasks
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(
        backupData: BackupData,
        notesRepository: NotesRepository,
        journalRepository: JournalRepository,
        taskRepository: TaskRepository
    ) {
        if (backupData.notes.isNotEmpty()) {
            notesRepository.insertAllNotes(backupData.notes)
        }
        if (backupData.journals.isNotEmpty()) {
            journalRepository.insertAllJournals(backupData.journals)
        }
        if (backupData.tasks.isNotEmpty()) {
            taskRepository.insertAllTasks(backupData.tasks)
        }
    }
}

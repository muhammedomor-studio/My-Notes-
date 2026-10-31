package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao
) {
    val allActiveNotes: Flow<List<NoteEntity>> = noteDao.getAllActiveNotes()
    val pinnedNotes: Flow<List<NoteEntity>> = noteDao.getPinnedNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val noteCount: Flow<Int> = noteDao.getActiveNoteCount()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    fun getNotesByCategory(categoryId: Long): Flow<List<NoteEntity>> = noteDao.getNotesByCategory(categoryId)

    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?> = noteDao.getNoteByIdFlow(id)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteById(id)

    suspend fun togglePin(note: NoteEntity) {
        noteDao.update(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(note: NoteEntity) {
        noteDao.update(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchive(note: NoteEntity) {
        noteDao.update(note.copy(isArchived = !note.isArchived, isPinned = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun duplicateNote(note: NoteEntity): Long {
        val copy = note.copy(
            id = 0L,
            title = "${note.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return noteDao.insert(copy)
    }

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun getAllNotesSync(): List<NoteEntity> = noteDao.getAllNotesSync()

    suspend fun getAllCategoriesSync(): List<CategoryEntity> = categoryDao.getAllCategoriesSync()

    suspend fun insertAllNotes(notes: List<NoteEntity>) = noteDao.insertAll(notes)

    suspend fun insertAllCategories(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)

    suspend fun deleteAllData() {
        noteDao.deleteAll()
        categoryDao.deleteAll()
    }
}

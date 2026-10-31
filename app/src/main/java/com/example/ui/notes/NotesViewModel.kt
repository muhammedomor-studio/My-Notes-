package com.example.ui.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.ExportUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotesSort {
    RECENT_UPDATED,
    RECENT_CREATED,
    ALPHABETICAL,
    OLDEST
}

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val sortOrder: NotesSort = NotesSort.RECENT_UPDATED,
    val isArchivedView: Boolean = false,
    val isFavoritesView: Boolean = false
)

class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _isGridView = MutableStateFlow(true)
    private val _sortOrder = MutableStateFlow(NotesSort.RECENT_UPDATED)
    private val _isArchivedView = MutableStateFlow(false)
    private val _isFavoritesView = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val defaultView = preferences.defaultNotesView.first()
            _isGridView.value = defaultView == "GRID"

            val defaultSort = preferences.defaultNotesSort.first()
            _sortOrder.value = try {
                NotesSort.valueOf(defaultSort)
            } catch (_: Exception) {
                NotesSort.RECENT_UPDATED
            }
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        notesRepository.allActiveNotes,
        notesRepository.archivedNotes,
        notesRepository.allCategories,
        _searchQuery,
        _selectedCategoryId,
        _selectedTag,
        _isGridView,
        _sortOrder,
        _isArchivedView,
        _isFavoritesView
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val activeNotes = args[0] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val archivedNotes = args[1] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = args[2] as List<CategoryEntity>
        val query = args[3] as String
        val categoryId = args[4] as Long?
        val tag = args[5] as String?
        val isGrid = args[6] as Boolean
        val sort = args[7] as NotesSort
        val isArchived = args[8] as Boolean
        val isFavorites = args[9] as Boolean

        val baseList = if (isArchived) archivedNotes else activeNotes

        var filtered = baseList.filter { note ->
            val matchesQuery = if (query.isBlank()) true else {
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true) ||
                        note.tags.contains(query, ignoreCase = true)
            }
            val matchesCategory = if (categoryId == null) true else note.categoryId == categoryId
            val matchesTag = if (tag == null) true else note.tags.split(",").map { it.trim() }.contains(tag)
            val matchesFavorites = if (!isFavorites) true else note.isFavorite

            matchesQuery && matchesCategory && matchesTag && matchesFavorites
        }

        filtered = when (sort) {
            NotesSort.RECENT_UPDATED -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })
            NotesSort.RECENT_CREATED -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.createdAt })
            NotesSort.ALPHABETICAL -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenBy { it.title.lowercase() })
            NotesSort.OLDEST -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenBy { it.createdAt })
        }

        NotesUiState(
            notes = filtered,
            categories = categories,
            selectedCategoryId = categoryId,
            selectedTag = tag,
            searchQuery = query,
            isGridView = isGrid,
            sortOrder = sort,
            isArchivedView = isArchived,
            isFavoritesView = isFavorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun onTagSelect(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun toggleViewMode() {
        val newView = !_isGridView.value
        _isGridView.value = newView
        viewModelScope.launch {
            preferences.setDefaultNotesView(if (newView) "GRID" else "LIST")
        }
    }

    fun setSortOrder(sort: NotesSort) {
        _sortOrder.value = sort
        viewModelScope.launch {
            preferences.setDefaultNotesSort(sort.name)
        }
    }

    fun toggleArchivedFilter() {
        _isArchivedView.value = !_isArchivedView.value
        if (_isArchivedView.value) _isFavoritesView.value = false
    }

    fun toggleFavoritesFilter() {
        _isFavoritesView.value = !_isFavoritesView.value
        if (_isFavoritesView.value) _isArchivedView.value = false
    }

    fun saveNote(
        id: Long = 0L,
        title: String,
        content: String,
        categoryId: Long?,
        color: String,
        tags: String,
        isPinned: Boolean,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                notesRepository.insertNote(
                    NoteEntity(
                        title = title.trim(),
                        content = content.trim(),
                        categoryId = categoryId,
                        color = color,
                        tags = tags.trim(),
                        isPinned = isPinned,
                        isFavorite = isFavorite,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                val existing = notesRepository.getNoteById(id)
                if (existing != null) {
                    notesRepository.updateNote(
                        existing.copy(
                            title = title.trim(),
                            content = content.trim(),
                            categoryId = categoryId,
                            color = color,
                            tags = tags.trim(),
                            isPinned = isPinned,
                            isFavorite = isFavorite,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.togglePin(note)
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.toggleFavorite(note)
        }
    }

    fun toggleArchive(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.toggleArchive(note)
        }
    }

    fun duplicateNote(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.duplicateNote(note)
        }
    }

    fun addCategory(name: String, color: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            notesRepository.insertCategory(
                CategoryEntity(name = name.trim(), color = color)
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            notesRepository.deleteCategory(category)
        }
    }

    fun shareNote(context: Context, note: NoteEntity) {
        ExportUtils.shareText(context, note.title, note.content)
    }

    fun exportNoteTxt(context: Context, note: NoteEntity) {
        val file = ExportUtils.exportNoteAsTxt(context, note)
        ExportUtils.shareFile(context, file, "text/plain")
    }

    fun exportNotePdf(context: Context, note: NoteEntity) {
        val file = ExportUtils.exportNoteAsPdf(context, note)
        ExportUtils.shareFile(context, file, "application/pdf")
    }

    class Factory(
        private val notesRepository: NotesRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(notesRepository, preferences) as T
        }
    }
}

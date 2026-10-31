package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteEntity::class,
        CategoryEntity::class,
        JournalEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun journalDao(): JournalDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_notes.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default categories
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultCategories = listOf(
                                CategoryEntity(name = "Personal", color = "#4361EE"),
                                CategoryEntity(name = "Work", color = "#3A0CA3"),
                                CategoryEntity(name = "Study", color = "#7209B7"),
                                CategoryEntity(name = "Ideas", color = "#4CC9F0"),
                                CategoryEntity(name = "Important", color = "#F72585")
                            )
                            getInstance(context).categoryDao().insertAll(defaultCategories)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

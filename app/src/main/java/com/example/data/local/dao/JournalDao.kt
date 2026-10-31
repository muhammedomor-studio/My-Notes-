package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journals ORDER BY entryDate DESC, createdAt DESC")
    fun getAllJournals(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journals WHERE entryDate = :entryDate ORDER BY createdAt DESC")
    fun getJournalsByDate(entryDate: String): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journals WHERE entryDate = :entryDate LIMIT 1")
    suspend fun getJournalForDateSync(entryDate: String): JournalEntity?

    @Query("SELECT * FROM journals WHERE id = :id LIMIT 1")
    suspend fun getJournalById(id: Long): JournalEntity?

    @Query("SELECT * FROM journals WHERE id = :id LIMIT 1")
    fun getJournalByIdFlow(id: Long): Flow<JournalEntity?>

    @Query("""
        SELECT * FROM journals 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'
        ORDER BY entryDate DESC, createdAt DESC
    """)
    fun searchJournals(query: String): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: JournalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(journals: List<JournalEntity>)

    @Update
    suspend fun update(journal: JournalEntity)

    @Delete
    suspend fun delete(journal: JournalEntity)

    @Query("DELETE FROM journals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM journals")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM journals")
    fun getJournalCount(): Flow<Int>

    @Query("SELECT * FROM journals")
    suspend fun getAllJournalsSync(): List<JournalEntity>
}

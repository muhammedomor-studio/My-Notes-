package com.example.data.repository

import com.example.data.local.dao.JournalDao
import com.example.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalDao: JournalDao
) {
    val allJournals: Flow<List<JournalEntity>> = journalDao.getAllJournals()
    val journalCount: Flow<Int> = journalDao.getJournalCount()

    fun getJournalsByDate(date: String): Flow<List<JournalEntity>> = journalDao.getJournalsByDate(date)

    suspend fun getJournalForDateSync(date: String): JournalEntity? = journalDao.getJournalForDateSync(date)

    fun getJournalByIdFlow(id: Long): Flow<JournalEntity?> = journalDao.getJournalByIdFlow(id)

    suspend fun getJournalById(id: Long): JournalEntity? = journalDao.getJournalById(id)

    fun searchJournals(query: String): Flow<List<JournalEntity>> = journalDao.searchJournals(query)

    suspend fun insertJournal(journal: JournalEntity): Long = journalDao.insert(journal)

    suspend fun updateJournal(journal: JournalEntity) = journalDao.update(journal)

    suspend fun deleteJournal(journal: JournalEntity) = journalDao.delete(journal)

    suspend fun deleteJournalById(id: Long) = journalDao.deleteById(id)

    suspend fun getAllJournalsSync(): List<JournalEntity> = journalDao.getAllJournalsSync()

    suspend fun insertAllJournals(journals: List<JournalEntity>) = journalDao.insertAll(journals)

    suspend fun deleteAllJournals() = journalDao.deleteAll()
}

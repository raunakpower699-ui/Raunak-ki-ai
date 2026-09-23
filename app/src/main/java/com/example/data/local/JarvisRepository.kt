package com.example.data.local

import com.example.data.model.JarvisLog
import com.example.data.model.JarvisReminder
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val dao: JarvisDao) {
    val allLogs: Flow<List<JarvisLog>> = dao.getAllLogs()
    val allReminders: Flow<List<JarvisReminder>> = dao.getAllReminders()
    val allVaultItems: Flow<List<VaultItem>> = dao.getAllVaultItems()

    suspend fun logCommand(
        query: String,
        response: String,
        actionType: String,
        status: String
    ): Long {
        return dao.insertLog(
            JarvisLog(
                query = query,
                response = response,
                actionType = actionType,
                status = status
            )
        )
    }

    suspend fun clearLogs() = dao.clearLogs()

    suspend fun addReminder(title: String, dueTimeText: String, category: String): Long {
        return dao.insertReminder(
            JarvisReminder(
                title = title,
                dueTimeText = dueTimeText,
                category = category
            )
        )
    }

    suspend fun toggleReminder(reminder: JarvisReminder) {
        dao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) = dao.deleteReminderById(id)

    suspend fun addVaultItem(title: String, category: String, secret: String, note: String): Long {
        return dao.insertVaultItem(
            VaultItem(
                title = title,
                category = category,
                secretValue = secret,
                note = note
            )
        )
    }

    suspend fun deleteVaultItem(id: Long) = dao.deleteVaultItemById(id)
}

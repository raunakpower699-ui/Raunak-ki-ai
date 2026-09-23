package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JarvisLog
import com.example.data.model.JarvisReminder
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Command logs
    @Query("SELECT * FROM jarvis_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<JarvisLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JarvisLog): Long

    @Query("DELETE FROM jarvis_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM jarvis_logs")
    suspend fun clearLogs()

    // Reminders
    @Query("SELECT * FROM jarvis_reminders ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<JarvisReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: JarvisReminder): Long

    @Update
    suspend fun updateReminder(reminder: JarvisReminder)

    @Query("DELETE FROM jarvis_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    // Vault
    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemById(id: Long)
}

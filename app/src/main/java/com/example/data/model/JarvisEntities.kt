package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_logs")
data class JarvisLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val response: String,
    val actionType: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jarvis_reminders")
data class JarvisReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dueTimeText: String,
    val category: String = "GENERAL",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val secretValue: String,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

enum class OrbMood {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ALERT
}

data class SystemStatusState(
    val batteryPct: Int = 84,
    val isCharging: Boolean = false,
    val isTorchOn: Boolean = false,
    val wifiConnected: Boolean = true,
    val bluetoothConnected: Boolean = true,
    val dndActive: Boolean = false,
    val volumeLevelPct: Int = 75,
    val privacyShieldActive: Boolean = true,
    val activeAppProtectionCount: Int = 18
)

data class ProactiveAlert(
    val id: String,
    val title: String,
    val messageHinglish: String,
    val actionLabel: String,
    val commandToExecute: String,
    val iconType: String
)

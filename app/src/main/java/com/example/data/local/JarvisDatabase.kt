package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.JarvisLog
import com.example.data.model.JarvisReminder
import com.example.data.model.VaultItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [JarvisLog::class, JarvisReminder::class, VaultItem::class],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_ultimate_database"
                )
                    .addCallback(JarvisDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class JarvisDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.jarvisDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: JarvisDao) {
                // Pre-populate with realistic initial state
                dao.insertLog(
                    JarvisLog(
                        query = "System Initialized",
                        response = "Online boss! JARVIS ULTIMATE ready with Maximum System Access.",
                        actionType = "SYSTEM",
                        status = "SUCCESS",
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
                dao.insertReminder(
                    JarvisReminder(
                        title = "Doctor appointment book karna",
                        dueTimeText = "Tomorrow, 10:00 AM",
                        category = "HEALTH",
                        isCompleted = false
                    )
                )
                dao.insertReminder(
                    JarvisReminder(
                        title = "Mom ko call karna for dinner plans",
                        dueTimeText = "Tonight, 8:00 PM",
                        category = "PERSONAL",
                        isCompleted = false
                    )
                )
                dao.insertVaultItem(
                    VaultItem(
                        title = "Banking Proxy Biometric Key",
                        category = "PASSWORD",
                        secretValue = "AES256::7f920da832...#PROXY_ACTIVE",
                        note = "Hardware secure enclave simulated token"
                    )
                )
            }
        }
    }
}

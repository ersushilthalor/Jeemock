package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Question
import com.example.data.model.TestSession

@Database(entities = [Question::class, TestSession::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class JeeDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun testSessionDao(): TestSessionDao

    companion object {
        @Volatile
        private var INSTANCE: JeeDatabase? = null

        fun getDatabase(context: Context): JeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JeeDatabase::class.java,
                    "jee_mock_test_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

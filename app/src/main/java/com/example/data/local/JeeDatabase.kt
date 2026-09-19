package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActiveExamState
import com.example.data.model.Question
import com.example.data.model.TestSession

@Database(entities = [Question::class, TestSession::class, ActiveExamState::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class JeeDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun testSessionDao(): TestSessionDao
    abstract fun activeExamDao(): ActiveExamDao

    companion object {
        @Volatile
        private var INSTANCE: JeeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add numericalTolerance to questions table
                db.execSQL("ALTER TABLE questions ADD COLUMN numericalTolerance REAL NOT NULL DEFAULT 0.0")

                // Add analytics and breakdown columns to test_sessions
                db.execSQL("ALTER TABLE test_sessions ADD COLUMN percentage REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE test_sessions ADD COLUMN subjectBreakdownJson TEXT")
                db.execSQL("ALTER TABLE test_sessions ADD COLUMN chapterBreakdownJson TEXT")
                db.execSQL("ALTER TABLE test_sessions ADD COLUMN topicBreakdownJson TEXT")

                // Create active_exam_state table for continuous test persistence
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS active_exam_state (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        source TEXT NOT NULL,
                        examPattern TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        targetEndTimeMillis INTEGER NOT NULL,
                        currentQuestionIndex INTEGER NOT NULL,
                        activeSectionFilter TEXT,
                        attemptsJson TEXT NOT NULL,
                        isInProgress INTEGER NOT NULL,
                        lastUpdatedMillis INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): JeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JeeDatabase::class.java,
                    "jee_mock_test_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

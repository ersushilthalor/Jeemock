package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExamPattern
import com.example.data.model.Question
import com.example.data.model.Subject
import com.example.data.model.TestSession
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Query("SELECT * FROM questions WHERE isGenuinePyq = 1")
    suspend fun getGenuinePyqs(): List<Question>

    @Query("SELECT * FROM questions WHERE subject = :subject AND isGenuinePyq = 1")
    suspend fun getGenuinePyqsBySubject(subject: Subject): List<Question>

    @Query("SELECT * FROM questions WHERE subject = :subject AND chapter = :chapter")
    suspend fun getQuestionsByChapter(subject: Subject, chapter: String): List<Question>

    @Query("SELECT * FROM questions WHERE examPattern = :pattern AND isGenuinePyq = 1")
    suspend fun getGenuinePyqsByPattern(pattern: ExamPattern): List<Question>

    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions WHERE isGenuinePyq = 1")
    suspend fun getGenuinePyqCount(): Int

    @Query("SELECT DISTINCT chapter FROM questions WHERE subject = :subject")
    suspend fun getChaptersForSubject(subject: Subject): List<String>
}

@Dao
interface TestSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TestSession)

    @Update
    suspend fun updateSession(session: TestSession)

    @Query("SELECT * FROM test_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TestSession>>

    @Query("SELECT * FROM test_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): TestSession?

    @Query("SELECT * FROM test_sessions WHERE isCompleted = 1 ORDER BY timestamp DESC LIMIT 10")
    fun getRecentCompletedSessions(): Flow<List<TestSession>>
}

@Dao
interface ActiveExamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveExam(state: com.example.data.model.ActiveExamState)

    @Query("SELECT * FROM active_exam_state WHERE id = 'ACTIVE_TEST' LIMIT 1")
    suspend fun getActiveExam(): com.example.data.model.ActiveExamState?

    @Query("DELETE FROM active_exam_state WHERE id = 'ACTIVE_TEST'")
    suspend fun clearActiveExam()
}


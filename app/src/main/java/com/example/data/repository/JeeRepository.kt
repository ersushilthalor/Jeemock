package com.example.data.repository

import android.content.Context
import com.example.data.local.GenuinePyqBank
import com.example.data.local.JeeDatabase
import com.example.data.model.Difficulty
import com.example.data.model.ExamPattern
import com.example.data.model.Question
import com.example.data.model.QuestionAttempt
import com.example.data.model.Subject
import com.example.data.model.TestSession
import com.example.data.model.TestSource
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class JeeRepository(
    private val context: Context,
    private val database: JeeDatabase
) {
    private val questionDao = database.questionDao()
    private val testSessionDao = database.testSessionDao()
    private val activeExamDao = database.activeExamDao()

    suspend fun initializeDatabaseIfNeeded() {
        val count = questionDao.getGenuinePyqCount()
        if (count == 0) {
            questionDao.insertAll(GenuinePyqBank.questions)
        }
    }

    suspend fun getGenuinePyqTest(
        pattern: ExamPattern = ExamPattern.JEE_MAIN,
        subject: Subject? = null,
        limit: Int = 10
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val allPyqs = if (subject != null) {
            questionDao.getGenuinePyqsBySubject(subject)
        } else {
            questionDao.getGenuinePyqs()
        }
        return allPyqs.shuffled().take(limit)
    }

    suspend fun generateAiTest(
        pattern: ExamPattern = ExamPattern.JEE_MAIN,
        subject: Subject?,
        chapters: List<String>,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val existingQuestions = questionDao.getGenuinePyqs()
        val signatures = existingQuestions.map { it.textEn }.toSet()

        val generated = GeminiService.generatePersonalizedQuestions(
            context = context,
            pattern = ExamPattern.JEE_MAIN,
            subject = subject,
            chapters = chapters,
            difficulty = difficulty,
            count = count,
            existingQuestionSignatures = signatures
        )

        // Only persist valid, high-quality questions into DB
        if (generated.isNotEmpty()) {
            questionDao.insertAll(generated)
        }
        return generated
    }

    suspend fun generateOfflinePyqTest(
        pattern: ExamPattern = ExamPattern.JEE_MAIN,
        subject: Subject?,
        chapters: List<String>,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val allPyqs = if (subject != null) {
            questionDao.getGenuinePyqsBySubject(subject)
        } else {
            questionDao.getGenuinePyqs()
        }

        // Filter by chapters if selected
        val chapterFiltered = if (chapters.isNotEmpty()) {
            allPyqs.filter { q ->
                chapters.any { ch ->
                    q.chapter.equals(ch, ignoreCase = true) || q.topic.contains(ch, ignoreCase = true)
                }
            }
        } else {
            allPyqs
        }

        val pool = if (chapterFiltered.isNotEmpty()) chapterFiltered else allPyqs
        return pool.shuffled().take(count.coerceAtLeast(1))
    }

    suspend fun submitTestSession(
        title: String,
        pattern: ExamPattern = ExamPattern.JEE_MAIN,
        source: TestSource,
        durationMinutes: Int,
        timeSpentSeconds: Long,
        attempts: List<QuestionAttempt>
    ): TestSession {
        // Use deterministic, robust scoring engine without arbitrary 0.05 tolerance
        val scoring = com.example.data.model.JeeScoringEngine.calculateTestScore(attempts)

        val session = TestSession(
            id = UUID.randomUUID().toString(),
            title = title,
            examPattern = ExamPattern.JEE_MAIN,
            totalQuestions = attempts.size,
            durationMinutes = durationMinutes,
            timeSpentSeconds = timeSpentSeconds,
            totalMarks = scoring.totalMarks,
            score = scoring.score,
            correctCount = scoring.correctCount,
            wrongCount = scoring.wrongCount,
            unattemptedCount = scoring.unattemptedCount,
            accuracy = scoring.accuracy,
            percentage = scoring.percentage,
            timestamp = System.currentTimeMillis(),
            source = source,
            weakTopics = scoring.weakTopics,
            isCompleted = true,
            subjectBreakdownJson = scoring.subjectBreakdownJson,
            chapterBreakdownJson = scoring.chapterBreakdownJson,
            topicBreakdownJson = scoring.topicBreakdownJson
        )

        testSessionDao.insertSession(session)
        // Clear active exam on successful submit
        activeExamDao.clearActiveExam()
        return session
    }

    suspend fun saveActiveExamState(state: com.example.data.model.ActiveExamState) {
        activeExamDao.saveActiveExam(state)
    }

    suspend fun getActiveExamState(): com.example.data.model.ActiveExamState? {
        return activeExamDao.getActiveExam()
    }

    suspend fun clearActiveExamState() {
        activeExamDao.clearActiveExam()
    }

    fun getAllSessions(): Flow<List<TestSession>> = testSessionDao.getAllSessions()

    fun getRecentCompletedSessions(): Flow<List<TestSession>> =
        testSessionDao.getRecentCompletedSessions()

    suspend fun getChaptersForSubject(subject: Subject): List<String> {
        val dbChapters = questionDao.getChaptersForSubject(subject)
        val defaultChapters = GenuinePyqBank.chaptersBySubject[subject] ?: emptyList()
        return (dbChapters + defaultChapters).distinct().sorted()
    }
}

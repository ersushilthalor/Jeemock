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

    suspend fun initializeDatabaseIfNeeded() {
        val count = questionDao.getGenuinePyqCount()
        if (count == 0) {
            questionDao.insertAll(GenuinePyqBank.questions)
        }
    }

    suspend fun getGenuinePyqTest(
        pattern: ExamPattern,
        subject: Subject? = null,
        limit: Int = 10
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val allPyqs = if (subject != null) {
            questionDao.getGenuinePyqsBySubject(subject)
        } else {
            questionDao.getGenuinePyqsByPattern(pattern).ifEmpty {
                questionDao.getGenuinePyqs()
            }
        }
        return allPyqs.shuffled().take(limit)
    }

    suspend fun generateAiTest(
        pattern: ExamPattern,
        subject: Subject?,
        chapters: List<String>,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val existingQuestions = questionDao.getGenuinePyqs()
        val signatures = existingQuestions.map {
            it.textEn.lowercase().filter { ch -> ch.isLetterOrDigit() }.take(60)
        }.toSet()

        val generated = GeminiService.generatePersonalizedQuestions(
            context = context,
            pattern = pattern,
            subject = subject,
            chapters = chapters,
            difficulty = difficulty,
            count = count,
            existingQuestionSignatures = signatures
        )

        // Cache generated questions into DB for reference
        questionDao.insertAll(generated)
        return generated
    }

    suspend fun generateOfflinePyqTest(
        pattern: ExamPattern,
        subject: Subject?,
        chapters: List<String>,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        initializeDatabaseIfNeeded()
        val allPyqs = if (subject != null) {
            questionDao.getGenuinePyqsBySubject(subject)
        } else {
            questionDao.getGenuinePyqsByPattern(pattern).ifEmpty {
                questionDao.getGenuinePyqs()
            }
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
        pattern: ExamPattern,
        source: TestSource,
        durationMinutes: Int,
        timeSpentSeconds: Long,
        attempts: List<QuestionAttempt>
    ): TestSession {
        var score = 0
        var correctCount = 0
        var wrongCount = 0
        var unattemptedCount = 0
        val maxScore = attempts.sumOf { it.question.positiveMarks }

        val chapterMistakes = mutableMapOf<String, Int>()
        val chapterSuccesses = mutableMapOf<String, Int>()

        for (attempt in attempts) {
            val q = attempt.question
            val userAns = attempt.selectedOption?.trim()?.uppercase()
            val correctAns = q.correctAnswer.trim().uppercase()

            if (userAns.isNullOrBlank()) {
                unattemptedCount++
            } else {
                val isCorrect = if (q.questionType == com.example.data.model.QuestionType.NUMERICAL) {
                    val userNum = userAns.toDoubleOrNull()
                    val correctNum = correctAns.toDoubleOrNull()
                    if (userNum != null && correctNum != null) {
                        Math.abs(userNum - correctNum) < 0.05
                    } else {
                        userAns == correctAns
                    }
                } else {
                    userAns == correctAns
                }

                if (isCorrect) {
                    correctCount++
                    score += q.positiveMarks
                    chapterSuccesses[q.chapter] = (chapterSuccesses[q.chapter] ?: 0) + 1
                } else {
                    wrongCount++
                    score -= q.negativeMarks
                    chapterMistakes[q.chapter] = (chapterMistakes[q.chapter] ?: 0) + 1
                }
            }
        }

        val totalAttempted = correctCount + wrongCount
        val accuracy = if (totalAttempted > 0) {
            (correctCount.toFloat() / totalAttempted.toFloat()) * 100f
        } else {
            0f
        }

        // Identify weak topics where mistakes >= successes or errors > 0
        val identifiedWeak = mutableListOf<String>()
        chapterMistakes.forEach { (chapter, mistakes) ->
            val correct = chapterSuccesses[chapter] ?: 0
            if (mistakes > correct || mistakes >= 1) {
                identifiedWeak.add(chapter)
            }
        }

        val session = TestSession(
            id = UUID.randomUUID().toString(),
            title = title,
            examPattern = pattern,
            totalQuestions = attempts.size,
            durationMinutes = durationMinutes,
            timeSpentSeconds = timeSpentSeconds,
            totalMarks = maxScore,
            score = score,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unattemptedCount = unattemptedCount,
            accuracy = accuracy,
            timestamp = System.currentTimeMillis(),
            source = source,
            weakTopics = identifiedWeak,
            isCompleted = true
        )

        testSessionDao.insertSession(session)
        return session
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

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class Subject(val displayNameEn: String, val displayNameHi: String) {
    PHYSICS("Physics", "भौतिक विज्ञान"),
    CHEMISTRY("Chemistry", "रसायन विज्ञान"),
    MATHEMATICS("Mathematics", "गणित")
}

enum class QuestionType {
    MCQ,
    NUMERICAL
}

enum class ExamPattern(val displayName: String) {
    JEE_MAIN("JEE Main"),
    JEE_ADVANCED("JEE Advanced")
}

enum class Difficulty(val displayNameEn: String, val displayNameHi: String) {
    EASY("Easy", "सरल"),
    MEDIUM("Medium", "मध्यम"),
    HARD("Hard", "कठिन"),
    MIXED("Mixed", "मिश्रित")
}

enum class QuestionStatus {
    NOT_VISITED,
    NOT_ANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED
}

enum class TestSource {
    GENUINE_PYQ,
    GEMINI_GENERATED
}

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val textEn: String,
    val textHi: String,
    val optionsEn: List<String> = emptyList(),
    val optionsHi: List<String> = emptyList(),
    val correctAnswer: String, // "A", "B", "C", "D" or numeric string e.g. "25" or "4.5"
    val solutionEn: String,
    val solutionHi: String,
    val subject: Subject,
    val chapter: String,
    val topic: String,
    val examPattern: ExamPattern = ExamPattern.JEE_MAIN,
    val year: Int? = null,
    val session: String? = null, // e.g. "2024 Jan 27 Shift 1" or "2023 Paper 1"
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val questionType: QuestionType = QuestionType.MCQ,
    val isGenuinePyq: Boolean = true,
    val positiveMarks: Int = 4,
    val negativeMarks: Int = 1
)

@Entity(tableName = "test_sessions")
data class TestSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val examPattern: ExamPattern,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val timeSpentSeconds: Long = 0,
    val totalMarks: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unattemptedCount: Int = 0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val source: TestSource = TestSource.GENUINE_PYQ,
    val weakTopics: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

data class QuestionAttempt(
    val question: Question,
    var selectedOption: String? = null, // "A", "B", "C", "D" or numerical value string
    var status: QuestionStatus = QuestionStatus.NOT_VISITED,
    var timeSpentSeconds: Long = 0,
    var isMarkedForReview: Boolean = false
) {
    fun isAnswered(): Boolean = !selectedOption.isNullOrBlank()
}

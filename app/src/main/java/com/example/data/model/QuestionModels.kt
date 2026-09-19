package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

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
    JEE_MAIN("JEE Main")
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
    val session: String? = null, // e.g. "2024 Jan 27 Shift 1"
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val questionType: QuestionType = QuestionType.MCQ,
    val isGenuinePyq: Boolean = true,
    val positiveMarks: Int = 4,
    val negativeMarks: Int = 1,
    val numericalTolerance: Double = 0.0 // Question-defined tolerance for numerical answers
)

@Entity(tableName = "test_sessions")
data class TestSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val examPattern: ExamPattern = ExamPattern.JEE_MAIN,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val timeSpentSeconds: Long = 0,
    val totalMarks: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unattemptedCount: Int = 0,
    val accuracy: Float = 0f,
    val percentage: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val source: TestSource = TestSource.GENUINE_PYQ,
    val weakTopics: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val subjectBreakdownJson: String? = null,
    val chapterBreakdownJson: String? = null,
    val topicBreakdownJson: String? = null
)

@Entity(tableName = "active_exam_state")
data class ActiveExamState(
    @PrimaryKey val id: String = "ACTIVE_TEST",
    val title: String,
    val source: String,
    val examPattern: String = "JEE_MAIN",
    val durationMinutes: Int,
    val targetEndTimeMillis: Long,
    val currentQuestionIndex: Int = 0,
    val activeSectionFilter: String? = null,
    val attemptsJson: String,
    val isInProgress: Boolean = true,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    fun toAttempts(): List<QuestionAttempt> {
        return try {
            val arr = JSONArray(attemptsJson)
            val list = mutableListOf<QuestionAttempt>()
            for (i in 0 until arr.length()) {
                list.add(questionAttemptFromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromAttempts(
            title: String,
            source: TestSource,
            examPattern: ExamPattern = ExamPattern.JEE_MAIN,
            durationMinutes: Int,
            targetEndTimeMillis: Long,
            currentIndex: Int,
            sectionFilter: String?,
            attempts: List<QuestionAttempt>
        ): ActiveExamState {
            val arr = JSONArray()
            attempts.forEach { arr.put(questionAttemptToJson(it)) }
            return ActiveExamState(
                title = title,
                source = source.name,
                examPattern = examPattern.name,
                durationMinutes = durationMinutes,
                targetEndTimeMillis = targetEndTimeMillis,
                currentQuestionIndex = currentIndex,
                activeSectionFilter = sectionFilter,
                attemptsJson = arr.toString(),
                isInProgress = true,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        }

        private fun questionAttemptToJson(attempt: QuestionAttempt): JSONObject {
            val obj = JSONObject()
            obj.put("selectedOption", attempt.selectedOption ?: JSONObject.NULL)
            obj.put("status", attempt.status.name)
            obj.put("timeSpentSeconds", attempt.timeSpentSeconds)
            obj.put("isMarkedForReview", attempt.isMarkedForReview)

            val qObj = JSONObject()
            val q = attempt.question
            qObj.put("id", q.id)
            qObj.put("textEn", q.textEn)
            qObj.put("textHi", q.textHi)
            val optEnArr = JSONArray()
            q.optionsEn.forEach { optEnArr.put(it) }
            qObj.put("optionsEn", optEnArr)
            val optHiArr = JSONArray()
            q.optionsHi.forEach { optHiArr.put(it) }
            qObj.put("optionsHi", optHiArr)
            qObj.put("correctAnswer", q.correctAnswer)
            qObj.put("solutionEn", q.solutionEn)
            qObj.put("solutionHi", q.solutionHi)
            qObj.put("subject", q.subject.name)
            qObj.put("chapter", q.chapter)
            qObj.put("topic", q.topic)
            qObj.put("examPattern", q.examPattern.name)
            if (q.year != null) qObj.put("year", q.year)
            if (q.session != null) qObj.put("session", q.session)
            qObj.put("difficulty", q.difficulty.name)
            qObj.put("questionType", q.questionType.name)
            qObj.put("isGenuinePyq", q.isGenuinePyq)
            qObj.put("positiveMarks", q.positiveMarks)
            qObj.put("negativeMarks", q.negativeMarks)
            qObj.put("numericalTolerance", q.numericalTolerance)

            obj.put("question", qObj)
            return obj
        }

        private fun questionAttemptFromJson(obj: JSONObject): QuestionAttempt {
            val qObj = obj.getJSONObject("question")
            val optEn = mutableListOf<String>()
            val optEnArr = qObj.optJSONArray("optionsEn")
            if (optEnArr != null) {
                for (i in 0 until optEnArr.length()) {
                    optEn.add(optEnArr.getString(i))
                }
            }
            val optHi = mutableListOf<String>()
            val optHiArr = qObj.optJSONArray("optionsHi")
            if (optHiArr != null) {
                for (i in 0 until optHiArr.length()) {
                    optHi.add(optHiArr.getString(i))
                }
            }

            val question = Question(
                id = qObj.optString("id", UUID.randomUUID().toString()),
                textEn = qObj.optString("textEn", ""),
                textHi = qObj.optString("textHi", ""),
                optionsEn = optEn,
                optionsHi = optHi,
                correctAnswer = qObj.optString("correctAnswer", "A"),
                solutionEn = qObj.optString("solutionEn", ""),
                solutionHi = qObj.optString("solutionHi", ""),
                subject = try { Subject.valueOf(qObj.getString("subject")) } catch (e: Exception) { Subject.PHYSICS },
                chapter = qObj.optString("chapter", ""),
                topic = qObj.optString("topic", ""),
                examPattern = try { ExamPattern.valueOf(qObj.optString("examPattern", "JEE_MAIN")) } catch (e: Exception) { ExamPattern.JEE_MAIN },
                year = if (qObj.has("year")) qObj.optInt("year") else null,
                session = if (qObj.has("session")) qObj.optString("session") else null,
                difficulty = try { Difficulty.valueOf(qObj.optString("difficulty", "MEDIUM")) } catch (e: Exception) { Difficulty.MEDIUM },
                questionType = try { QuestionType.valueOf(qObj.optString("questionType", "MCQ")) } catch (e: Exception) { QuestionType.MCQ },
                isGenuinePyq = qObj.optBoolean("isGenuinePyq", true),
                positiveMarks = qObj.optInt("positiveMarks", 4),
                negativeMarks = qObj.optInt("negativeMarks", 1),
                numericalTolerance = qObj.optDouble("numericalTolerance", 0.0)
            )

            val selectedOption = if (obj.isNull("selectedOption")) null else obj.optString("selectedOption")
            val status = try { QuestionStatus.valueOf(obj.optString("status", QuestionStatus.NOT_VISITED.name)) } catch (e: Exception) { QuestionStatus.NOT_VISITED }
            val timeSpentSeconds = obj.optLong("timeSpentSeconds", 0L)
            val isMarkedForReview = obj.optBoolean("isMarkedForReview", false)

            return QuestionAttempt(
                question = question,
                selectedOption = selectedOption,
                status = status,
                timeSpentSeconds = timeSpentSeconds,
                isMarkedForReview = isMarkedForReview
            )
        }
    }
}

data class QuestionAttempt(
    val question: Question,
    var selectedOption: String? = null, // "A", "B", "C", "D" or numerical value string
    var status: QuestionStatus = QuestionStatus.NOT_VISITED,
    var timeSpentSeconds: Long = 0,
    var isMarkedForReview: Boolean = false
) {
    fun isAnswered(): Boolean = !selectedOption.isNullOrBlank()
}

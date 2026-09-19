package com.example.data.model

import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.roundToLong

object JeeScoringEngine {
    const val JEE_MAIN_POSITIVE_MARKS = 4
    const val JEE_MAIN_NEGATIVE_MARKS = 1

    /**
     * Determines whether the candidate's answer is correct for the given JEE Main question.
     * Evaluates both Single-Choice MCQs and Numerical Value Type questions.
     * Does NOT use an arbitrary 0.05 tolerance. Instead:
     * - Checks exact string match.
     * - Parses numeric values.
     * - If the expected answer is an integer, verifies integer equivalence (4 vs 4.0 vs 4.00).
     * - If the question defines a specific tolerance (numericalTolerance > 0), verifies within that boundary.
     * - Otherwise uses IEEE floating-point safe precision comparison (< 1e-5).
     */
    fun isAnswerCorrect(question: Question, userOption: String?): Boolean {
        if (userOption.isNullOrBlank()) return false
        val userClean = userOption.trim()
        val correctClean = question.correctAnswer.trim()

        return if (question.questionType == QuestionType.NUMERICAL) {
            isNumericalCorrect(userClean, correctClean, question.numericalTolerance)
        } else {
            // MCQ: standard option key comparison ("A", "B", "C", "D")
            userClean.equals(correctClean, ignoreCase = true)
        }
    }

    /**
     * Accurate numerical verification without arbitrary tolerance reliance.
     */
    fun isNumericalCorrect(userAns: String, correctAns: String, tolerance: Double = 0.0): Boolean {
        val uClean = userAns.trim()
        val cClean = correctAns.trim()

        if (uClean.equals(cClean, ignoreCase = true)) return true

        val userVal = uClean.toDoubleOrNull() ?: return false
        val correctVal = cClean.toDoubleOrNull() ?: return false

        // Check if correct answer is an exact integer
        val isIntegerExpected = abs(correctVal - correctVal.roundToLong()) < 1e-9 && tolerance <= 0.0
        if (isIntegerExpected) {
            return abs(userVal - correctVal) < 1e-5
        }

        // If explicit tolerance is defined by question metadata
        if (tolerance > 0.0) {
            return abs(userVal - correctVal) <= (tolerance + 1e-9)
        }

        // Standard floating point equality
        return abs(userVal - correctVal) < 1e-5
    }

    data class ScoringResult(
        val totalQuestions: Int,
        val totalMarks: Int,
        val score: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val unattemptedCount: Int,
        val accuracy: Float,
        val percentage: Float,
        val weakTopics: List<String>,
        val subjectBreakdownJson: String,
        val chapterBreakdownJson: String,
        val topicBreakdownJson: String
    )

    fun calculateTestScore(attempts: List<QuestionAttempt>): ScoringResult {
        var correctCount = 0
        var wrongCount = 0
        var unattemptedCount = 0
        var totalScore = 0
        var totalMaxMarks = 0

        val subjectStats = mutableMapOf<Subject, MutableSubjectAccumulator>()
        val chapterStats = mutableMapOf<String, MutableStatAccumulator>()
        val topicStats = mutableMapOf<String, MutableStatAccumulator>()
        val weakTopicList = mutableListOf<String>()

        for (attempt in attempts) {
            val q = attempt.question
            val pos = if (q.positiveMarks > 0) q.positiveMarks else JEE_MAIN_POSITIVE_MARKS
            val neg = if (q.negativeMarks > 0) q.negativeMarks else JEE_MAIN_NEGATIVE_MARKS
            totalMaxMarks += pos

            val subAcc = subjectStats.getOrPut(q.subject) { MutableSubjectAccumulator(q.subject) }
            val chapAcc = chapterStats.getOrPut(q.chapter) { MutableStatAccumulator() }
            val topAcc = topicStats.getOrPut(q.topic) { MutableStatAccumulator() }

            subAcc.total++
            chapAcc.total++
            topAcc.total++

            val userAns = attempt.selectedOption
            if (userAns.isNullOrBlank()) {
                unattemptedCount++
                subAcc.unattempted++
                chapAcc.unattempted++
                topAcc.unattempted++
            } else {
                val correct = isAnswerCorrect(q, userAns)
                if (correct) {
                    correctCount++
                    totalScore += pos
                    subAcc.correct++
                    subAcc.score += pos
                    chapAcc.correct++
                    topAcc.correct++
                } else {
                    wrongCount++
                    totalScore -= neg
                    subAcc.wrong++
                    subAcc.score -= neg
                    chapAcc.wrong++
                    topAcc.wrong++
                    if (q.topic.isNotBlank() && !weakTopicList.contains(q.topic)) {
                        weakTopicList.add(q.topic)
                    }
                }
            }
        }

        val attemptedCount = correctCount + wrongCount
        val accuracy = if (attemptedCount > 0) {
            (correctCount.toFloat() / attemptedCount.toFloat()) * 100f
        } else 0f

        val percentage = if (totalMaxMarks > 0) {
            (totalScore.toFloat() / totalMaxMarks.toFloat()) * 100f
        } else 0f

        // Serialize Subject Breakdown
        val subJson = JSONObject()
        for ((sub, acc) in subjectStats) {
            val sObj = JSONObject().apply {
                put("total", acc.total)
                put("correct", acc.correct)
                put("wrong", acc.wrong)
                put("unattempted", acc.unattempted)
                put("score", acc.score)
                put("maxScore", acc.total * 4)
                put("accuracy", if (acc.correct + acc.wrong > 0) (acc.correct.toFloat() / (acc.correct + acc.wrong)) * 100f else 0f)
            }
            subJson.put(sub.name, sObj)
        }

        // Serialize Chapter Breakdown
        val chapJson = JSONObject()
        for ((chap, acc) in chapterStats) {
            val cObj = JSONObject().apply {
                put("total", acc.total)
                put("correct", acc.correct)
                put("wrong", acc.wrong)
                put("unattempted", acc.unattempted)
            }
            chapJson.put(chap, cObj)
        }

        // Serialize Topic Breakdown
        val topJson = JSONObject()
        for ((top, acc) in topicStats) {
            val tObj = JSONObject().apply {
                put("total", acc.total)
                put("correct", acc.correct)
                put("wrong", acc.wrong)
                put("unattempted", acc.unattempted)
            }
            topJson.put(top, tObj)
        }

        return ScoringResult(
            totalQuestions = attempts.size,
            totalMarks = totalMaxMarks,
            score = totalScore,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unattemptedCount = unattemptedCount,
            accuracy = accuracy,
            percentage = percentage,
            weakTopics = weakTopicList,
            subjectBreakdownJson = subJson.toString(),
            chapterBreakdownJson = chapJson.toString(),
            topicBreakdownJson = topJson.toString()
        )
    }

    private class MutableSubjectAccumulator(val subject: Subject) {
        var total: Int = 0
        var correct: Int = 0
        var wrong: Int = 0
        var unattempted: Int = 0
        var score: Int = 0
    }

    private class MutableStatAccumulator {
        var total: Int = 0
        var correct: Int = 0
        var wrong: Int = 0
        var unattempted: Int = 0
    }
}

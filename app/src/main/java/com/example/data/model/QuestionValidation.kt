package com.example.data.model

import java.util.Locale

object QuestionValidation {

    /**
     * Extracts meaningful word tokens to compute lexical overlap and avoid semantic/minor wording duplicates.
     */
    private fun extractTokens(text: String): Set<String> {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 }
            .toSet()
    }

    /**
     * Normalized alphanumeric signature for exact or near-exact prefix comparison.
     */
    fun getAlphanumericSignature(text: String): String {
        return text.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }.take(60)
    }

    /**
     * Checks if a question is a duplicate or slight rewording of an existing question.
     * Uses both normalized prefix matching and Jaccard token overlap (threshold 0.70).
     */
    fun isDuplicateQuestion(candidateText: String, existingTexts: Collection<String>): Boolean {
        if (candidateText.isBlank()) return true
        val candSig = getAlphanumericSignature(candidateText)
        val candTokens = extractTokens(candidateText)

        for (existing in existingTexts) {
            val exSig = getAlphanumericSignature(existing)
            // Check normalized signature match
            if (candSig.isNotEmpty() && candSig == exSig) {
                return true
            }

            // Check lexical token overlap (Jaccard similarity)
            val exTokens = extractTokens(existing)
            if (candTokens.isNotEmpty() && exTokens.isNotEmpty()) {
                val intersection = candTokens.intersect(exTokens).size
                val union = candTokens.union(exTokens).size
                if (union > 0) {
                    val similarity = intersection.toDouble() / union.toDouble()
                    if (similarity >= 0.70) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Validates that the question is internally consistent and strictly adheres to JEE Main specifications.
     * Rejects any question with missing/invalid correct answer, incomplete options, or inconsistent types.
     */
    fun validateQuestion(question: Question): Pair<Boolean, String?> {
        if (question.textEn.trim().length < 15) {
            return Pair(false, "Question text in English is too short or empty")
        }

        if (question.chapter.isBlank()) {
            return Pair(false, "Chapter metadata is missing")
        }

        if (question.solutionEn.isBlank() && question.solutionHi.isBlank()) {
            return Pair(false, "Solution is missing")
        }

        when (question.questionType) {
            QuestionType.MCQ -> {
                if (question.optionsEn.size != 4) {
                    return Pair(false, "MCQ must contain exactly 4 options in English (found ${question.optionsEn.size})")
                }
                if (question.optionsEn.any { it.isBlank() }) {
                    return Pair(false, "MCQ option cannot be blank")
                }
                // Check distinct options
                val distinctOptions = question.optionsEn.map { it.trim().lowercase(Locale.ROOT) }.distinct()
                if (distinctOptions.size != 4) {
                    return Pair(false, "MCQ options must be mutually distinct")
                }
                // Verify correct answer is strictly A, B, C, or D
                val validMcqAnswers = setOf("A", "B", "C", "D")
                if (question.correctAnswer.trim().uppercase(Locale.ROOT) !in validMcqAnswers) {
                    return Pair(false, "Invalid correct answer '${question.correctAnswer}' for MCQ (must be A, B, C, or D)")
                }
            }
            QuestionType.NUMERICAL -> {
                val numVal = question.correctAnswer.trim().toDoubleOrNull()
                if (numVal == null) {
                    return Pair(false, "Numerical answer '${question.correctAnswer}' cannot be parsed into a valid number")
                }
            }
        }

        return Pair(true, null)
    }
}

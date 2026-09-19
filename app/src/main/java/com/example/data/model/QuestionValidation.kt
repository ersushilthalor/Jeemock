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
     * Rejects any question with missing/invalid correct answer, incomplete options, contradictory solutions,
     * references to missing diagrams, or scientifically impossible values.
     */
    fun validateQuestion(question: Question): Pair<Boolean, String?> {
        val textEnTrimmed = question.textEn.trim()
        if (textEnTrimmed.length < 25) {
            return Pair(false, "Question text in English is too short or empty (${textEnTrimmed.length} chars)")
        }

        // Check for placeholder or dummy text
        val lowerTextEn = textEnTrimmed.lowercase(Locale.ROOT)
        val placeholderKeywords = listOf("lorem ipsum", "[insert", "[diagram", "[figure", "test question", "sample question", "asdf")
        if (placeholderKeywords.any { lowerTextEn.contains(it) }) {
            return Pair(false, "Question text contains placeholder or dummy text")
        }

        // Check for truncation (e.g. ends with trailing punctuation like comma, incomplete formula)
        if (textEnTrimmed.endsWith(",") || textEnTrimmed.endsWith("=") || textEnTrimmed.endsWith("+") || textEnTrimmed.endsWith("-")) {
            return Pair(false, "Question text appears truncated or ends mid-formula")
        }

        // Verify chapter metadata is meaningful
        val chapterTrimmed = question.chapter.trim()
        if (chapterTrimmed.length < 3 || chapterTrimmed.equals("General", ignoreCase = true) ||
            chapterTrimmed.equals("Unknown", ignoreCase = true) || chapterTrimmed.equals("None", ignoreCase = true)
        ) {
            return Pair(false, "Chapter metadata is missing or generic ('$chapterTrimmed')")
        }

        // Verify solution is substantial and not a dummy string
        val solEn = question.solutionEn.trim()
        val solHi = question.solutionHi.trim()
        if (solEn.length < 20 && solHi.length < 20) {
            return Pair(false, "Solution is missing, too brief, or lacks step-by-step reasoning")
        }

        // 1. REJECT UNVERIFIABLE / MISSING DIAGRAM QUESTIONS
        // Physics, chemistry, and math questions referencing an unavailable diagram are unanswerable.
        val missingDiagramPatterns = listOf(
            Regex("""\b(in the (given|following|adjacent|shown) (figure|diagram|circuit|graph|table|plot))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(as shown in (the )?(figure|diagram|circuit|graph))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(refer(ring)? to the (given|following) (figure|diagram|circuit|graph))\b""", RegexOption.IGNORE_CASE),
            Regex("""(दिए गए चित्र|चित्र में दिखाए अनुसार|दिए गए परिपथ|नीचे दिए गए आरेख|दिए गए ग्राफ)""")
        )
        for (pattern in missingDiagramPatterns) {
            if (pattern.containsMatchIn(textEnTrimmed) || pattern.containsMatchIn(question.textHi)) {
                return Pair(false, "Question refers to an external figure, diagram, or circuit that is not present")
            }
        }

        when (question.questionType) {
            QuestionType.MCQ -> {
                if (question.optionsEn.size != 4) {
                    return Pair(false, "MCQ must contain exactly 4 options in English (found ${question.optionsEn.size})")
                }
                if (question.optionsEn.any { it.trim().isBlank() }) {
                    return Pair(false, "MCQ option cannot be blank")
                }

                // Check distinct options (must have 4 distinct values)
                val distinctOptions = question.optionsEn.map { it.trim().lowercase(Locale.ROOT) }.distinct()
                if (distinctOptions.size != 4) {
                    return Pair(false, "MCQ options must be mutually distinct (found only ${distinctOptions.size} distinct)")
                }

                // Reject trivial single-letter options like ["A", "B", "C", "D"]
                if (question.optionsEn.all { it.trim().length <= 3 && it.trim().matches(Regex("""^\(?[A-Da-d1-4]\)?\.?$""")) }) {
                    return Pair(false, "MCQ options must contain actual answer content, not just label letters")
                }

                // Verify correct answer is strictly A, B, C, or D
                val cleanCorrect = question.correctAnswer.trim().uppercase(Locale.ROOT)
                val validMcqAnswers = setOf("A", "B", "C", "D")
                if (cleanCorrect !in validMcqAnswers) {
                    return Pair(false, "Invalid correct answer '$cleanCorrect' for MCQ (must be A, B, C, or D)")
                }

                // 2. SOLUTION-ANSWER CONSISTENCY & CONTRADICTION CHECK FOR MCQ
                val combinedSolution = "$solEn\n$solHi"
                
                // Check if solution explicitly concludes with a different option letter:
                // e.g. "Hence option (B) is correct", "Therefore, option C", "Correct option: D", "विकल्प B सही है"
                val optionConclusionRegexes = listOf(
                    Regex("""(?:hence|therefore|thus|so|correct\s+option|correct\s+answer|answer\s+is|option)\s*(?:is\s*)?[:\-]?\s*[\(\[]?([A-D])[\)\]]?""", RegexOption.IGNORE_CASE),
                    Regex("""(?:विकल्प|उत्तर)\s*[:\-]?\s*[\(\[]?([A-D])[\)\]]?""", RegexOption.IGNORE_CASE),
                    Regex("""[\(\[]?([A-D])[\)\]]?\s*सही\s*(?:उत्तर|विकल्प)""", RegexOption.IGNORE_CASE)
                )

                val statedLettersInConclusion = mutableSetOf<String>()
                for (regex in optionConclusionRegexes) {
                    regex.findAll(combinedSolution).forEach { match ->
                        val letter = match.groupValues[1].uppercase(Locale.ROOT)
                        statedLettersInConclusion.add(letter)
                    }
                }

                // If the solution explicitly identified a concluding option, verify it matches cleanCorrect
                if (statedLettersInConclusion.isNotEmpty() && cleanCorrect !in statedLettersInConclusion) {
                    return Pair(false, "Solution concludes with option ${statedLettersInConclusion.joinToString("/")}, which contradicts stated correctAnswer $cleanCorrect")
                }
            }

            QuestionType.NUMERICAL -> {
                val numVal = question.correctAnswer.trim().toDoubleOrNull()
                if (numVal == null || numVal.isNaN() || numVal.isInfinite()) {
                    return Pair(false, "Numerical answer '${question.correctAnswer}' cannot be parsed into a valid finite number")
                }

                // 3. SOLUTION-ANSWER CONSISTENCY FOR NUMERICAL QUESTIONS
                val combinedSolution = "$solEn\n$solHi"
                val rawAnswer = question.correctAnswer.trim()

                // The numerical answer (as integer or as float) must appear in the solution text
                val intVal = if (numVal % 1.0 == 0.0) numVal.toLong().toString() else null
                val hasExactStr = combinedSolution.contains(rawAnswer)
                val hasIntStr = intVal != null && combinedSolution.contains(intVal)

                // Also check numeric tokens in solution
                val solutionNumbers = Regex("""-?\d+(?:\.\d+)?""")
                    .findAll(combinedSolution)
                    .mapNotNull { it.value.toDoubleOrNull() }
                    .toList()

                val matchesAnyNumber = solutionNumbers.any { kotlin.math.abs(it - numVal) <= 0.01 }

                if (!hasExactStr && !hasIntStr && !matchesAnyNumber) {
                    return Pair(false, "Numerical answer $rawAnswer does not appear anywhere in the solution derivation")
                }

                // Check if solution concludes with an explicitly conflicting final number in its last segment
                val conclusionSegment = combinedSolution.takeLast(120)
                val conclusionNumberMatch = Regex("""(?:answer|ans|result|=)\s*(?:is\s*)?[:\-]?\s*(-?\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
                    .findAll(conclusionSegment)
                    .lastOrNull()

                if (conclusionNumberMatch != null) {
                    val statedFinalNum = conclusionNumberMatch.groupValues[1].toDoubleOrNull()
                    if (statedFinalNum != null && kotlin.math.abs(statedFinalNum - numVal) > 0.05) {
                        return Pair(false, "Solution concludes with $statedFinalNum, which contradicts numerical correctAnswer $rawAnswer")
                    }
                }
            }
        }

        // 4. SCIENTIFIC & MATHEMATICAL SANITY CHECKS
        val lowerAll = (textEnTrimmed + " " + solEn).lowercase(Locale.ROOT)

        // Probability check: must be in [0, 1]
        if (lowerAll.contains("probability") || lowerAll.contains("प्रायिकता")) {
            val num = question.correctAnswer.trim().toDoubleOrNull()
            if (num != null && (num < 0.0 || num > 1.0)) {
                return Pair(false, "Probability value ($num) cannot be negative or greater than 1")
            }
        }

        // Absolute temperature check in Kelvin: cannot be negative
        if (Regex("""\b(temperature|kelvin)\b""", RegexOption.IGNORE_CASE).containsMatchIn(textEnTrimmed) &&
            Regex("""\b(in\s+k|in\s+kelvin)\b""", RegexOption.IGNORE_CASE).containsMatchIn(textEnTrimmed)
        ) {
            val num = question.correctAnswer.trim().toDoubleOrNull()
            if (num != null && num < 0.0) {
                return Pair(false, "Absolute temperature in Kelvin ($num K) cannot be negative")
            }
        }

        // Resistance / Inductance / Capacitance: passive values cannot be negative
        if (Regex("""\b(resistance|capacitance|inductance)\b""", RegexOption.IGNORE_CASE).containsMatchIn(textEnTrimmed)) {
            val num = question.correctAnswer.trim().toDoubleOrNull()
            if (num != null && num < 0.0) {
                return Pair(false, "Physical circuit parameter ($num) cannot be negative")
            }
        }

        return Pair(true, null)
    }
}

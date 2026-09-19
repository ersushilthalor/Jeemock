package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ApiKeyPreferences
import com.example.data.model.Difficulty
import com.example.data.model.ExamPattern
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

class NoInternetException(message: String = "No internet connection detected") : IOException(message)
class GeminiApiException(message: String) : Exception(message)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // High-speed, verified models with fallback
    private val CANDIDATE_MODELS = listOf(
        "gemini-3.1-flash-lite-preview",
        "gemini-flash-latest",
        "gemini-3.6-flash",
        "gemini-3.1-pro-preview"
    )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(75, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = connectivityManager.activeNetwork ?: return true
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Throwable) {
            true // Allow attempt even if capability inspection fails on some device ROMs
        }
    }

    suspend fun generatePersonalizedQuestions(
        context: Context,
        pattern: ExamPattern,
        subject: Subject?,
        chapters: List<String>,
        difficulty: Difficulty,
        count: Int,
        existingQuestionSignatures: Set<String>
    ): List<Question> = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyPreferences.getApiKey(context)

        if (apiKey.isBlank()) {
            throw GeminiApiException("API_NOT_CONFIGURED")
        }

        val subjectScope = subject?.displayNameEn ?: "Physics, Chemistry, and Mathematics equally"
        val chaptersScope = if (chapters.isNotEmpty()) chapters.joinToString(", ") else "All standard JEE chapters"
        val patternDescription = "JEE Main pattern (Mix of 4-option Single Choice MCQs and Numerical Value Type questions, marking +4 for correct and -1 for wrong for both MCQ and Numerical)."

        // Limit count to maximum 15 per call
        val safeCount = count.coerceIn(3, 15)

        val prompt = """
            You are a senior national test paper setter for the Indian Joint Entrance Examination (JEE Main).
            Generate EXACTLY $safeCount real, rigorous, high-quality JEE Main examination questions matching:
            - Exam: JEE Main
            - Subject: $subjectScope
            - Chapters/Topics: $chaptersScope
            - Difficulty Level: ${difficulty.displayNameEn}
            
            CRITICAL REQUIREMENTS:
            1. BILINGUAL SUPPORT: Every question, all 4 options, and the step-by-step solution MUST be provided in BOTH English and Hindi.
            2. SCIENTIFIC TERMINOLOGY: Use authentic Indian NCERT/JEE standard Hindi scientific vocabulary (e.g. त्वरण, संवेग, जड़त्व आघूर्ण, ऊष्मागतिकी, संकरण, समन्वय यौगिक, समाकलन, अवकल समीकरण, आदि). Do NOT use literal machine translations.
            3. MATH & FORMULAS: Keep formulas, units, and scientific notation clean and readable (e.g. F = m*a, 1.6 × 10^-19 C, ∫ x^2 dx, etc.).
            4. VALIDATION & CONSISTENCY:
               - For MCQ: 'optionsEn' MUST have exactly 4 distinct options. 'optionsHi' MUST have exactly 4 corresponding options. 'correctAnswer' MUST be strictly "A", "B", "C", or "D".
               - For NUMERICAL: 'optionsEn' and 'optionsHi' MUST be empty arrays []. 'correctAnswer' MUST be a clean numeric string (e.g. "12", "4.5", "-3").
               - 'solutionEn' and 'solutionHi' must provide complete step-by-step mathematical working.
               - If an answer cannot be verified, DO NOT guess.
            
            OUTPUT FORMAT: Return ONLY a valid JSON array of question objects with this schema:
            [
              {
                "subject": "PHYSICS" | "CHEMISTRY" | "MATHEMATICS",
                "chapter": "String",
                "topic": "String",
                "questionType": "MCQ" | "NUMERICAL",
                "textEn": "Question statement in English",
                "textHi": "Question statement in Hindi",
                "optionsEn": ["Option A text", "Option B text", "Option C text", "Option D text"],
                "optionsHi": ["Option A text in Hindi", "Option B text in Hindi", "Option C text in Hindi", "Option D text in Hindi"],
                "correctAnswer": "A",
                "solutionEn": "Step-by-step solution in English",
                "solutionHi": "Step-by-step solution in Hindi",
                "positiveMarks": 4,
                "negativeMarks": 1
              }
            ]
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        var lastError: Exception? = null

        // Try candidate models in priority sequence with automatic fallback
        for (modelName in CANDIDATE_MODELS) {
            val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            try {
                Log.d(TAG, "Attempting question generation with model: $modelName")
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val code = response.code
                    val err = response.body?.string() ?: ""
                    Log.w(TAG, "Model $modelName returned HTTP $code: $err")
                    // If model is busy (503/429) or unavailable (404), fall back to next model
                    if (code == 503 || code == 429 || code == 404) {
                        lastError = GeminiApiException("Model $modelName busy or unavailable ($code). Trying fallback...")
                        continue
                    }
                    if (code == 400 || code == 403) {
                        throw GeminiApiException("API_KEY_INVALID: The provided Gemini API Key is invalid or expired ($code). Please re-check your key in API settings.")
                    }
                    throw GeminiApiException("API error ($code): $err")
                }

                val responseBodyString = response.body?.string() ?: throw GeminiApiException("Empty response received from Gemini.")
                val parsedQuestions = parseAndValidateQuestions(responseBodyString, pattern, difficulty, existingQuestionSignatures)

                if (parsedQuestions.isNotEmpty()) {
                    Log.d(TAG, "Successfully generated ${parsedQuestions.size} questions using $modelName")
                    return@withContext parsedQuestions
                } else {
                    Log.w(TAG, "Parsing yielded 0 valid questions for $modelName. Trying next candidate...")
                }
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Network host unreachable: ${e.message}")
                throw NoInternetException("Network unreachable. Please check your internet connection and try again.")
            } catch (e: ConnectException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                throw NoInternetException("Failed to connect to the network. Please check your internet connection.")
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Timeout on model $modelName: ${e.message}")
                lastError = e
                // Try next model if one timed out
                continue
            } catch (e: IOException) {
                Log.w(TAG, "I/O failure on model $modelName: ${e.message}")
                lastError = e
                continue
            } catch (e: Exception) {
                Log.w(TAG, "Unexpected error on model $modelName: ${e.message}")
                lastError = e
                continue
            }
        }

        // If we reached here and have an error:
        if (lastError is SocketTimeoutException) {
            throw GeminiApiException("Connection timed out. The server took too long to respond. Please try again with fewer questions or check your connection.")
        }
        if (lastError is NoInternetException) {
            throw lastError
        }
        throw GeminiApiException(lastError?.message ?: "Unable to generate personalized questions at this moment. Please check your network and try again.")
    }

    private fun parseAndValidateQuestions(
        responseJsonStr: String,
        pattern: ExamPattern,
        difficulty: Difficulty,
        existingQuestionSignatures: Set<String>
    ): List<Question> {
        val root = try {
            JSONObject(responseJsonStr)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response JSON: ${e.message}")
            return emptyList()
        }

        val candidates = root.optJSONArray("candidates") ?: return emptyList()
        if (candidates.length() == 0) return emptyList()

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return emptyList()
        val parts = content.optJSONArray("parts") ?: return emptyList()
        if (parts.length() == 0) return emptyList()

        // Extract and concatenate all text from parts (handles thinking parts or split chunks)
        val textBuilder = StringBuilder()
        for (p in 0 until parts.length()) {
            val partObj = parts.optJSONObject(p) ?: continue
            val partText = partObj.optString("text", "")
            if (partText.isNotBlank()) {
                textBuilder.append(partText)
            }
        }
        val text = textBuilder.toString().trim()
        if (text.isBlank()) return emptyList()

        // Find clean JSON array boundary
        val firstBracket = text.indexOf('[')
        val lastBracket = text.lastIndexOf(']')

        val jsonArray = try {
            if (firstBracket != -1 && lastBracket > firstBracket) {
                JSONArray(text.substring(firstBracket, lastBracket + 1))
            } else {
                val cleaned = text
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                if (cleaned.startsWith("[")) {
                    JSONArray(cleaned)
                } else if (cleaned.startsWith("{")) {
                    val obj = JSONObject(cleaned)
                    obj.optJSONArray("questions") ?: JSONArray().apply { put(obj) }
                } else {
                    return emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse questions array: ${e.message}", e)
            return emptyList()
        }

        val validList = mutableListOf<Question>()
        val seenInCurrentBatch = mutableSetOf<String>()

        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                val textEn = obj.optString("textEn", "").trim()
                val textHi = obj.optString("textHi", "").trim()
                if (textEn.isBlank()) continue

                // Fallback textHi if model missed it
                val safeTextHi = if (textHi.isNotBlank()) textHi else textEn

                // Check duplicate using advanced token/signature analysis
                if (com.example.data.model.QuestionValidation.isDuplicateQuestion(textEn, existingQuestionSignatures) ||
                    com.example.data.model.QuestionValidation.isDuplicateQuestion(textEn, seenInCurrentBatch)
                ) {
                    Log.d(TAG, "Skipping duplicate/reworded AI question: ${textEn.take(40)}...")
                    continue
                }

                val typeStr = obj.optString("questionType", "MCQ").uppercase()
                val qType = if (typeStr.contains("NUM")) QuestionType.NUMERICAL else QuestionType.MCQ

                val optionsEnList = mutableListOf<String>()
                val optionsHiList = mutableListOf<String>()

                val optionsEnArr = obj.optJSONArray("optionsEn")
                val optionsHiArr = obj.optJSONArray("optionsHi")

                if (qType == QuestionType.MCQ) {
                    if (optionsEnArr == null || optionsEnArr.length() < 4) {
                        Log.w(TAG, "Skipping MCQ: Insufficient options in English.")
                        continue
                    }
                    for (k in 0 until 4) {
                        val optEn = optionsEnArr.optString(k, "").trim()
                        val optHi = optionsHiArr?.optString(k, "")?.trim() ?: ""
                        if (optEn.isBlank()) continue
                        optionsEnList.add(optEn)
                        optionsHiList.add(if (optHi.isNotBlank()) optHi else optEn)
                    }
                    if (optionsEnList.size != 4) {
                        Log.w(TAG, "Skipping MCQ: Blank options detected.")
                        continue
                    }
                }

                val rawCorrect = obj.optString("correctAnswer", "").trim()
                if (rawCorrect.isBlank()) {
                    Log.w(TAG, "Skipping question: Blank correctAnswer received from AI.")
                    continue
                }

                var normalizedCorrect = when (rawCorrect.uppercase()) {
                    "A", "1", "OPTION A", "OPTION 1", "(A)", "(1)" -> "A"
                    "B", "2", "OPTION B", "OPTION 2", "(B)", "(2)" -> "B"
                    "C", "3", "OPTION C", "OPTION 3", "(C)", "(3)" -> "C"
                    "D", "4", "OPTION D", "OPTION 4", "(D)", "(4)" -> "D"
                    else -> rawCorrect
                }

                // If model returned option value itself for MCQ, find matching index
                if (qType == QuestionType.MCQ && normalizedCorrect !in listOf("A", "B", "C", "D")) {
                    val matchEn = optionsEnList.indexOfFirst { it.equals(rawCorrect, ignoreCase = true) }
                    val matchHi = optionsHiList.indexOfFirst { it.equals(rawCorrect, ignoreCase = true) }
                    val foundIdx = if (matchEn != -1) matchEn else matchHi
                    if (foundIdx in 0..3) {
                        normalizedCorrect = listOf("A", "B", "C", "D")[foundIdx]
                    } else {
                        // User requirement: Do NOT default to "A" if answer is invalid or unresolved! Reject the question.
                        Log.w(TAG, "Rejecting AI question: MCQ correct answer '$rawCorrect' does not resolve to any option.")
                        continue
                    }
                }

                if (qType == QuestionType.NUMERICAL) {
                    val cleanNum = rawCorrect.filter { it.isDigit() || it == '.' || it == '-' }
                    if (cleanNum.toDoubleOrNull() == null) {
                        // User requirement: Do NOT default to "0" if answer is missing/invalid! Reject the question.
                        Log.w(TAG, "Rejecting AI question: Numerical correct answer '$rawCorrect' is not a valid number.")
                        continue
                    }
                    normalizedCorrect = cleanNum
                }

                val solutionEn = obj.optString("solutionEn", "").trim()
                val solutionHi = obj.optString("solutionHi", "").trim()
                if (solutionEn.isBlank() && solutionHi.isBlank()) {
                    Log.w(TAG, "Rejecting AI question: Both solutionEn and solutionHi are missing.")
                    continue
                }
                val safeSolutionEn = if (solutionEn.isNotBlank()) solutionEn else "Detailed solution: Correct answer is $normalizedCorrect."
                val safeSolutionHi = if (solutionHi.isNotBlank()) solutionHi else "विस्तृत हल: सही उत्तर $normalizedCorrect है।"

                val subjectStr = obj.optString("subject", "PHYSICS").uppercase()
                val subject = when {
                    subjectStr.contains("CHEM") -> Subject.CHEMISTRY
                    subjectStr.contains("MATH") -> Subject.MATHEMATICS
                    else -> Subject.PHYSICS
                }

                val chapter = obj.optString("chapter", "General").trim()
                val topic = obj.optString("topic", "Concepts").trim()
                val posMarks = obj.optInt("positiveMarks", 4)
                // In JEE Main, both MCQ and Numerical have -1 negative marking
                val negMarks = obj.optInt("negativeMarks", 1)

                val question = Question(
                    id = "AI_${UUID.randomUUID()}",
                    textEn = textEn,
                    textHi = safeTextHi,
                    optionsEn = optionsEnList,
                    optionsHi = optionsHiList,
                    correctAnswer = normalizedCorrect,
                    solutionEn = safeSolutionEn,
                    solutionHi = safeSolutionHi,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    difficulty = difficulty,
                    questionType = qType,
                    isGenuinePyq = false,
                    year = 2025,
                    examPattern = ExamPattern.JEE_MAIN,
                    session = "Gemini AI Personalized Test",
                    positiveMarks = posMarks,
                    negativeMarks = negMarks,
                    numericalTolerance = 0.0
                )

                // Thorough internal consistency check
                val (isValid, reason) = com.example.data.model.QuestionValidation.validateQuestion(question)
                if (!isValid) {
                    Log.w(TAG, "Rejecting AI question failing validation: $reason")
                    continue
                }

                validList.add(question)
                seenInCurrentBatch.add(textEn)
            } catch (e: Exception) {
                Log.w(TAG, "Skipping question due to parsing anomaly: ${e.message}")
            }
        }

        return validList
    }
}

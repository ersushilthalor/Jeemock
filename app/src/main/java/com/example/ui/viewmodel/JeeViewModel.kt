package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ApiKeyPreferences
import com.example.data.local.JeeDatabase
import com.example.data.model.Difficulty
import com.example.data.model.ExamPattern
import com.example.data.model.Question
import com.example.data.model.QuestionAttempt
import com.example.data.model.QuestionStatus
import com.example.data.model.Subject
import com.example.data.model.TestSession
import com.example.data.model.TestSource
import com.example.data.remote.GeminiApiException
import com.example.data.remote.NoInternetException
import com.example.data.repository.JeeRepository
import com.example.ui.common.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    DASHBOARD,
    AI_GENERATOR,
    ACTIVE_EXAM,
    EXAM_RESULT,
    TEST_HISTORY
}

sealed class GeneratorUiState {
    object Idle : GeneratorUiState()
    object Loading : GeneratorUiState()
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val isApiKeyError: Boolean = false
    ) : GeneratorUiState()
}

class JeeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = JeeDatabase.getDatabase(application)
    private val repository = JeeRepository(application, database)

    // Global app language
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Navigation screen
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Generator Form State
    private val _generatorState = MutableStateFlow<GeneratorUiState>(GeneratorUiState.Idle)
    val generatorState: StateFlow<GeneratorUiState> = _generatorState.asStateFlow()

    var genPattern = MutableStateFlow(ExamPattern.JEE_MAIN)
    var genSubject = MutableStateFlow<Subject?>(null) // null = Full Syllabus
    var genSelectedChapters = MutableStateFlow<Set<String>>(emptySet())
    var genDifficulty = MutableStateFlow(Difficulty.MEDIUM)
    var genQuestionCount = MutableStateFlow(5)
    var genDurationMinutes = MutableStateFlow(15)

    val availableChapters = MutableStateFlow<List<String>>(emptyList())

    // API Key State
    private val _hasApiKey = MutableStateFlow(ApiKeyPreferences.hasApiKey(application))
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    private val _maskedApiKey = MutableStateFlow(ApiKeyPreferences.getMaskedApiKey(application))
    val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    fun openApiKeyDialog() {
        _showApiKeyDialog.value = true
    }

    fun closeApiKeyDialog() {
        _showApiKeyDialog.value = false
    }

    fun saveCustomApiKey(key: String) {
        ApiKeyPreferences.saveApiKey(getApplication(), key)
        _hasApiKey.value = ApiKeyPreferences.hasApiKey(getApplication())
        _maskedApiKey.value = ApiKeyPreferences.getMaskedApiKey(getApplication())
        _showApiKeyDialog.value = false
        if (_generatorState.value is GeneratorUiState.Error) {
            _generatorState.value = GeneratorUiState.Idle
        }
    }

    fun clearCustomApiKey() {
        ApiKeyPreferences.clearApiKey(getApplication())
        _hasApiKey.value = ApiKeyPreferences.hasApiKey(getApplication())
        _maskedApiKey.value = ApiKeyPreferences.getMaskedApiKey(getApplication())
    }

    // Active Exam State
    private val _activeTestTitle = MutableStateFlow("JEE Mock Test")
    val activeTestTitle: StateFlow<String> = _activeTestTitle.asStateFlow()

    private val _activeTestSource = MutableStateFlow(TestSource.GENUINE_PYQ)
    val activeTestSource: StateFlow<TestSource> = _activeTestSource.asStateFlow()

    private val _activePattern = MutableStateFlow(ExamPattern.JEE_MAIN)
    val activePattern: StateFlow<ExamPattern> = _activePattern.asStateFlow()

    private val _attempts = MutableStateFlow<List<QuestionAttempt>>(emptyList())
    val attempts: StateFlow<List<QuestionAttempt>> = _attempts.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(1200L) // 20 mins default
    val timeRemainingSeconds: StateFlow<Long> = _timeRemainingSeconds.asStateFlow()

    private val _activeSectionFilter = MutableStateFlow<Subject?>(null)
    val activeSectionFilter: StateFlow<Subject?> = _activeSectionFilter.asStateFlow()

    // Per-question language override: questionId -> AppLanguage
    private val _questionLanguageOverrides = MutableStateFlow<Map<String, AppLanguage>>(emptyMap())
    val questionLanguageOverrides: StateFlow<Map<String, AppLanguage>> = _questionLanguageOverrides.asStateFlow()

    // Result of last completed test
    private val _lastCompletedSession = MutableStateFlow<TestSession?>(null)
    val lastCompletedSession: StateFlow<TestSession?> = _lastCompletedSession.asStateFlow()

    private val _lastCompletedAttempts = MutableStateFlow<List<QuestionAttempt>>(emptyList())
    val lastCompletedAttempts: StateFlow<List<QuestionAttempt>> = _lastCompletedAttempts.asStateFlow()

    // History Flow from Room
    val testHistory: StateFlow<List<TestSession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weak topics extracted from test history
    private val _aggregatedWeakTopics = MutableStateFlow<List<String>>(emptyList())
    val aggregatedWeakTopics: StateFlow<List<String>> = _aggregatedWeakTopics.asStateFlow()

    private var timerJob: Job? = null
    private var testStartDurationMinutes: Int = 20

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            loadChaptersForSubject(null)
            // Observe history to compute weak topics
            repository.getAllSessions().collect { sessions ->
                val weak = sessions.flatMap { it.weakTopics }.distinct()
                _aggregatedWeakTopics.value = weak
            }
        }
    }

    fun toggleAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun toggleQuestionLanguage(questionId: String) {
        val current = _questionLanguageOverrides.value[questionId] ?: _appLanguage.value
        val next = if (current == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
        _questionLanguageOverrides.value = _questionLanguageOverrides.value + (questionId to next)
    }

    fun getQuestionLanguage(questionId: String): AppLanguage {
        return _questionLanguageOverrides.value[questionId] ?: _appLanguage.value
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun loadChaptersForSubject(subject: Subject?) {
        viewModelScope.launch {
            if (subject == null) {
                val allChapters = Subject.values().flatMap { repository.getChaptersForSubject(it) }
                availableChapters.value = allChapters.distinct().sorted()
            } else {
                availableChapters.value = repository.getChaptersForSubject(subject)
            }
        }
    }

    fun toggleChapterSelection(chapter: String) {
        val current = genSelectedChapters.value.toMutableSet()
        if (current.contains(chapter)) {
            current.remove(chapter)
        } else {
            current.add(chapter)
        }
        genSelectedChapters.value = current
    }

    // Launch genuine PYQ Mock Test
    fun startGenuinePyqTest(pattern: ExamPattern, subject: Subject? = null) {
        viewModelScope.launch {
            val title = if (subject != null) {
                "${subject.displayNameEn} Official PYQ Test (${pattern.displayName})"
            } else {
                "Complete ${pattern.displayName} Genuine PYQs Mock"
            }
            val questions = repository.getGenuinePyqTest(pattern, subject, limit = 12)
            if (questions.isNotEmpty()) {
                setupAndStartExam(
                    title = title,
                    source = TestSource.GENUINE_PYQ,
                    pattern = pattern,
                    questions = questions,
                    durationMinutes = 25
                )
            }
        }
    }

    // Launch AI Personalized Test
    fun generateAndStartAiTest() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val hasKey = ApiKeyPreferences.hasApiKey(context)

            if (!hasKey) {
                _showApiKeyDialog.value = true
                val isEn = _appLanguage.value == AppLanguage.ENGLISH
                _generatorState.value = GeneratorUiState.Error(
                    message = if (isEn)
                        "Gemini API key is not configured. Please tap 'Set API Key' below to enter your free key, or generate from genuine offline PYQs."
                    else
                        "जेमिनी एपीआई कुंजी कॉन्फ़िगर नहीं है। कृपया नीचे 'Set API Key' दबाकर अपनी मुफ्त की दर्ज करें, या ऑफलाइन पीवाईक्यू से टेस्ट बनाएं।",
                    canRetry = false,
                    isApiKeyError = true
                )
                return@launch
            }

            _generatorState.value = GeneratorUiState.Loading
            try {
                val pattern = genPattern.value
                val subject = genSubject.value
                val chapters = genSelectedChapters.value.toList()
                val difficulty = genDifficulty.value
                val count = genQuestionCount.value
                val duration = genDurationMinutes.value

                val title = buildString {
                    append("AI Personalized ")
                    if (subject != null) append("${subject.displayNameEn} ") else append("Full Syllabus ")
                    append("(${pattern.displayName})")
                }

                val questions = repository.generateAiTest(
                    pattern = pattern,
                    subject = subject,
                    chapters = chapters,
                    difficulty = difficulty,
                    count = count
                )

                _generatorState.value = GeneratorUiState.Idle
                setupAndStartExam(
                    title = title,
                    source = TestSource.GEMINI_GENERATED,
                    pattern = pattern,
                    questions = questions,
                    durationMinutes = duration
                )
            } catch (e: NoInternetException) {
                val isEn = _appLanguage.value == AppLanguage.ENGLISH
                _generatorState.value = GeneratorUiState.Error(
                    message = if (isEn) "Network connection error. Please check your internet connection and try again."
                    else "नेटवर्क कनेक्शन त्रुटि। कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।",
                    canRetry = true,
                    isApiKeyError = false
                )
            } catch (e: GeminiApiException) {
                val isEn = _appLanguage.value == AppLanguage.ENGLISH
                val isKeyError = e.message?.contains("API_NOT_CONFIGURED") == true ||
                        e.message?.contains("API_KEY_INVALID") == true ||
                        e.message?.contains("API key") == true

                if (isKeyError) {
                    _showApiKeyDialog.value = true
                }

                val userFriendlyMessage = when {
                    e.message?.contains("API_NOT_CONFIGURED") == true ->
                        if (isEn) "Gemini API key is not configured. Tap 'Set API Key' to enter your free key."
                        else "जेमिनी एपीआई कुंजी कॉन्फ़िगर नहीं है। कृपया 'Set API Key' दबाकर अपनी की दर्ज करें।"
                    e.message?.contains("API_KEY_INVALID") == true ->
                        if (isEn) "Invalid Gemini API key. Please check and re-enter your key."
                        else "अमान्य जेमिनी एपीआई की। कृपया अपनी कुंजी की जांच करें और पुनः दर्ज करें।"
                    else -> e.message ?: "Gemini API error. Please try again."
                }

                _generatorState.value = GeneratorUiState.Error(
                    message = userFriendlyMessage,
                    canRetry = true,
                    isApiKeyError = isKeyError
                )
            } catch (e: Exception) {
                val isEn = _appLanguage.value == AppLanguage.ENGLISH
                _generatorState.value = GeneratorUiState.Error(
                    message = if (isEn) "Unable to generate test: ${e.localizedMessage}"
                    else "टेस्ट जनरेट करने में असमर्थ: ${e.localizedMessage}",
                    canRetry = true,
                    isApiKeyError = false
                )
            }
        }
    }

    // Launch Offline Genuine PYQ Test with custom configuration
    fun generateAndStartOfflineTest() {
        viewModelScope.launch {
            _generatorState.value = GeneratorUiState.Loading
            try {
                val pattern = genPattern.value
                val subject = genSubject.value
                val chapters = genSelectedChapters.value.toList()
                val difficulty = genDifficulty.value
                val count = genQuestionCount.value
                val duration = genDurationMinutes.value

                val title = buildString {
                    append(pattern.displayName)
                    if (subject != null) append(" - ${subject.displayNameEn}")
                    append(" (PYQ Mock)")
                }

                val questions = repository.generateOfflinePyqTest(
                    pattern = pattern,
                    subject = subject,
                    chapters = chapters,
                    difficulty = difficulty,
                    count = count
                )

                _generatorState.value = GeneratorUiState.Idle
                setupAndStartExam(
                    title = title,
                    source = TestSource.GENUINE_PYQ,
                    pattern = pattern,
                    questions = questions,
                    durationMinutes = duration
                )
            } catch (e: Exception) {
                val isEn = _appLanguage.value == AppLanguage.ENGLISH
                _generatorState.value = GeneratorUiState.Error(
                    message = if (isEn) "Unable to load offline questions: ${e.localizedMessage}"
                    else "ऑफलाइन प्रश्न लोड करने में असमर्थ: ${e.localizedMessage}",
                    canRetry = true,
                    isApiKeyError = false
                )
            }
        }
    }

    fun startTargetedWeakTopicTest(topic: String) {
        genSelectedChapters.value = setOf(topic)
        genQuestionCount.value = 5
        genDurationMinutes.value = 10
        genDifficulty.value = Difficulty.MEDIUM
        _currentScreen.value = Screen.AI_GENERATOR
        generateAndStartAiTest()
    }

    private fun setupAndStartExam(
        title: String,
        source: TestSource,
        pattern: ExamPattern,
        questions: List<Question>,
        durationMinutes: Int
    ) {
        _activeTestTitle.value = title
        _activeTestSource.value = source
        _activePattern.value = pattern
        testStartDurationMinutes = durationMinutes
        _timeRemainingSeconds.value = durationMinutes * 60L
        _currentQuestionIndex.value = 0
        _activeSectionFilter.value = null

        val attemptList = questions.mapIndexed { index, question ->
            QuestionAttempt(
                question = question,
                status = if (index == 0) QuestionStatus.NOT_ANSWERED else QuestionStatus.NOT_VISITED
            )
        }
        _attempts.value = attemptList
        _currentScreen.value = Screen.ACTIVE_EXAM

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000L)
                _timeRemainingSeconds.value -= 1
            }
            // Auto submit when time expires
            submitExam()
        }
    }

    fun setSectionFilter(subject: Subject?) {
        _activeSectionFilter.value = subject
        // If current question does not match section, jump to first matching question
        if (subject != null) {
            val idx = _attempts.value.indexOfFirst { it.question.subject == subject }
            if (idx >= 0) {
                navigateToQuestion(idx)
            }
        }
    }

    fun updateSelectedAnswer(answer: String?) {
        val currentList = _attempts.value.toMutableList()
        val idx = _currentQuestionIndex.value
        if (idx in currentList.indices) {
            val item = currentList[idx].copy(selectedOption = answer)
            currentList[idx] = item
            _attempts.value = currentList
        }
    }

    fun onSaveAndNext(answer: String?) {
        val currentList = _attempts.value.toMutableList()
        val idx = _currentQuestionIndex.value
        if (idx in currentList.indices) {
            val old = currentList[idx]
            val newStatus = if (!answer.isNullOrBlank()) {
                QuestionStatus.ANSWERED
            } else {
                QuestionStatus.NOT_ANSWERED
            }
            currentList[idx] = old.copy(selectedOption = answer, status = newStatus)
            _attempts.value = currentList
        }

        // Advance to next question
        if (idx < currentList.size - 1) {
            val nextIdx = idx + 1
            if (currentList[nextIdx].status == QuestionStatus.NOT_VISITED) {
                currentList[nextIdx] = currentList[nextIdx].copy(status = QuestionStatus.NOT_ANSWERED)
                _attempts.value = currentList
            }
            _currentQuestionIndex.value = nextIdx
        }
    }

    fun onMarkForReviewAndNext(answer: String?) {
        val currentList = _attempts.value.toMutableList()
        val idx = _currentQuestionIndex.value
        if (idx in currentList.indices) {
            val old = currentList[idx]
            val newStatus = if (!answer.isNullOrBlank()) {
                QuestionStatus.ANSWERED_AND_MARKED
            } else {
                QuestionStatus.MARKED_FOR_REVIEW
            }
            currentList[idx] = old.copy(selectedOption = answer, status = newStatus)
            _attempts.value = currentList
        }

        // Advance
        if (idx < currentList.size - 1) {
            val nextIdx = idx + 1
            if (currentList[nextIdx].status == QuestionStatus.NOT_VISITED) {
                currentList[nextIdx] = currentList[nextIdx].copy(status = QuestionStatus.NOT_ANSWERED)
                _attempts.value = currentList
            }
            _currentQuestionIndex.value = nextIdx
        }
    }

    fun onClearResponse() {
        val currentList = _attempts.value.toMutableList()
        val idx = _currentQuestionIndex.value
        if (idx in currentList.indices) {
            val old = currentList[idx]
            currentList[idx] = old.copy(selectedOption = null, status = QuestionStatus.NOT_ANSWERED)
            _attempts.value = currentList
        }
    }

    fun navigateToQuestion(targetIndex: Int) {
        val currentList = _attempts.value.toMutableList()
        if (targetIndex in currentList.indices) {
            // Update target status if it was not visited
            if (currentList[targetIndex].status == QuestionStatus.NOT_VISITED) {
                currentList[targetIndex] = currentList[targetIndex].copy(status = QuestionStatus.NOT_ANSWERED)
                _attempts.value = currentList
            }
            _currentQuestionIndex.value = targetIndex
        }
    }

    fun submitExam() {
        timerJob?.cancel()
        val timeSpent = (testStartDurationMinutes * 60L) - _timeRemainingSeconds.value
        val currentAttempts = _attempts.value

        viewModelScope.launch {
            val session = repository.submitTestSession(
                title = _activeTestTitle.value,
                pattern = _activePattern.value,
                source = _activeTestSource.value,
                durationMinutes = testStartDurationMinutes,
                timeSpentSeconds = if (timeSpent > 0) timeSpent else 0,
                attempts = currentAttempts
            )
            _lastCompletedSession.value = session
            _lastCompletedAttempts.value = currentAttempts
            _currentScreen.value = Screen.EXAM_RESULT
        }
    }
}

package com.example.ui.common

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी")
}

object Strings {
    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "JEE Mock Test"
        AppLanguage.HINDI -> "जेईई मॉक टेस्ट"
    }

    fun officialPyqTests(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Official JEE PYQ Tests"
        AppLanguage.HINDI -> "वास्तविक जेईई विगत वर्ष प्रश्न"
    }

    fun officialPyqSub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Authentic previous year questions with year, shift & session metadata"
        AppLanguage.HINDI -> "वर्ष, शिफ्ट और सत्र विवरण के साथ प्रामाणिक पिछले वर्षों के प्रश्न"
    }

    fun aiGeneratorTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Gemini AI Test Generator"
        AppLanguage.HINDI -> "जेमिनी एआई अनुकूलित टेस्ट निर्माता"
    }

    fun aiGeneratorSub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Real-time personalized mock tests tailored to your syllabus & difficulty"
        AppLanguage.HINDI -> "आपके पाठ्यक्रम एवं कठिनाई स्तर अनुसार रीयल-टाइम व्यक्तिगत टेस्ट"
    }

    fun weakTopicsAlert(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Identified Weak Topics"
        AppLanguage.HINDI -> "कमजोर विषय विश्लेषण"
    }

    fun generateWeakTest(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Practice Weak Topics (AI)"
        AppLanguage.HINDI -> "कमजोर विषयों का अभ्यास (AI)"
    }

    fun startTest(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Start Mock Test"
        AppLanguage.HINDI -> "मॉक टेस्ट प्रारंभ करें"
    }

    fun examInstructions(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "NTA CBT Exam Instructions"
        AppLanguage.HINDI -> "एनटीए सीबीटी परीक्षा निर्देश"
    }

    fun timeRemaining(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Time Left"
        AppLanguage.HINDI -> "शेष समय"
    }

    fun saveAndNext(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Save & Next"
        AppLanguage.HINDI -> "सहेजें और अगला"
    }

    fun markForReview(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Mark for Review & Next"
        AppLanguage.HINDI -> "समीक्षा हेतु चिह्नित करें"
    }

    fun clearResponse(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Clear Response"
        AppLanguage.HINDI -> "उत्तर हटाएं"
    }

    fun previous(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Previous"
        AppLanguage.HINDI -> "पिछला"
    }

    fun submitTest(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Submit Test"
        AppLanguage.HINDI -> "टेस्ट जमा करें"
    }

    fun questionPalette(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Question Palette"
        AppLanguage.HINDI -> "प्रश्न तालिका"
    }

    fun numericalInputPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Enter your numerical answer:"
        AppLanguage.HINDI -> "अपना संख्यात्मक उत्तर दर्ज करें:"
    }

    fun questionTypeLabel(type: com.example.data.model.QuestionType, lang: AppLanguage): String = when (type) {
        com.example.data.model.QuestionType.MCQ -> if (lang == AppLanguage.ENGLISH) "Single Choice MCQ (+4, -1)" else "एकल विकल्प प्रश्न (+4, -1)"
        com.example.data.model.QuestionType.NUMERICAL -> if (lang == AppLanguage.ENGLISH) "Numerical Value Type (+4, -1)" else "संख्यात्मक मान प्रकार (+4, -1)"
    }

    fun genuineBadge(session: String?, lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "GENUINE PYQ • ${session ?: "Official JEE Main"}"
        AppLanguage.HINDI -> "प्रामाणिक विगत वर्ष • ${session ?: "आधिकारिक जेईई मेन"}"
    }

    fun aiGeneratedBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "AI Practice (JEE Main Pattern)"
        AppLanguage.HINDI -> "एआई अभ्यास प्रश्न (जेईई मेन पैटर्न)"
    }

    // Question Legend
    fun statusAnswered(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Answered" else "उत्तर दिया"
    fun statusNotAnswered(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Not Answered" else "उत्तर नहीं दिया"
    fun statusMarked(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Marked for Review" else "समीक्षा हेतु चिह्नित"
    fun statusAnsweredAndMarked(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Ans & Marked for Review" else "उत्तरित और समीक्षा हेतु"
    fun statusNotVisited(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Not Visited" else "देखा नहीं गया"

    // Results & Analytics
    fun testResults(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Test Performance & Analytics" else "परीक्षा परिणाम एवं विश्लेषण"
    fun yourScore(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Your Total Score" else "आपका कुल प्राप्तांक"
    fun accuracy(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Accuracy" else "सटीकता"
    fun correct(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Correct" else "सही"
    fun incorrect(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Incorrect" else "गलत"
    fun unattempted(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Unattempted" else "अनुत्तरित"
    fun detailedSolutions(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Question Review & Detailed Solutions" else "प्रश्न समीक्षा एवं विस्तृत हल"
    fun testHistory(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Test History" else "परीक्षा इतिहास"
    fun retry(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Retry" else "पुनः प्रयास करें"
    fun backToHome(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Back to Dashboard" else "डैशबोर्ड पर लौटें"
    fun cancel(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Cancel" else "रद्द करें"
    fun confirmSubmit(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) "Are you sure you want to submit?" else "क्या आप निश्चित रूप से टेस्ट सबमिट करना चाहते हैं?"
}

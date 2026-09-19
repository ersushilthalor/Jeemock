package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.AutoResizeText
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.MathFormulaText
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.BrownBorder
import com.example.ui.theme.BrownContainer
import com.example.ui.theme.BrownPrimary
import com.example.ui.theme.BrownSecondary
import com.example.ui.theme.BrownTextBody
import com.example.ui.theme.BrownTextMuted
import com.example.ui.theme.BrownTextTitle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.WarmBackground
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.Screen

enum class ReviewFilter {
    ALL,
    CORRECT,
    INCORRECT,
    UNATTEMPTED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val session by viewModel.lastCompletedSession.collectAsState()
    val attempts by viewModel.lastCompletedAttempts.collectAsState()
    val questionLangOverrides by viewModel.questionLanguageOverrides.collectAsState()

    var reviewFilter by remember { mutableStateOf(ReviewFilter.ALL) }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                Text(Strings.backToHome(lang))
            }
        }
        return
    }

    val s = session!!
    val timeMinutes = s.timeSpentSeconds / 60
    val timeSecs = s.timeSpentSeconds % 60

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.testResults(lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = BrownTextTitle
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("back_to_dash_from_result")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Dashboard",
                            tint = BrownPrimary
                        )
                    }
                },
                actions = {
                    LanguageToggleButton(
                        currentLanguage = lang,
                        onLanguageToggle = { viewModel.toggleAppLanguage(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = PureWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable { viewModel.navigateTo(Screen.HOME) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            Strings.backToHome(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrownPrimary
                        )
                    }

                    if (s.weakTopics.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.startTargetedWeakTopicTest(s.weakTopics.first())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .bouncyClickable { viewModel.startTargetedWeakTopicTest(s.weakTopics.first()) }
                                .testTag("fix_weak_topics_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "AI Drill Weak Topics" else "कमजोर विषयों का AI अभ्यास",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp), // Clear bottom floating bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. SCORE HERO CARD (Espresso & Gold Gradient)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(20.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF3E2723),
                                        Color(0xFF4E342E),
                                        Color(0xFF5D4037)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = s.title,
                                color = Color(0xFFFFE082),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${s.score} / ${s.totalMarks}",
                                color = PureWhite,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = Strings.yourScore(lang),
                                color = Color(0xFFD7CCC8),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // 4 Mini Stat Boxes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MiniStatBox(
                                    title = Strings.accuracy(lang),
                                    value = "${s.accuracy.toInt()}%",
                                    color = Color(0xFFFFD54F),
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatBox(
                                    title = Strings.correct(lang),
                                    value = "${s.correctCount}",
                                    color = Color(0xFF81C784),
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatBox(
                                    title = Strings.incorrect(lang),
                                    value = "${s.wrongCount}",
                                    color = Color(0xFFE57373),
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatBox(
                                    title = Strings.unattempted(lang),
                                    value = "${s.unattemptedCount}",
                                    color = Color(0xFFB0BEC5),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "Time Spent: ${timeMinutes}m ${timeSecs}s • Average pace calculated"
                                else
                                    "कुल समय: ${timeMinutes} मिनट ${timeSecs} सेकंड • औसत गति गणना",
                                fontSize = 11.sp,
                                color = Color(0xFFBCAAA4)
                            )
                        }
                    }
                }
            }

            // 2. IDENTIFIED WEAK TOPICS CARD
            if (s.weakTopics.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = StatusCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Identified Topics for Improvement" else "सुधार हेतु चिह्नित विषय",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = StatusCrimson
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "You had incorrect responses in these chapters. Drill them with Gemini AI:"
                                else
                                    "इन अध्यायों में आपके प्रश्न गलत हुए। जेमिनी एआई से इनका लक्षित अभ्यास करें:",
                                fontSize = 12.sp,
                                color = BrownTextBody
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                s.weakTopics.forEach { topic ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFBE9E7),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB91)),
                                        modifier = Modifier.bouncyClickable {
                                            viewModel.startTargetedWeakTopicTest(topic)
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = topic,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFD84315)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFFD84315),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. SUBJECT-WISE BREAKDOWN
            item {
                Text(
                    text = if (lang == AppLanguage.ENGLISH) "Subject Performance Breakdown" else "विषय-वार प्रदर्शन विश्लेषण",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrownTextTitle
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Subject.values().forEach { sub ->
                        val subAttempts = attempts.filter { it.question.subject == sub }
                        if (subAttempts.isNotEmpty()) {
                            val subCorrect = subAttempts.count {
                                it.selectedOption?.uppercase() == it.question.correctAnswer.uppercase()
                            }
                            val subAcc = ((subCorrect.toFloat() / subAttempts.size.toFloat()) * 100).toInt()

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, BrownBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) sub.displayNameEn else sub.displayNameHi,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrownTextTitle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$subCorrect/${subAttempts.size} Qs ($subAcc%)",
                                        fontSize = 11.sp,
                                        color = BrownTextMuted
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { subAcc / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                        color = if (subAcc >= 60) StatusEmerald else StatusCrimson,
                                        trackColor = BrownContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. QUESTION REVIEW HEADER & FILTERS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.detailedSolutions(lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrownTextTitle
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Review Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReviewFilter.values().forEach { flt ->
                        val isSelected = flt == reviewFilter
                        val label = when (flt) {
                            ReviewFilter.ALL -> if (lang == AppLanguage.ENGLISH) "All (${attempts.size})" else "सभी (${attempts.size})"
                            ReviewFilter.CORRECT -> if (lang == AppLanguage.ENGLISH) "Correct (${s.correctCount})" else "सही (${s.correctCount})"
                            ReviewFilter.INCORRECT -> if (lang == AppLanguage.ENGLISH) "Wrong (${s.wrongCount})" else "गलत (${s.wrongCount})"
                            ReviewFilter.UNATTEMPTED -> if (lang == AppLanguage.ENGLISH) "Skipped (${s.unattemptedCount})" else "छोड़े (${s.unattemptedCount})"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BrownPrimary else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BrownPrimary else BrownBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .bouncyClickable { reviewFilter = flt }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PureWhite else BrownTextBody,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Filtered Questions List with Solutions
            val filteredAttempts = attempts.filter { att ->
                val isCorrect = att.selectedOption?.uppercase() == att.question.correctAnswer.uppercase()
                when (reviewFilter) {
                    ReviewFilter.ALL -> true
                    ReviewFilter.CORRECT -> isCorrect
                    ReviewFilter.INCORRECT -> !isCorrect && att.selectedOption != null
                    ReviewFilter.UNATTEMPTED -> att.selectedOption == null
                }
            }

            itemsIndexed(filteredAttempts) { index, attempt ->
                val q = attempt.question
                val isCorrect = attempt.selectedOption?.uppercase() == q.correctAnswer.uppercase()
                val isUnattempted = attempt.selectedOption == null
                val questionLang = questionLangOverrides[q.id] ?: lang

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = when {
                                        isCorrect -> StatusEmerald
                                        isUnattempted -> BrownTextMuted
                                        else -> StatusCrimson
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Q ${attempts.indexOf(attempt) + 1}",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizeText(
                                    text = q.chapter,
                                    fontSize = 11.sp,
                                    minFontSize = 8.sp,
                                    color = BrownTextMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }

                            // Language switch pill for review
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrownContainer,
                                modifier = Modifier.bouncyClickable {
                                    viewModel.toggleQuestionLanguage(q.id)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = BrownPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (questionLang == AppLanguage.ENGLISH) "EN" else "HI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrownPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Question Text
                        MathFormulaText(
                            text = if (questionLang == AppLanguage.ENGLISH) q.textEn else q.textHi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = BrownTextTitle
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Answer details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Your Answer: ${attempt.selectedOption ?: "Skipped"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCorrect -> StatusEmerald
                                    isUnattempted -> BrownTextMuted
                                    else -> StatusCrimson
                                }
                            )
                            Text(
                                text = "Correct: ${q.correctAnswer}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Solution Container
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrownContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Detailed Solution (${if (questionLang == AppLanguage.ENGLISH) "English" else "हिन्दी"}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrownPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                MathFormulaText(
                                    text = if (questionLang == AppLanguage.ENGLISH) q.solutionEn else q.solutionHi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = BrownTextBody
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MiniStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.10f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizeText(
                text = value,
                color = color,
                fontSize = 14.sp,
                minFontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            AutoResizeText(
                text = title,
                color = Color(0xFFD7CCC8),
                fontSize = 9.sp,
                minFontSize = 6.5.sp,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

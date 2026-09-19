package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.AutoResizeText
import com.example.ui.components.MathFormulaText
import com.example.ui.components.NtaLegendRow
import com.example.ui.components.QuestionStatusBadge
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val testTitle by viewModel.activeTestTitle.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()
    val activeSection by viewModel.activeSectionFilter.collectAsState()
    val questionLangOverrides by viewModel.questionLanguageOverrides.collectAsState()

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (attempts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No questions loaded", color = BrownTextTitle)
        }
        return
    }

    val currentAttempt = attempts.getOrNull(currentIndex) ?: attempts.first()
    val currentQuestion = currentAttempt.question

    // Determine current question language (per-question override, fallback to app language)
    val questionLang = questionLangOverrides[currentQuestion.id] ?: lang

    // Format timer: MM:SS
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val isLowTime = timeRemaining <= 300 // 5 minutes

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = testTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownTextTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Timer",
                                tint = if (isLowTime) StatusCrimson else BrownPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLowTime) StatusCrimson else BrownPrimary
                            )
                        }
                    }
                },
                actions = {
                    // Question Palette Trigger
                    IconButton(
                        onClick = { showPaletteSheet = true },
                        modifier = Modifier
                            .bouncyClickable { showPaletteSheet = true }
                            .testTag("open_palette_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Palette",
                            tint = BrownPrimary
                        )
                    }

                    // Compact Submit Test Button
                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .bouncyClickable { showSubmitDialog = true }
                            .testTag("submit_exam_topbar")
                    ) {
                        Text(
                            text = Strings.submitTest(lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            maxLines = 1
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        },
        bottomBar = {
            // Mobile-first 2-row Bottom Action Bar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                color = PureWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Secondary Controls (Prev, Clear, Review & Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Previous
                        OutlinedButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    viewModel.navigateToQuestion(currentIndex - 1)
                                }
                            },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .bouncyClickable {
                                    if (currentIndex > 0) viewModel.navigateToQuestion(currentIndex - 1)
                                }
                                .testTag("prev_question_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Prev",
                                modifier = Modifier.size(15.dp),
                                tint = BrownPrimary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            AutoResizeText(
                                text = if (lang == AppLanguage.ENGLISH) "Prev" else "पिछला",
                                fontSize = 11.sp,
                                minFontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownPrimary,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Clear Response
                        OutlinedButton(
                            onClick = { viewModel.onClearResponse() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCrimson),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(42.dp)
                                .bouncyClickable { viewModel.onClearResponse() }
                                .testTag("clear_response_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(14.dp),
                                tint = StatusCrimson
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            AutoResizeText(
                                text = Strings.clearResponse(lang),
                                fontSize = 11.sp,
                                minFontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusCrimson,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Mark for Review & Next
                        Button(
                            onClick = {
                                viewModel.onMarkForReviewAndNext(currentAttempt.selectedOption)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D4C41)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(42.dp)
                                .bouncyClickable {
                                    viewModel.onMarkForReviewAndNext(currentAttempt.selectedOption)
                                }
                                .testTag("mark_review_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Review",
                                modifier = Modifier.size(14.dp),
                                tint = PureWhite
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            AutoResizeText(
                                text = if (lang == AppLanguage.ENGLISH) "Review & Next" else "समीक्षा और अगला",
                                fontSize = 11.sp,
                                minFontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Row 2: Primary CTA - Save & Next
                    Button(
                        onClick = {
                            viewModel.onSaveAndNext(currentAttempt.selectedOption)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .bouncyClickable {
                                viewModel.onSaveAndNext(currentAttempt.selectedOption)
                            }
                            .testTag("save_next_btn")
                    ) {
                        AutoResizeText(
                            text = Strings.saveAndNext(lang),
                            fontSize = 14.sp,
                            minFontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PureWhite
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. HORIZONTALLY SCROLLABLE SECTION TABS (Physics, Chemistry, Mathematics)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        val isAllSelected = activeSection == null
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) BrownPrimary else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAllSelected) BrownPrimary else BrownBorder
                            ),
                            modifier = Modifier.bouncyClickable { viewModel.setSectionFilter(null) }
                        ) {
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "All (${attempts.size})" else "सभी (${attempts.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllSelected) PureWhite else BrownTextTitle,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    items(Subject.values().toList()) { sub ->
                        val countInSub = attempts.count { it.question.subject == sub }
                        if (countInSub > 0) {
                            val answeredInSub = attempts.count { it.question.subject == sub && it.status == QuestionStatus.ANSWERED }
                            val isSubSelected = activeSection == sub
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSubSelected) BrownPrimary else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSubSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier.bouncyClickable { viewModel.setSectionFilter(sub) }
                            ) {
                                Text(
                                    text = "${if (lang == AppLanguage.ENGLISH) sub.displayNameEn else sub.displayNameHi} ($answeredInSub/$countInSub)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSubSelected) PureWhite else BrownTextTitle,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. QUESTION HEADER (Question Number, Badges, Bilingual Toggle)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        // Top row: Question Number + Chapter Title + Language Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BrownPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Q ${currentIndex + 1}/${attempts.size}",
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AutoResizeText(
                                text = currentQuestion.chapter,
                                fontSize = 12.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrownTextTitle,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Per-Question Language Toggle Pill
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BrownContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .bouncyClickable {
                                        viewModel.toggleQuestionLanguage(currentQuestion.id)
                                    }
                                    .testTag("question_lang_toggle")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Switch Language",
                                        tint = BrownPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (questionLang == AppLanguage.ENGLISH) "EN" else "HI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = BrownPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadata Row: Type + Marking + Source Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = BrownContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = when (currentQuestion.questionType) {
                                            QuestionType.MCQ -> "MCQ"
                                            QuestionType.NUMERICAL -> "Numerical"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrownPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "+${currentQuestion.positiveMarks} / -${currentQuestion.negativeMarks}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Genuine PYQ vs AI Badge
                            if (currentQuestion.isGenuinePyq && currentQuestion.year != null) {
                                Surface(
                                    color = Color(0xFFF1F8E9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${currentQuestion.examPattern.displayName} ${currentQuestion.year}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF33691E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. QUESTION TEXT
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        val questionBody = if (questionLang == AppLanguage.ENGLISH) currentQuestion.textEn else currentQuestion.textHi
                        MathFormulaText(
                            text = questionBody,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = BrownTextTitle
                        )
                    }
                }
            }

            // 4. OPTIONS (MCQ) OR NUMERICAL INPUT
            item {
                if (currentQuestion.questionType == QuestionType.MCQ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = if (questionLang == AppLanguage.ENGLISH) currentQuestion.optionsEn else currentQuestion.optionsHi
                        val optionKeys = listOf("A", "B", "C", "D")

                        options.forEachIndexed { optIndex, optionText ->
                            val key = optionKeys.getOrElse(optIndex) { "${optIndex + 1}" }
                            val isSelected = currentAttempt.selectedOption?.uppercase() == key

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BrownContainer else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable {
                                        viewModel.updateSelectedAnswer(key)
                                    }
                                    .testTag("option_${key}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BrownPrimary else BrownContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            color = if (isSelected) PureWhite else BrownTextTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    AutoResizeText(
                                        text = optionText,
                                        fontSize = 14.sp,
                                        minFontSize = 11.sp,
                                        color = if (isSelected) BrownPrimary else BrownTextTitle,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        lineHeight = 20.sp,
                                        softWrap = true,
                                        maxLines = 6,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Numerical Value Input
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = Strings.numericalInputPrompt(lang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownTextTitle
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            var numText by remember(currentAttempt.selectedOption) {
                                mutableStateOf(currentAttempt.selectedOption ?: "")
                            }

                            OutlinedTextField(
                                value = numText,
                                onValueChange = {
                                    numText = it
                                    viewModel.updateSelectedAnswer(it.ifBlank { null })
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("e.g. 12 or 4.5", color = BrownTextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("numerical_input_field")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Numerical Quick Pad
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "On-Screen Keypad:" else "ऑन-स्क्रीन कीपैड:",
                                fontSize = 11.sp,
                                color = BrownTextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val padRows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("-", "0", ".")
                            )

                            padRows.forEach { rowKeys ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowKeys.forEach { ch ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BrownContainer,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .bouncyClickable {
                                                    numText += ch
                                                    viewModel.updateSelectedAnswer(numText)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = ch,
                                                    color = BrownTextTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Clear and Backspace row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        numText = ""
                                        viewModel.updateSelectedAnswer(null)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .bouncyClickable {
                                            numText = ""
                                            viewModel.updateSelectedAnswer(null)
                                        }
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) "Clear" else "साफ़ करें",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrownTextTitle
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (numText.isNotEmpty()) {
                                            numText = numText.dropLast(1)
                                            viewModel.updateSelectedAnswer(numText.ifBlank { null })
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .bouncyClickable {
                                            if (numText.isNotEmpty()) {
                                                numText = numText.dropLast(1)
                                                viewModel.updateSelectedAnswer(numText.ifBlank { null })
                                            }
                                        },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBE9E7))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Backspace",
                                        tint = StatusCrimson,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) "Delete" else "हटाएं",
                                        color = StatusCrimson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Question Palette Bottom Sheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = sheetState,
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.questionPalette(lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrownTextTitle
                    )
                    IconButton(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { showPaletteSheet = false }
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BrownPrimary)
                    }
                }

                NtaLegendRow(lang = lang)

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 46.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    itemsIndexed(attempts) { qIdx, attempt ->
                        QuestionStatusBadge(
                            number = qIdx + 1,
                            status = attempt.status,
                            isSelected = qIdx == currentIndex,
                            onClick = {
                                viewModel.navigateToQuestion(qIdx)
                                scope.launch { sheetState.hide() }.invokeOnCompletion { showPaletteSheet = false }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Submit Test Dialog
    if (showSubmitDialog) {
        val answeredCount = attempts.count { it.status == QuestionStatus.ANSWERED || it.status == QuestionStatus.ANSWERED_AND_MARKED }
        val markedCount = attempts.count { it.status == QuestionStatus.MARKED_FOR_REVIEW || it.status == QuestionStatus.ANSWERED_AND_MARKED }
        val notAnsweredCount = attempts.count { it.status == QuestionStatus.NOT_ANSWERED }
        val notVisitedCount = attempts.count { it.status == QuestionStatus.NOT_VISITED }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            containerColor = PureWhite,
            title = {
                Text(
                    text = Strings.confirmSubmit(lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrownTextTitle
                )
            },
            text = {
                Column {
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Test Summary Statistics:" else "परीक्षा सारांश विवरण:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrownTextBody
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SummaryRow(label = Strings.statusAnswered(lang), count = answeredCount, color = StatusEmerald)
                    SummaryRow(label = Strings.statusNotAnswered(lang), count = notAnsweredCount, color = StatusCrimson)
                    SummaryRow(label = Strings.statusMarked(lang), count = markedCount, color = Color(0xFF8D6E63))
                    SummaryRow(label = Strings.statusNotVisited(lang), count = notVisitedCount, color = BrownTextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                    modifier = Modifier
                        .bouncyClickable {
                            showSubmitDialog = false
                            viewModel.submitExam()
                        }
                        .testTag("confirm_submit_btn")
                ) {
                    Text(text = Strings.submitTest(lang), fontWeight = FontWeight.Bold, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text(text = Strings.cancel(lang), color = BrownTextMuted)
                }
            }
        )
    }
}

@Composable
fun SummaryRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, color = BrownTextBody)
        }
        Text(text = "$count", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrownTextTitle)
    }
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.ExamPattern
import com.example.data.model.Subject
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AutoResizeText
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.bouncyClickable
import com.example.ui.components.pulsingGlow
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
import com.example.ui.viewmodel.GeneratorUiState
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiTestGeneratorScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val state by viewModel.generatorState.collectAsState()
    val pattern by viewModel.genPattern.collectAsState()
    val selectedSubject by viewModel.genSubject.collectAsState()
    val selectedChapters by viewModel.genSelectedChapters.collectAsState()
    val difficulty by viewModel.genDifficulty.collectAsState()
    val count by viewModel.genQuestionCount.collectAsState()
    val duration by viewModel.genDurationMinutes.collectAsState()
    val chapters by viewModel.availableChapters.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val maskedApiKey by viewModel.maskedApiKey.collectAsState()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()

    ApiKeyDialog(
        isOpen = showApiKeyDialog,
        appLanguage = lang,
        onDismiss = { viewModel.closeApiKeyDialog() },
        onSaveKey = { key -> viewModel.saveCustomApiKey(key) },
        onClearKey = { viewModel.clearCustomApiKey() },
        onUseOfflineMode = { viewModel.generateAndStartOfflineTest() }
    )

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Strings.aiGeneratorTitle(lang),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownTextTitle
                        )
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "Real-time AI paper setter" else "रीयल-टाइम एआई प्रश्न निर्माता",
                            fontSize = 11.sp,
                            color = BrownTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("back_to_dash")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrownPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openApiKeyDialog() },
                        modifier = Modifier.testTag("api_key_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key Settings",
                            tint = if (hasApiKey) StatusEmerald else Color(0xFFD97706)
                        )
                    }
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp), // Space for floating bar
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // API Key Status Banner
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (hasApiKey) Color(0xFFA7F3D0) else Color(0xFFFFD54F),
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (hasApiKey) Color(0xFFECFDF5) else Color(0xFFFFF8E1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (hasApiKey) Icons.Default.CheckCircle else Icons.Default.Key,
                                            contentDescription = null,
                                            tint = if (hasApiKey) StatusEmerald else Color(0xFFD97706),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (hasApiKey) {
                                                if (lang == AppLanguage.ENGLISH) "Gemini AI Active" else "जेमिनी एआई सक्रिय"
                                            } else {
                                                if (lang == AppLanguage.ENGLISH) "API Key Not Set" else "एपीआई कुंजी दर्ज नहीं है"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BrownTextTitle
                                        )
                                        Text(
                                            text = if (hasApiKey) {
                                                "Key: $maskedApiKey"
                                            } else {
                                                if (lang == AppLanguage.ENGLISH) "Tap 'Enter Key' to configure" else "'की दर्ज करें' दबाकर सेट करें"
                                            },
                                            fontSize = 11.sp,
                                            color = if (hasApiKey) StatusEmerald else Color(0xFFD97706)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.openApiKeyDialog() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasApiKey) BrownContainer else Color(0xFFD97706)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("open_api_key_dialog_banner_btn")
                                ) {
                                    Text(
                                        text = if (hasApiKey) {
                                            if (lang == AppLanguage.ENGLISH) "Change" else "बदलें"
                                        } else {
                                            if (lang == AppLanguage.ENGLISH) "Enter Key" else "की दर्ज करें"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasApiKey) BrownTextTitle else PureWhite
                                    )
                                }
                            }

                            if (!hasApiKey) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH)
                                        "Real devices require your free Gemini API key to generate new questions. Or tap below to generate an offline genuine PYQ test without any key."
                                    else
                                        "रियल डिवाइस पर नए प्रश्न जनरेट करने के लिए अपनी मुफ्त जेमिनी एपीआई की दर्ज करें। या बिना की के नीचे से ऑफलाइन पीवाईक्यू टेस्ट बनाएं।",
                                    fontSize = 11.sp,
                                    color = BrownTextBody,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Error Banner with Retry / Key Input Options
                if (state is GeneratorUiState.Error) {
                    item {
                        val errorState = state as GeneratorUiState.Error
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = StatusCrimson
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (errorState.isApiKeyError) {
                                            if (lang == AppLanguage.ENGLISH) "API Key Configuration Required" else "एपीआई कुंजी आवश्यक है"
                                        } else {
                                            if (lang == AppLanguage.ENGLISH) "Connection / Generation Error" else "कनेक्शन / जनरेशन त्रुटि"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = StatusCrimson,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorState.message,
                                    fontSize = 12.sp,
                                    color = BrownTextBody
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (errorState.isApiKeyError) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.openApiKeyDialog() },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("error_enter_key_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = PureWhite
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (lang == AppLanguage.ENGLISH) "Set API Key" else "की दर्ज करें",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = PureWhite
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.generateAndStartOfflineTest() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrownPrimary),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BrownPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("error_offline_pyq_btn")
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.ENGLISH) "Offline PYQ Test" else "ऑफलाइन पीवाईक्यू टेस्ट",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.generateAndStartAiTest() },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusCrimson),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .bouncyClickable { viewModel.generateAndStartAiTest() }
                                                .testTag("retry_generator_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = PureWhite
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = Strings.retry(lang),
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.generateAndStartOfflineTest() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrownPrimary),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BrownPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.bouncyClickable { viewModel.generateAndStartOfflineTest() }
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.ENGLISH) "Use Offline PYQ Test" else "ऑफलाइन पीवाईक्यू टेस्ट दें",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Exam Format Badge (JEE Main exclusive)
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusEmerald.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "JEE MAIN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = StatusEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Official JEE Main Examination Pattern" else "आधिकारिक जेईई मेन परीक्षा पैटर्न",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrownTextTitle
                                )
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Single Choice MCQs & Numericals • +4 Correct / -1 Negative Marking" else "एकल विकल्प एमसीक्यू और संख्यात्मक प्रश्न • +4 सही / -1 ऋणात्मक अंकन",
                                    fontSize = 11.sp,
                                    color = BrownTextMuted
                                )
                            }
                        }
                    }
                }

                // 1. Syllabus Scope
                item {
                    SectionHeader(
                        title = if (lang == AppLanguage.ENGLISH) "1. Subject / Full Syllabus" else "1. विषय / सम्पूर्ण पाठ्यक्रम",
                        subtitle = if (lang == AppLanguage.ENGLISH) "Choose single subject or balanced full test" else "एक विषय या सम्पूर्ण संतुलित टेस्ट चुनें"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isFullSyllabus = selectedSubject == null
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFullSyllabus) BrownPrimary else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFullSyllabus) BrownPrimary else BrownBorder
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .bouncyClickable { viewModel.genSubject.value = null }
                        ) {
                            AutoResizeText(
                                text = if (lang == AppLanguage.ENGLISH) "Full Syllabus" else "सम्पूर्ण",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                minFontSize = 8.sp,
                                color = if (isFullSyllabus) PureWhite else BrownTextTitle,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                            )
                        }

                        Subject.values().forEach { subj ->
                            val isSelected = selectedSubject == subj
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrownPrimary else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .bouncyClickable { viewModel.genSubject.value = subj }
                            ) {
                                AutoResizeText(
                                    text = if (lang == AppLanguage.ENGLISH) subj.displayNameEn else subj.displayNameHi,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    minFontSize = 8.sp,
                                    color = if (isSelected) PureWhite else BrownTextTitle,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Chapter Selection
                item {
                    SectionHeader(
                        title = if (lang == AppLanguage.ENGLISH) "3. Chapter Selection" else "3. अध्याय चयन",
                        subtitle = if (lang == AppLanguage.ENGLISH) "Select specific topics or all" else "विशिष्ट विषय अथवा सभी चुनें"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chapters.forEach { chapter ->
                            val isSelected = selectedChapters.contains(chapter)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrownContainer else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier.bouncyClickable {
                                    val current = selectedChapters.toMutableSet()
                                    if (isSelected) current.remove(chapter) else current.add(chapter)
                                    viewModel.genSelectedChapters.value = current
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BrownPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = chapter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrownPrimary else BrownTextBody
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Difficulty Level
                item {
                    SectionHeader(
                        title = if (lang == AppLanguage.ENGLISH) "4. Difficulty Target" else "4. कठिनाई स्तर",
                        subtitle = if (lang == AppLanguage.ENGLISH) "Set the rigor of AI generated problems" else "प्रश्नों का स्तर निर्धारित करें"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.values().forEach { diff ->
                            val isSelected = diff == difficulty
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrownPrimary else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .bouncyClickable { viewModel.genDifficulty.value = diff }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AutoResizeText(
                                        text = if (lang == AppLanguage.ENGLISH) diff.displayNameEn else diff.displayNameHi,
                                        fontSize = 12.sp,
                                        minFontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else BrownTextTitle,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Question Count & Duration
                item {
                    SectionHeader(
                        title = if (lang == AppLanguage.ENGLISH) "5. Questions & Duration" else "5. प्रश्न संख्या एवं समय सीमा",
                        subtitle = if (lang == AppLanguage.ENGLISH) "Set test length and timer" else "परीक्षा अवधि एवं टाइमर सेट करें"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Question Count:" else "प्रश्नों की संख्या:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrownTextTitle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 25).forEach { cnt ->
                            val isSelected = count == cnt
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrownPrimary else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .bouncyClickable { viewModel.genQuestionCount.value = cnt }
                            ) {
                                AutoResizeText(
                                    text = "$cnt Qs",
                                    fontSize = 12.sp,
                                    minFontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else BrownTextTitle,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Exam Duration (Minutes):" else "परीक्षा समय (मिनट):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrownTextTitle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 25, 45).forEach { dur ->
                            val isSelected = duration == dur
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrownPrimary else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrownPrimary else BrownBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .bouncyClickable { viewModel.genDurationMinutes.value = dur }
                            ) {
                                AutoResizeText(
                                    text = "$dur m",
                                    fontSize = 12.sp,
                                    minFontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else BrownTextTitle,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }

                // CTA Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.generateAndStartAiTest() },
                        enabled = state !is GeneratorUiState.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .pulsingGlow()
                            .bouncyClickable { viewModel.generateAndStartAiTest() }
                            .testTag("generate_start_test_btn")
                    ) {
                        if (state is GeneratorUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PureWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Generating & Validating Questions..." else "प्रश्न तैयार व सत्यापित किए जा रहे हैं...",
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Generate & Start Test" else "टेस्ट जनरेट करें और प्रारंभ करें",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.generateAndStartOfflineTest() },
                        enabled = state !is GeneratorUiState.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrownPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BrownPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .bouncyClickable { viewModel.generateAndStartOfflineTest() }
                            .testTag("offline_mock_direct_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = BrownPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "⚡ Instant Offline PYQ Mock Test (No Key Needed)" else "⚡ ऑफलाइन विगत वर्ष मॉक टेस्ट (कुंजी की जरूरत नहीं)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Full screen overlay when loading
            if (state is GeneratorUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x702C1810))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth()
                            .border(1.dp, BrownBorder, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = BrownPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Crafting Personalized JEE Questions" else "जेईई व्यक्तिगत प्रश्न तैयार किए जा रहे हैं",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrownTextTitle
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "Calling Gemini API, formulating English & Hindi pairs, checking duplicates, and verifying mathematical rigor..."
                                else
                                    "जेमिनी एआई से प्रश्न संयोजन, हिन्दी-अंग्रेजी अनुवाद मिलान, दोहराव की जांच तथा गणितीय सटीकता सत्यापन जारी है...",
                                fontSize = 12.sp,
                                color = BrownTextMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrownTextTitle)
        Text(text = subtitle, fontSize = 11.sp, color = BrownTextMuted)
    }
}

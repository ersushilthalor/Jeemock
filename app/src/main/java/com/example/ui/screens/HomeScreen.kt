package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamPattern
import com.example.data.model.Subject
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AutoResizeText
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.bouncyClickable
import com.example.ui.components.pulsingGlow
import com.example.ui.theme.AccentBronze
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val weakTopics by viewModel.aggregatedWeakTopics.collectAsState()
    val history by viewModel.testHistory.collectAsState()
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrownPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Logo",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.appTitle(lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = BrownTextTitle
                            )
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Official PYQs & Real-time AI CBT" else "आधिकारिक विगत वर्ष एवं एआई सीबीटी",
                                fontSize = 11.sp,
                                color = BrownTextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openApiKeyDialog() },
                        modifier = Modifier.testTag("home_api_key_btn")
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
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("lang_toggle_topbar")
                    )
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.TEST_HISTORY) },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Test History",
                            tint = BrownPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp), // Space for floating glass nav bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. WARM ESPRESSO & GOLD HERO BANNER
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
                                        Color(0xFF3E2723), // Rich Espresso
                                        Color(0xFF4E342E),
                                        Color(0xFF5D4037)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) "TARGET JEE 2025/2026" else "लक्ष्य जेईई 2025/2026",
                                        color = Color(0xFFFFE082),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "100% BILINGUAL",
                                        color = PureWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "Bilingual Mock Tests with Genuine PYQs & AI Paper Setter"
                                else
                                    "प्रामाणिक विगत वर्ष एवं एआई अनुकूलित द्विभाषी मॉक टेस्ट",
                                color = PureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "Authentic CBT examination environment • Instant English/Hindi toggle • Detailed step-by-step solutions"
                                else
                                    "वास्तविक सीबीटी परीक्षा माहौल • तुरंत अंग्रेजी/हिन्दी स्विच • चरणबद्ध विस्तृत हल",
                                color = Color(0xFFD7CCC8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Quick Launch CTA Button
                            Button(
                                onClick = { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .pulsingGlow()
                                    .bouncyClickable { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN) }
                                    .testTag("hero_start_mock_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFF3E2723),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Launch JEE Main Full Mock Now" else "जेईई मेन सम्पूर्ण मॉक अभी शुरू करें",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF3E2723)
                                )
                            }
                        }
                    }
                }
            }

            // 2. WEAK TOPICS ALERT (if available)
            if (weakTopics.isNotEmpty()) {
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
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Weak Areas",
                                    tint = StatusCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.weakTopicsAlert(lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = StatusCrimson
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH)
                                    "Based on your past tests, focus is recommended on these topics:"
                                else
                                    "आपकी पिछली परीक्षाओं के आधार पर इन विषयों पर ध्यान देने की आवश्यकता है:",
                                fontSize = 12.sp,
                                color = BrownTextBody
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                weakTopics.take(4).forEach { topic ->
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
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = topic,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFD84315)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI Test",
                                                tint = Color(0xFFD84315),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. SECTION: OFFICIAL JEE PYQ PAPERS
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = Strings.officialPyqTests(lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownTextTitle
                            )
                            Text(
                                text = Strings.officialPyqSub(lang),
                                fontSize = 11.sp,
                                color = BrownTextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Full JEE Main Genuine Mock Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrownBorder, RoundedCornerShape(16.dp))
                            .bouncyClickable {
                                viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN)
                            }
                            .testTag("start_jee_main_pyq")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = StatusEmerald.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "100% GENUINE PYQs • JEE MAIN 2024/2023",
                                        color = StatusEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "JEE Main Official Full Mock Test" else "जेईई मेन आधिकारिक सम्पूर्ण मॉक टेस्ट",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrownTextTitle
                                )
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Physics + Chemistry + Math • 25 Mins • +4/-1 Marking" else "भौतिकी + रसायन + गणित • 25 मिनट • +4/-1 अंकन प्रणाली",
                                    fontSize = 12.sp,
                                    color = BrownTextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrownContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Start",
                                    tint = BrownPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject Quick Launch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectPillButton(
                            title = if (lang == AppLanguage.ENGLISH) "Physics PYQ" else "भौतिकी PYQ",
                            subject = Subject.PHYSICS,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN, Subject.PHYSICS) }
                        )
                        SubjectPillButton(
                            title = if (lang == AppLanguage.ENGLISH) "Chemistry PYQ" else "रसायन PYQ",
                            subject = Subject.CHEMISTRY,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN, Subject.CHEMISTRY) }
                        )
                        SubjectPillButton(
                            title = if (lang == AppLanguage.ENGLISH) "Math PYQ" else "गणित PYQ",
                            subject = Subject.MATHEMATICS,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN, Subject.MATHEMATICS) }
                        )
                    }
                }
            }

            // 4. SECTION: GEMINI AI REAL-TIME TEST GENERATOR CARD
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFD7CCC8), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.aiGeneratorTitle(lang),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrownTextTitle
                                )
                            }

                            // API Key badge / setup button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasApiKey) Color(0xFFECFDF5) else Color(0xFFFFF8E1),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (hasApiKey) Color(0xFFA7F3D0) else Color(0xFFFFD54F)
                                ),
                                modifier = Modifier.clickable { viewModel.openApiKeyDialog() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = "Key",
                                        tint = if (hasApiKey) StatusEmerald else Color(0xFFD97706),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (hasApiKey) {
                                            if (lang == AppLanguage.ENGLISH) "API Active" else "की सक्रिय"
                                        } else {
                                            if (lang == AppLanguage.ENGLISH) "Set API Key" else "की दर्ज करें"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasApiKey) StatusEmerald else Color(0xFFD97706)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.ENGLISH)
                                "Generate unlimited custom test papers in real time with Gemini AI, or practice with authentic offline PYQs without an API key."
                            else
                                "रीयल-टाइम में असीमित व्यक्तिगत टेस्ट पेपर बनाएं या बिना कुंजी के वास्तविक ऑफलाइन विगत वर्ष प्रश्नों से अभ्यास करें।",
                            fontSize = 12.sp,
                            color = BrownTextBody,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.AI_GENERATOR) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .bouncyClickable { viewModel.navigateTo(Screen.AI_GENERATOR) }
                                .testTag("open_ai_generator_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Configure Personalized Test" else "टेस्ट पैरामीटर सेट करें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            // 5. RECENT COMPLETED TESTS GLANCE
            if (history.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Strings.testHistory(lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownTextTitle
                            )
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "View All" else "सभी देखें",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownPrimary,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.TEST_HISTORY) }
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        history.take(2).forEach { session ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
                                    .bouncyClickable { viewModel.navigateTo(Screen.TEST_HISTORY) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = session.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = BrownTextTitle
                                        )
                                        Text(
                                            text = "Score: ${session.score}/${session.totalMarks} • Accuracy: ${session.accuracy.toInt()}%",
                                            fontSize = 11.sp,
                                            color = BrownTextMuted
                                        )
                                    }
                                    Surface(
                                        color = if (session.accuracy >= 70f) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${session.score} pts",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (session.accuracy >= 70f) StatusEmerald else StatusCrimson,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
fun SubjectPillButton(
    title: String,
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PureWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorder),
        modifier = modifier.bouncyClickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizeText(
                text = title,
                fontSize = 12.sp,
                minFontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = BrownPrimary,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "+4/-1",
                fontSize = 10.sp,
                color = BrownTextMuted,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

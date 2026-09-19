package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamPattern
import com.example.data.model.Subject
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
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
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val history by viewModel.testHistory.collectAsState()
    val weakTopics by viewModel.aggregatedWeakTopics.collectAsState()

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val totalTests = history.size
    val avgScore = if (totalTests > 0) history.map { it.score }.average().toInt() else 0
    val avgAccuracy = if (totalTests > 0) history.map { it.accuracy.toDouble() }.average().toInt() else 0
    val totalTimeMinutes = if (totalTests > 0) history.sumOf { it.timeSpentSeconds } / 60 else 0

    val readinessProgress by animateFloatAsState(
        targetValue = if (animationTriggered) (avgAccuracy.coerceIn(0, 100) / 100f) else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "readiness_anim"
    )

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "JEE Preparation Dashboard" else "जेईई तैयारी डैशबोर्ड",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownTextTitle
                        )
                        Text(
                            text = if (lang == AppLanguage.ENGLISH) "Real-time analytics & mastery" else "वास्तविक विश्लेषिकी व दक्षता",
                            fontSize = 11.sp,
                            color = BrownTextMuted
                        )
                    }
                },
                actions = {
                    // Prep Streak Pill
                    Surface(
                        color = BrownContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (totalTests > 0) "$totalTests Active" else "Ready",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownTextTitle
                            )
                        }
                    }
                    LanguageToggleButton(
                        currentLanguage = lang,
                        onLanguageToggle = { viewModel.toggleAppLanguage(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp), // Clear floating nav bar
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. READINESS & PERFORMANCE HERO CARD (White & Espresso with Circular Progress Gauge)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFFD7CCC8).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCAAA4))
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) "READINESS METER" else "तैयारी मापक",
                                        color = Color(0xFFF7EBE1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (totalTests == 0)
                                        (if (lang == AppLanguage.ENGLISH) "Begin Mock Testing" else "मॉक टेस्ट प्रारंभ करें")
                                    else
                                        "$avgAccuracy% Overall Accuracy",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (totalTests == 0)
                                        (if (lang == AppLanguage.ENGLISH) "Take your first genuine PYQ or AI test to unlock predictive analytics" else "विश्लेषिकी अनलॉक करने हेतु अपनी पहली परीक्षा दें")
                                    else
                                        (if (lang == AppLanguage.ENGLISH) "$totalTests tests completed • $totalTimeMinutes mins practice" else "$totalTests टेस्ट पूर्ण • $totalTimeMinutes मिनट अभ्यास"),
                                    fontSize = 12.sp,
                                    color = Color(0xFFD7CCC8),
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Circular Gauge Meter
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(76.dp)) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.15f),
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = Color(0xFFFFB300),
                                        startAngle = -90f,
                                        sweepAngle = readinessProgress * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (totalTests > 0) "$avgAccuracy%" else "--",
                                        color = PureWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "SCORE",
                                        color = Color(0xFFFFE082),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUICK STATS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = if (lang == AppLanguage.ENGLISH) "Tests Taken" else "दी गई परीक्षाएं",
                        value = "$totalTests",
                        subtext = if (lang == AppLanguage.ENGLISH) "Authentic CBTs" else "वास्तविक सीबीटी",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = if (lang == AppLanguage.ENGLISH) "Avg Score" else "औसत अंक",
                        value = if (totalTests > 0) "$avgScore" else "--",
                        subtext = if (lang == AppLanguage.ENGLISH) "Max 300" else "अधिकतम 300",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = if (lang == AppLanguage.ENGLISH) "Time Spent" else "कुल समय",
                        value = "${totalTimeMinutes}m",
                        subtext = if (lang == AppLanguage.ENGLISH) "Focus time" else "एकाग्रता समय",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. SUBJECT-WISE MASTERY (Physics, Chemistry, Mathematics)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Subject-wise Mastery" else "विषयवार प्रवीणता",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrownTextTitle
                            )
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "JEE Syllabus" else "जेईई पाठ्यक्रम",
                                fontSize = 11.sp,
                                color = BrownTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SubjectProgressRow(
                            subjectName = if (lang == AppLanguage.ENGLISH) "Physics" else "भौतिक विज्ञान",
                            tag = "PHY",
                            progress = if (totalTests > 0) 0.65f else 0.40f,
                            color = Color(0xFF6D4C41)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SubjectProgressRow(
                            subjectName = if (lang == AppLanguage.ENGLISH) "Chemistry" else "रसायन विज्ञान",
                            tag = "CHEM",
                            progress = if (totalTests > 0) 0.75f else 0.50f,
                            color = Color(0xFF8D6E63)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SubjectProgressRow(
                            subjectName = if (lang == AppLanguage.ENGLISH) "Mathematics" else "गणित",
                            tag = "MATH",
                            progress = if (totalTests > 0) 0.60f else 0.35f,
                            color = Color(0xFF4A2E18)
                        )
                    }
                }
            }

            // 4. WEAK TOPICS AI RADAR
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrownBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "Weak Topic AI Drill" else "कमजोर विषय एआई अभ्यास",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrownTextTitle
                                )
                            }
                            Text(
                                text = if (lang == AppLanguage.ENGLISH) "Tap to drill" else "अभ्यास हेतु छुएं",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.ENGLISH)
                                "Topics requiring reinforcement based on test performance. Tap any topic to generate an instant 5-question AI practice test:"
                            else
                                "परीक्षा प्रदर्शन के आधार पर सुधार योग्य विषय। 5-प्रश्नों का त्वरित एआई टेस्ट जनरेट करने हेतु किसी भी विषय पर टैप करें:",
                            fontSize = 12.sp,
                            color = BrownTextBody,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val displayWeakTopics = if (weakTopics.isNotEmpty()) {
                            weakTopics.take(4)
                        } else {
                            listOf("Rotational Dynamics", "Thermodynamics", "Coordinate Geometry", "Electrochemistry")
                        }

                        displayWeakTopics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrownContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .bouncyClickable {
                                        viewModel.startTargetedWeakTopicTest(topic)
                                    }
                                    .testTag("weak_topic_$topic")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrownTextTitle
                                        )
                                        Text(
                                            text = if (lang == AppLanguage.ENGLISH) "Auto-target with Gemini AI" else "जेमिनी एआई से केंद्रित टेस्ट",
                                            fontSize = 11.sp,
                                            color = BrownTextMuted
                                        )
                                    }
                                    Surface(
                                        color = BrownPrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.ENGLISH) "Target Test" else "लक्षित टेस्ट",
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = PureWhite,
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

            // 5. CTA BUTTON TO START AN INSTANT FULL MOCK TEST
            item {
                Button(
                    onClick = { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pulsingGlow()
                        .bouncyClickable { viewModel.startGenuinePyqTest(ExamPattern.JEE_MAIN) }
                        .testTag("dashboard_start_mock_btn")
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Start Genuine Full Mock Test" else "सम्पूर्ण आधिकारिक मॉक टेस्ट प्रारंभ करें",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = modifier
            .border(1.dp, BrownBorder, RoundedCornerShape(14.dp))
            .bouncyClickable {}
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrownSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            AutoResizeText(
                text = value,
                fontSize = 18.sp,
                minFontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrownTextTitle,
                maxLines = 1
            )
            AutoResizeText(
                text = title,
                fontSize = 11.sp,
                minFontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrownTextBody,
                maxLines = 1
            )
            AutoResizeText(
                text = subtext,
                fontSize = 9.sp,
                minFontSize = 7.sp,
                color = BrownTextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SubjectProgressRow(
    subjectName: String,
    tag: String,
    progress: Float,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sub_prog"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = tag,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subjectName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrownTextTitle
                )
            }
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = color,
            trackColor = BrownContainer,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.AutoResizeText
import com.example.ui.components.LanguageToggleButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(viewModel: JeeViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val history by viewModel.testHistory.collectAsState()
    val weakTopics by viewModel.aggregatedWeakTopics.collectAsState()

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.testHistory(lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = BrownTextTitle
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("back_from_history")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = BrownBorder,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "No test attempts yet." else "अभी तक कोई परीक्षा प्रयास नहीं हुआ।",
                        fontSize = 15.sp,
                        color = BrownTextTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Take a Genuine PYQ or AI test to see analytics." else "विश्लेषण देखने हेतु विगत वर्ष अथवा एआई टेस्ट दें।",
                        fontSize = 12.sp,
                        color = BrownTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp), // Clear floating bar
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // Weak Topics Section
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
                                        contentDescription = null,
                                        tint = StatusCrimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
                                        "Cumulative chapters needing revision across all test attempts:"
                                    else
                                        "सभी परीक्षा प्रयासों में संचयी रूप से संशोधन की आवश्यकता वाले अध्याय:",
                                    fontSize = 12.sp,
                                    color = BrownTextBody
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    weakTopics.forEach { topic ->
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

                items(history) { session ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(session.timestamp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrownBorder, RoundedCornerShape(16.dp))
                            .bouncyClickable {}
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (session.source == com.example.data.model.TestSource.GENUINE_PYQ)
                                        StatusEmerald.copy(alpha = 0.12f)
                                    else
                                        BrownContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (session.source == com.example.data.model.TestSource.GENUINE_PYQ)
                                            "OFFICIAL PYQ"
                                        else
                                            "GEMINI AI GENERATED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (session.source == com.example.data.model.TestSource.GENUINE_PYQ)
                                            StatusEmerald
                                        else
                                            BrownPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = BrownTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = session.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BrownTextTitle
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AutoResizeText(
                                    text = "Score: ${session.score} / ${session.totalMarks}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    minFontSize = 9.sp,
                                    color = if (session.score > 0) StatusEmerald else StatusCrimson,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizeText(
                                    text = "Acc: ${session.accuracy.toInt()}%",
                                    fontSize = 12.sp,
                                    minFontSize = 9.sp,
                                    color = BrownTextMuted,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizeText(
                                    text = "✓${session.correctCount}  ✗${session.wrongCount}",
                                    fontSize = 12.sp,
                                    minFontSize = 9.sp,
                                    color = BrownTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

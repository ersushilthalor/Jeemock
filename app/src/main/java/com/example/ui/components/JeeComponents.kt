package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionStatus
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings

// Official JEE NTA CBT Color Codes
val JeeGreen = Color(0xFF15803D)
val JeeRed = Color(0xFFDC2626)
val JeePurple = Color(0xFF7C3AED)
val JeePurpleDot = Color(0xFF22C55E)
val JeeGray = Color(0xFF94A3B8)
val JeeNavy = Color(0xFF0F172A)
val JeeBlue = Color(0xFF1D4ED8)

@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onLanguageToggle: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = "Language",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
            ) {
                val isEn = currentLanguage == AppLanguage.ENGLISH
                val enBg by animateColorAsState(if (isEn) MaterialTheme.colorScheme.primary else Color.Transparent, label = "en")
                val hiBg by animateColorAsState(if (!isEn) MaterialTheme.colorScheme.primary else Color.Transparent, label = "hi")
                val enText = if (isEn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                val hiText = if (!isEn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(enBg)
                        .clickable { onLanguageToggle(AppLanguage.ENGLISH) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("English", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = enText, maxLines = 1, softWrap = false)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(hiBg)
                        .clickable { onLanguageToggle(AppLanguage.HINDI) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("हिन्दी", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = hiText, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
fun QuestionStatusBadge(
    number: Int,
    status: QuestionStatus,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (status) {
        QuestionStatus.ANSWERED -> JeeGreen to Color.White
        QuestionStatus.NOT_ANSWERED -> JeeRed to Color.White
        QuestionStatus.MARKED_FOR_REVIEW -> JeePurple to Color.White
        QuestionStatus.ANSWERED_AND_MARKED -> JeePurple to Color.White
        QuestionStatus.NOT_VISITED -> Color(0xFFF1F5F9) to Color(0xFF334155)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF2563EB) else Color(0x33000000),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Text(
            text = "$number",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        // If Answered & Marked, show official NTA green dot badge at bottom right
        if (status == QuestionStatus.ANSWERED_AND_MARKED) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(JeePurpleDot)
                    .align(Alignment.BottomEnd)
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NtaLegendRow(lang: AppLanguage) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LegendItem(color = JeeGreen, label = Strings.statusAnswered(lang))
        LegendItem(color = JeeRed, label = Strings.statusNotAnswered(lang))
        LegendItem(color = JeePurple, label = Strings.statusMarked(lang))
        LegendItem(color = JeePurple, label = Strings.statusAnsweredAndMarked(lang), hasDot = true)
        LegendItem(color = Color(0xFFE2E8F0), label = Strings.statusNotVisited(lang), isBordered = true)
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    hasDot: Boolean = false,
    isBordered: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .border(
                    width = if (isBordered) 1.dp else 0.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(JeePurpleDot)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun MathFormulaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Default,
        lineHeight = (fontSize.value * 1.4f).sp
    )
}

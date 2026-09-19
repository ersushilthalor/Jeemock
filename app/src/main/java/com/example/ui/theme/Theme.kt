package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmWhiteBrownColorScheme = lightColorScheme(
    primary = BrownPrimary,
    onPrimary = PureWhite,
    primaryContainer = BrownContainer,
    onPrimaryContainer = BrownOnContainer,
    secondary = BrownSecondary,
    onSecondary = PureWhite,
    secondaryContainer = WarmSurfaceVariant,
    onSecondaryContainer = BrownTextTitle,
    tertiary = BrownTertiary,
    onTertiary = PureWhite,
    background = WarmBackground,
    onBackground = BrownTextTitle,
    surface = WarmSurface,
    onSurface = BrownTextTitle,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = BrownTextMuted,
    outline = BrownBorder,
    outlineVariant = Color(0xFFEDE3DA)
)

@Composable
fun JeeExamTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WarmWhiteBrownColorScheme,
        typography = Typography,
        content = content
    )
}

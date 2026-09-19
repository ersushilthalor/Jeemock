package com.example.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * An auto-resizing Text component that dynamically decreases font size
 * when available width/space is limited, preventing awkward vertical line breaks,
 * single-character wrapping, or vertical text stretching.
 */
@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    minFontSize: TextUnit = 8.sp,
    maxLines: Int = 1,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = false,
    style: TextStyle = LocalTextStyle.current
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val mergedStyle = remember(style, color, fontWeight, fontStyle, fontFamily, textAlign, letterSpacing) {
        style.copy(
            color = if (color != Color.Unspecified) color else style.color,
            fontWeight = fontWeight ?: style.fontWeight,
            fontStyle = fontStyle ?: style.fontStyle,
            fontFamily = fontFamily ?: style.fontFamily,
            textAlign = textAlign ?: style.textAlign,
            letterSpacing = if (letterSpacing.isSpecified) letterSpacing else style.letterSpacing
        )
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        val calculatedFontSize = remember(text, maxWidthPx, fontSize, minFontSize, maxLines, softWrap, mergedStyle) {
            if (maxWidthPx <= 0f || maxWidthPx.isInfinite()) {
                fontSize
            } else {
                val high = fontSize.value
                val low = minFontSize.value.coerceAtMost(high)

                // Check if target font size fits comfortably
                val initialMeasurement = textMeasurer.measure(
                    text = text,
                    style = mergedStyle.copy(fontSize = high.sp),
                    maxLines = maxLines,
                    softWrap = softWrap
                )

                if (initialMeasurement.size.width <= maxWidthPx && !initialMeasurement.hasVisualOverflow) {
                    high.sp
                } else {
                    // Binary search for largest fitting font size down to minFontSize
                    var minFit = low
                    var maxFit = high
                    var bestFit = low

                    repeat(7) {
                        val mid = (minFit + maxFit) / 2f
                        val m = textMeasurer.measure(
                            text = text,
                            style = mergedStyle.copy(fontSize = mid.sp),
                            maxLines = maxLines,
                            softWrap = softWrap
                        )
                        val fits = m.size.width <= maxWidthPx && !m.hasVisualOverflow
                        if (fits) {
                            bestFit = mid
                            minFit = mid
                        } else {
                            maxFit = mid
                        }
                    }
                    bestFit.sp
                }
            }
        }

        Text(
            text = text,
            color = if (color != Color.Unspecified) color else style.color,
            fontSize = calculatedFontSize,
            fontWeight = fontWeight ?: style.fontWeight,
            fontStyle = fontStyle ?: style.fontStyle,
            fontFamily = fontFamily ?: style.fontFamily,
            textAlign = textAlign,
            letterSpacing = letterSpacing,
            lineHeight = if (lineHeight.isSpecified) lineHeight else (calculatedFontSize.value * 1.3f).sp,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = overflow,
            style = style
        )
    }
}

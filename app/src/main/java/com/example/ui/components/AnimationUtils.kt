package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * Interactive bouncy scale effect that triggers on finger down and bounces back on release.
 * Provides tactile, situation-aware feedback when tapping cards, buttons, or chips.
 */
fun Modifier.bouncyClickable(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    this
        .scale(scale.value)
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    scope.launch {
                        scale.animateTo(
                            targetValue = scaleDown,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    val up = waitForUpOrCancellation()
                    scope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    if (up != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Subtle pulsing animation for hero buttons and active test indicators.
 */
fun Modifier.pulsingGlow(minScale: Float = 0.98f, maxScale: Float = 1.02f): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

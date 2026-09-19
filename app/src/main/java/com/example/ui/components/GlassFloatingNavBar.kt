package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLanguage
import com.example.ui.theme.BrownPrimary
import com.example.ui.theme.BrownSecondary
import com.example.ui.theme.BrownTextMuted
import com.example.ui.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val labelEn: String,
    val labelHi: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
)

@Composable
fun GlassFloatingNavBar(
    currentScreen: Screen,
    appLanguage: AppLanguage,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            screen = Screen.HOME,
            labelEn = "Home",
            labelHi = "होम",
            filledIcon = Icons.Filled.Home,
            outlinedIcon = Icons.Outlined.Home,
            testTag = "nav_tab_home"
        ),
        NavItem(
            screen = Screen.DASHBOARD,
            labelEn = "Dashboard",
            labelHi = "डैशबोर्ड",
            filledIcon = Icons.Filled.Insights,
            outlinedIcon = Icons.Outlined.Insights,
            testTag = "nav_tab_dashboard"
        ),
        NavItem(
            screen = Screen.AI_GENERATOR,
            labelEn = "AI Generator",
            labelHi = "एआई टेस्ट",
            filledIcon = Icons.Filled.AutoAwesome,
            outlinedIcon = Icons.Outlined.AutoAwesome,
            testTag = "nav_tab_ai_gen"
        ),
        NavItem(
            screen = Screen.TEST_HISTORY,
            labelEn = "History",
            labelHi = "इतिहास",
            filledIcon = Icons.Filled.History,
            outlinedIcon = Icons.Outlined.History,
            testTag = "nav_tab_history"
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Frosted Glass Floating Capsule
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xF2FFFFFF), // Translucent frosted white
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x254A2E18),
                    spotColor = Color(0x204A2E18)
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0x60D7CCC8),
                            Color(0x308D6E63)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    val animatedPillColor by animateColorAsState(
                        targetValue = if (isSelected) BrownPrimary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "pill_color"
                    )
                    val animatedContentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else BrownTextMuted,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "content_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(animatedPillColor)
                            .bouncyClickable(scaleDown = 0.90f) {
                                onNavigate(item.screen)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.labelEn,
                                tint = animatedContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AutoResizeText(
                                text = if (appLanguage == AppLanguage.ENGLISH) item.labelEn else item.labelHi,
                                fontSize = 10.sp,
                                minFontSize = 7.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedContentColor,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.GlassFloatingNavBar
import com.example.ui.components.bouncyClickable
import com.example.ui.screens.AiTestGeneratorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.BrownPrimary
import com.example.ui.theme.BrownTextBody
import com.example.ui.theme.BrownTextMuted
import com.example.ui.theme.BrownTextTitle
import com.example.ui.theme.JeeExamTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.WarmBackground
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JeeExamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmBackground
                ) {
                    JeeApp()
                }
            }
        }
    }
}

@Composable
fun JeeApp(viewModel: JeeViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()
    var showExitExamDialog by remember { mutableStateOf(false) }

    // Intercept back button during exam
    BackHandler(enabled = currentScreen == Screen.ACTIVE_EXAM) {
        showExitExamDialog = true
    }

    // Back to Home from secondary tab screens
    BackHandler(enabled = currentScreen != Screen.ACTIVE_EXAM && currentScreen != Screen.HOME) {
        viewModel.navigateTo(Screen.HOME)
    }

    val showFloatingNav = currentScreen in listOf(
        Screen.HOME,
        Screen.DASHBOARD,
        Screen.AI_GENERATOR,
        Screen.TEST_HISTORY
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                Screen.HOME -> HomeScreen(viewModel = viewModel)
                Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                Screen.AI_GENERATOR -> AiTestGeneratorScreen(viewModel = viewModel)
                Screen.ACTIVE_EXAM -> ExamScreen(viewModel = viewModel)
                Screen.EXAM_RESULT -> ResultScreen(viewModel = viewModel)
                Screen.TEST_HISTORY -> HistoryScreen(viewModel = viewModel)
            }
        }

        // Floating Transparent Front Glass Style Navigation Tab
        AnimatedVisibility(
            visible = showFloatingNav,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassFloatingNavBar(
                currentScreen = currentScreen,
                appLanguage = lang,
                onNavigate = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        }
    }

    if (showExitExamDialog) {
        AlertDialog(
            onDismissRequest = { showExitExamDialog = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = if (lang == AppLanguage.ENGLISH) "Exit Active Test?" else "सक्रिय परीक्षा छोड़ें?",
                    fontWeight = FontWeight.Bold,
                    color = BrownTextTitle
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.ENGLISH)
                        "If you leave now, you can either submit your current responses or return to complete the test."
                    else
                        "यदि आप अभी बाहर निकलते हैं, तो आप अपने वर्तमान उत्तर सबमिट कर सकते हैं या परीक्षा पूरी करने के लिए वापस लौट सकते हैं।",
                    color = BrownTextBody
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitExamDialog = false
                        viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.bouncyClickable {
                        showExitExamDialog = false
                        viewModel.submitExam()
                    }
                ) {
                    Text(
                        text = Strings.submitTest(lang),
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitExamDialog = false },
                    modifier = Modifier.bouncyClickable { showExitExamDialog = false }
                ) {
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "Resume Exam" else "परीक्षा जारी रखें",
                        color = BrownPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

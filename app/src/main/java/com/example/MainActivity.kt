package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.IncomingCallDialog
import com.example.ui.components.JarvisListeningFab
import com.example.ui.components.LiquidGlassCard
import com.example.ui.screens.DeviceControlScreen
import com.example.ui.screens.JarvisCoreScreen
import com.example.ui.screens.LogsAndVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SuitesHubScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

class MainActivity : ComponentActivity() {
    private val jarvisViewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = jarvisViewModel)
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val incomingCall by viewModel.incomingCall.collectAsStateWithLifecycle()
    val confirmationData by viewModel.confirmationData.collectAsStateWithLifecycle()
    val isLiveCallActive by viewModel.isLiveCallActive.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val currentMood by viewModel.currentMood.collectAsStateWithLifecycle()
    val lastUserTranscript by viewModel.lastUserVoiceTranscript.collectAsStateWithLifecycle()
    val lastJarvisResponse by viewModel.lastJarvisVoiceResponse.collectAsStateWithLifecycle()
    val audioLevel by viewModel.audioLevel.collectAsStateWithLifecycle()
    val isTouchGuardActive by viewModel.isTouchGuardActive.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = SpaceObsidian,
        floatingActionButton = {
            if (!isLiveCallActive && !isTouchGuardActive) {
                JarvisListeningFab(
                    isListening = isListening,
                    audioLevel = audioLevel,
                    onToggleListening = { viewModel.toggleLiveMic() }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            JarvisBottomNavigationBar(
                currentTab = currentTab,
                onSelectTab = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "navigation_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> JarvisCoreScreen(viewModel = viewModel)
                    1 -> DeviceControlScreen(viewModel = viewModel)
                    2 -> SuitesHubScreen(viewModel = viewModel)
                    3 -> LogsAndVaultScreen(viewModel = viewModel)
                    4 -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Floating Incoming Call Screening Dialog
            incomingCall?.let { call ->
                IncomingCallDialog(
                    callerName = call.callerName,
                    phoneNumber = call.phoneNumber,
                    announcementText = call.announcement,
                    onAnswer = { viewModel.answerIncomingCall(context) },
                    onReject = { viewModel.rejectIncomingCall() },
                    onQuickReply = { replyMsg -> viewModel.replyIncomingCall(replyMsg, context) },
                    onDismiss = { viewModel.rejectIncomingCall() }
                )
            }

            // Safety Verification Confirmation Dialog
            confirmationData?.let { conf ->
                ConfirmationDialog(
                    promptMessage = conf.prompt,
                    onConfirm = { viewModel.resolveConfirmation(true, context) },
                    onCancel = { viewModel.resolveConfirmation(false, context) }
                )
            }

            // Full Screen Live Voice Call Overlay with Jigri Yaar
            AnimatedVisibility(
                visible = isLiveCallActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                com.example.ui.components.LiveVoiceCallOverlay(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    mood = currentMood,
                    lastUserTranscript = lastUserTranscript,
                    lastJarvisResponse = lastJarvisResponse,
                    audioLevel = audioLevel,
                    onToggleMic = { viewModel.toggleLiveMic() },
                    onEndCall = { viewModel.endLiveCall() },
                    onQuickVoicePrompt = { viewModel.sendCommand(it, context) }
                )
            }

            // Full-Screen OLED Touch Guard Overlay
            AnimatedVisibility(
                visible = isTouchGuardActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                com.example.ui.components.TouchGuardOverlay(
                    isActive = isTouchGuardActive,
                    isListening = isListening,
                    onUnlock = { viewModel.disableTouchGuard() },
                    onStopApp = { viewModel.stopAndKillApp(context) }
                )
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun JarvisBottomNavigationBar(
    currentTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val tabs = listOf(
        NavTabItem("Core", Icons.Default.AutoAwesome, "nav_core"),
        NavTabItem("Control", Icons.Default.Smartphone, "nav_control"),
        NavTabItem("100+ Suites", Icons.Default.Widgets, "nav_suites"),
        NavTabItem("Vault", Icons.Default.History, "nav_vault"),
        NavTabItem("Protocol", Icons.Default.Settings, "nav_settings")
    )

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(64.dp)
            .testTag("jarvis_bottom_nav"),
        cornerRadius = 24.dp,
        borderColor = Color(0x3500F0FF),
        backgroundColor = Color(0xD00A1325)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = currentTab == index
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(index) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag(tab.tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 32.dp else 26.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) CyberCyan else TextTertiary,
                            modifier = Modifier.size(if (isSelected) 20.dp else 18.dp)
                        )
                    }
                    Text(
                        text = tab.title,
                        color = if (isSelected) TextPrimary else TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// Retained for GreetingScreenshotTest compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}

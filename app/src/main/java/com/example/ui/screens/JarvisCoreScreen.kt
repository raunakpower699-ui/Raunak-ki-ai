package com.example.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import com.example.ui.components.VoicePermissionRationaleDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrbMood
import com.example.ui.JarvisViewModel
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.AutonomousActionBanner
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MorphingJarvisOrb
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JarvisCoreScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mood by viewModel.currentMood.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val pipelineSteps by viewModel.pipelineSteps.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val isPipelineExecuting by viewModel.isPipelineExecuting.collectAsStateWithLifecycle()
    val proactiveAlerts by viewModel.proactiveAlerts.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val audioLevel by viewModel.audioLevel.collectAsStateWithLifecycle()

    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showRationaleDialog by remember { mutableStateOf(false) }

    if (showRationaleDialog) {
        VoicePermissionRationaleDialog(
            permissionState = micPermissionState,
            onDismiss = { showRationaleDialog = false }
        )
    }

    fun handleMicClick() {
        if (micPermissionState.status.isGranted) {
            if (isListening) viewModel.stopListening() else viewModel.startListening()
        } else {
            if (micPermissionState.status.shouldShowRationale) {
                showRationaleDialog = true
            } else {
                micPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickCommands = listOf(
        "Bhai mood off hai baat kar na",
        "Aaj ka kya scene hai?",
        "Bhai koi mast jugaad bata",
        "Bhai interview phod diya!",
        "Bohot tension chal rahi hai",
        "Touch guard on karo",
        "Home screen jao",
        "YouTube pe trending gaana chalao",
        "Instagram reels kholo",
        "Background me chalao",
        "Sharma ji ki profile dikhao",
        "Mom ko WhatsApp draft karo",
        "Torch on karo",
        "Tu kaisa hai jigri yaar?",
        "Battery saver on karo",
        "Nearby petrol pump dhoondo",
        "Ex ko message bhejo 😄"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpaceObsidian,
                        Color(0xFF090E1D),
                        Color(0xFF05070E)
                    )
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        // 1. Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "JARVIS",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JIGRI YAAR EDITION",
                        color = ElectricViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "ON-DEVICE AUTONOMOUS AGENT",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Call, Touch Guard, Home & Battery HUD Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Touch Guard Button
                LiquidGlassCard(
                    modifier = Modifier.testTag("hud_touch_guard_button"),
                    cornerRadius = 10.dp,
                    backgroundColor = Color(0x3500F0FF),
                    onClick = { viewModel.enableTouchGuard() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Touch Guard",
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Guard",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Home Screen Button
                LiquidGlassCard(
                    modifier = Modifier.testTag("hud_home_screen_button"),
                    cornerRadius = 10.dp,
                    backgroundColor = Color(0x2500E676),
                    onClick = { viewModel.goToHomeScreen(context) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home Screen",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Home",
                            color = EmeraldGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LiquidGlassCard(
                    modifier = Modifier.testTag("simulate_call_button"),
                    cornerRadius = 10.dp,
                    backgroundColor = Color(0x30E0AAFF),
                    onClick = { viewModel.triggerIncomingCallSimulation() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Simulate Call",
                            tint = ElectricViolet,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Call",
                            color = ElectricViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${systemStatus.batteryPct}%",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Proactive Alert Card (Carousel)
        if (proactiveAlerts.isNotEmpty()) {
            val alert = proactiveAlerts.first()
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("proactive_alert_banner"),
                cornerRadius = 14.dp,
                borderColor = ElectricViolet,
                backgroundColor = Color(0x301E1B4B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElectricViolet.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = alert.title,
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alert.messageHinglish,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassCard(
                            cornerRadius = 8.dp,
                            borderColor = EmeraldGlow,
                            backgroundColor = EmeraldGlow.copy(alpha = 0.2f),
                            onClick = { viewModel.sendCommand(alert.commandToExecute, context) }
                        ) {
                            Text(
                                text = alert.actionLabel,
                                color = EmeraldGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissAlert(alert.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Central Morphing Holographic Orb & State Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MorphingJarvisOrb(
                    mood = mood,
                    sizeDp = 175.dp,
                    speechIntensity = audioLevel,
                    isVoiceActive = isListening || isSpeaking,
                    onClick = { handleMicClick() }
                )

                // Voice Wave & Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioWaveVisualizer(
                        isActive = isSpeaking || isListening,
                        barsCount = 12,
                        maxHeight = 20.dp
                    )
                    Text(
                        text = when (mood) {
                            OrbMood.LISTENING -> "SUN RAHA HOON... (BOL MERI JAAN! 🎙️)"
                            OrbMood.THINKING -> "SOCH RAHA HOON BE..."
                            OrbMood.SPEAKING -> "JIGRI YAAR BOL RAHA HAI 🔊"
                            OrbMood.EXECUTING -> "TASK EXECUTE KAR RAHA HOON ⚡"
                            OrbMood.ALERT -> "SAFETY VERIFICATION SHIELD"
                            OrbMood.IDLE -> "TOUCH ORB YA BOL 'HEY JARVIS'"
                        },
                        color = when (mood) {
                            OrbMood.LISTENING -> EmeraldGlow
                            OrbMood.SPEAKING -> NeonPink
                            OrbMood.EXECUTING -> CyberCyan
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    AudioWaveVisualizer(
                        isActive = isSpeaking || isListening,
                        barsCount = 12,
                        maxHeight = 20.dp
                    )
                }

                // Interactive Mic Tap Pill right below Orb
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isListening) EmeraldGlow.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, if (isListening) EmeraldGlow else CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { handleMicClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("orb_quick_mic_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic Trigger",
                        tint = if (isListening) EmeraldGlow else CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isListening) "Mic Active (Sun Raha Hoon - Tap to Stop)" else "🎙️ TAP TO TALK (BOL BE!)",
                        color = if (isListening) EmeraldGlow else CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Autonomous Action Pipeline Banner
        AutonomousActionBanner(
            steps = pipelineSteps,
            currentStepIndex = currentStepIndex,
            isExecuting = isPipelineExecuting,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // 5. Quick Hinglish Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickCommands.forEach { cmd ->
                LiquidGlassCard(
                    cornerRadius = 20.dp,
                    borderColor = Color(0x3500F0FF),
                    backgroundColor = Color(0x250F172A),
                    onClick = { viewModel.sendCommand(cmd, context) }
                ) {
                    Text(
                        text = cmd,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 6. Live Chat Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("conversation_stream"),
            contentPadding = PaddingValues(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(if (isUser) 0.8f else 0.92f)
                            .testTag(if (isUser) "user_message_bubble" else "jarvis_message_bubble"),
                        cornerRadius = 16.dp,
                        borderColor = if (isUser) CyberCyan.copy(alpha = 0.5f) else ElectricViolet.copy(alpha = 0.6f),
                        backgroundColor = if (isUser) Color(0x40003847) else Color(0x451A102E)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUser) "YOU (BHAI)" else "JIGRI YAAR (JARVIS) ❤️",
                                    color = if (isUser) CyberCyan else ElectricViolet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                msg.actionType?.let {
                                    Text(
                                        text = it,
                                        color = EmeraldGlow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // 7. HUGE PROMINENT "LIVE VOICE CALL MODE" BUTTON
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("start_live_call_button"),
            cornerRadius = 18.dp,
            borderColor = EmeraldGlow,
            backgroundColor = Color(0x28052E1E),
            onClick = {
                if (!micPermissionState.status.isGranted) {
                    if (micPermissionState.status.shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        micPermissionState.launchPermissionRequest()
                    }
                }
                viewModel.startLiveCall(context)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Live Voice Mode",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "🔴 LIVE VOICE CALL (JIGRI YAAR SE LIVE BAAT KARO)",
                            color = EmeraldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Chatting chhod, seedha bol kar saare apps chalwa!",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGlow)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONNECT",
                        color = SpaceObsidian,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 8. Bottom Voice & Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        "Bhai ko bol ya type kar... (Hinglish)",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color(0x4000F0FF),
                    focusedContainerColor = Color(0x350A192F),
                    unfocusedContainerColor = Color(0x250A192F),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCommand.isNotBlank()) {
                        viewModel.sendCommand(inputCommand, context)
                        inputCommand = ""
                    }
                })
            )

            // High Visibility Dedicated Mic Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Brush.radialGradient(listOf(EmeraldGlow, CyberCyan))
                        else Brush.radialGradient(listOf(CyberCyan, ElectricViolet))
                    )
                    .border(
                        2.dp,
                        if (isListening) EmeraldGlow else CyberCyan,
                        CircleShape
                    )
                    .clickable { handleMicClick() }
                    .testTag("voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Recognition",
                    tint = SpaceObsidian,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Send Button
            if (inputCommand.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .clickable {
                            viewModel.sendCommand(inputCommand, context)
                            inputCommand = ""
                        }
                        .testTag("command_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command",
                        tint = SpaceObsidian,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrbMood
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveVoiceCallOverlay(
    isListening: Boolean,
    isSpeaking: Boolean,
    mood: OrbMood,
    lastUserTranscript: String,
    lastJarvisResponse: String,
    audioLevel: Float = 0f,
    onToggleMic: () -> Unit,
    onEndCall: () -> Unit,
    onQuickVoicePrompt: (String) -> Unit
) {
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showRationaleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!micPermissionState.status.isGranted) {
            micPermissionState.launchPermissionRequest()
        }
    }

    if (showRationaleDialog) {
        VoicePermissionRationaleDialog(
            permissionState = micPermissionState,
            onDismiss = { showRationaleDialog = false }
        )
    }

    val handleToggleMic = {
        if (micPermissionState.status.isGranted) {
            onToggleMic()
        } else {
            if (micPermissionState.status.shouldShowRationale) {
                showRationaleDialog = true
            } else {
                micPermissionState.launchPermissionRequest()
            }
        }
    }

    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    val quickBuddyPrompts = listOf(
        "Insta reels khol be",
        "Torch chala mere bhai",
        "YouTube pe gaana baja",
        "Kaise ho jigri yaar?",
        "Mummy ko WhatsApp draft karo"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xE614052E),
                        Color(0xF5060814),
                        SpaceObsidian
                    )
                )
            )
            .clickable(enabled = false) {}
            .testTag("live_voice_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Call Header & Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x3500FF9D))
                        .border(1.dp, EmeraldGlow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow)
                    )
                    Text(
                        text = "LIVE AUDIO CALL • JIGRI YAAR ACTIVE",
                        color = EmeraldGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = durationText,
                    color = TextTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Center Morphing Orb & Audio Visualizer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MorphingJarvisOrb(
                    mood = mood,
                    sizeDp = 210.dp,
                    speechIntensity = audioLevel,
                    isVoiceActive = isListening || isSpeaking,
                    onClick = handleToggleMic
                )

                // State indicator pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x30101935))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AudioWaveVisualizer(
                        isActive = isSpeaking || isListening,
                        barsCount = 10,
                        maxHeight = 18.dp
                    )
                    Text(
                        text = when {
                            isListening -> "SUN RAHA HOON... (BOL BHAI! 🎙️)"
                            isSpeaking -> "JIGRI YAAR BOL RAHA HAI... 🔊"
                            mood == OrbMood.THINKING -> "SOCH RAHA HOON MERI JAAN... 🧠"
                            else -> "TAP MIC TO TALK (BOL BE!)"
                        },
                        color = when {
                            isListening -> EmeraldGlow
                            isSpeaking -> NeonPink
                            else -> CyberCyan
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    AudioWaveVisualizer(
                        isActive = isSpeaking || isListening,
                        barsCount = 10,
                        maxHeight = 18.dp
                    )
                }
            }

            // 3. Live Dialogue Cards (What you said & What buddy replied)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (lastUserTranscript.isNotBlank()) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        borderColor = CyberCyan.copy(alpha = 0.5f),
                        backgroundColor = Color(0x35002C3E)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "YOU SAID:",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\"$lastUserTranscript\"",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (lastJarvisResponse.isNotBlank()) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        borderColor = ElectricViolet.copy(alpha = 0.6f),
                        backgroundColor = Color(0x401C0D30)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = NeonPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "JIGRI YAAR:",
                                    color = ElectricViolet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lastJarvisResponse,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Quick suggestions to tap during call
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickBuddyPrompts.take(3).forEach { prompt ->
                        LiquidGlassCard(
                            cornerRadius = 12.dp,
                            borderColor = Color(0x30FFFFFF),
                            backgroundColor = Color(0x201E293B),
                            onClick = { onQuickVoicePrompt(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4. Call Control Actions (Mute/Unmute & End Call)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute / Unmute Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) EmeraldGlow.copy(alpha = 0.25f)
                                else Color(0x351E293B)
                            )
                            .border(
                                2.dp,
                                if (isListening) EmeraldGlow else Color(0x40FFFFFF),
                                CircleShape
                            )
                            .clickable { handleToggleMic() }
                            .testTag("live_call_mic_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (isListening) "Mute Mic" else "Unmute Mic",
                            tint = if (isListening) EmeraldGlow else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = if (isListening) "Mic On (Bol!)" else "Mic Off",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                // End Call Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48))
                            .clickable { onEndCall() }
                            .testTag("live_call_hangup_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Live Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "End Call",
                        color = Color(0xFFF43F5E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

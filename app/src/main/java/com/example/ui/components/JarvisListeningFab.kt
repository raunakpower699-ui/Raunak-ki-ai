package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DeviceAutomationManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Floating Action Button (FAB) that triggers 'Listening' mode with a pulse animation
 * synced in real-time with incoming audio input levels.
 * Powered by Accompanist runtime permissions for RECORD_AUDIO.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JarvisListeningFab(
    isListening: Boolean,
    audioLevel: Float,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Accompanist runtime permission state for RECORD_AUDIO
    val micPermissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )
    var showRationaleDialog by remember { mutableStateOf(false) }

    if (showRationaleDialog) {
        VoicePermissionRationaleDialog(
            permissionState = micPermissionState,
            onDismiss = { showRationaleDialog = false }
        )
    }

    val handleFabClick = {
        if (micPermissionState.status.isGranted) {
            DeviceAutomationManager.vibrateDevice(context, 35)
            onToggleListening()
        } else {
            if (micPermissionState.status.shouldShowRationale) {
                showRationaleDialog = true
            } else {
                micPermissionState.launchPermissionRequest()
            }
        }
    }

    // Ambient infinite animation for idle breathing or wave travel
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse_ambient")

    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breath"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 900 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Smooth real-time audio input level interpolation
    // If listening, combine raw microphone level with subtle ambient movement so visual feedback is always active
    val targetAudioEnergy = if (isListening) {
        maxOf(audioLevel, idleBreath * 0.7f)
    } else {
        0f
    }

    val smoothAudioLevel by animateFloatAsState(
        targetValue = targetAudioEnergy,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 350f
        ),
        label = "smooth_audio_level"
    )

    val primaryColor = if (isListening) EmeraldGlow else CyberCyan
    val secondaryColor = if (isListening) CyberCyan else ElectricViolet

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .semantics {
                contentDescription = if (isListening) "Listening active. Tap to stop." else "Voice listening FAB. Tap to speak."
            }
            .testTag("jarvis_listening_fab_container")
    ) {
        // Floating live speech status badge
        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xE005101E))
                    .border(1.dp, EmeraldGlow.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGlow)
                )
                Text(
                    text = "LISTENING (${(smoothAudioLevel * 100).toInt()}%)",
                    color = EmeraldGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Main FAB with Audio-Reactive Pulse Ripples
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            // Audio-synced Canvas Pulse Shockwaves
            Canvas(modifier = Modifier.size(92.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseFabRadius = 28.dp.toPx()

                if (isListening) {
                    // Outer acoustic shockwave ring 1 (scaled aggressively by real-time audio input)
                    val shockwaveRadius1 = baseFabRadius + (16.dp.toPx() * smoothAudioLevel) + (8.dp.toPx() * (1f + sin(Math.toRadians(wavePhase.toDouble())).toFloat()))
                    val shockwaveAlpha1 = (0.7f - (smoothAudioLevel * 0.3f)).coerceIn(0.15f, 0.75f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = shockwaveAlpha1 * 0.5f),
                                secondaryColor.copy(alpha = shockwaveAlpha1 * 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = shockwaveRadius1
                        ),
                        radius = shockwaveRadius1,
                        center = center
                    )

                    drawCircle(
                        color = primaryColor.copy(alpha = shockwaveAlpha1),
                        radius = shockwaveRadius1,
                        center = center,
                        style = Stroke(
                            width = (1.5f + smoothAudioLevel * 3.5f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Secondary staggered harmonic ring 2
                    val shockwaveRadius2 = baseFabRadius + (8.dp.toPx() * smoothAudioLevel)
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.5f + smoothAudioLevel * 0.4f),
                        radius = shockwaveRadius2,
                        center = center,
                        style = Stroke(
                            width = (1f + smoothAudioLevel * 2f).dp.toPx()
                        )
                    )
                } else {
                    // Subtle breathing glow when idle
                    val idleRadius = baseFabRadius + 5.dp.toPx() * (1f + idleBreath)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.22f),
                        radius = idleRadius,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }

            // Core Interactive FAB Button
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .shadow(
                        elevation = if (isListening) 12.dp else 6.dp,
                        shape = CircleShape,
                        spotColor = primaryColor
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isListening) {
                                listOf(
                                    EmeraldGlow.copy(alpha = 0.95f),
                                    Color(0xFF004D40),
                                    Color(0xFF021B1A)
                                )
                            } else {
                                listOf(
                                    CyberCyan.copy(alpha = 0.9f),
                                    ElectricViolet.copy(alpha = 0.85f),
                                    Color(0xFF0D0221)
                                )
                            },
                            center = Offset(20f, 20f)
                        )
                    )
                    .border(
                        width = if (isListening) 2.dp else 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                primaryColor,
                                secondaryColor,
                                Color.White.copy(alpha = 0.9f),
                                primaryColor
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = primaryColor),
                        onClick = handleFabClick
                    )
                    .testTag("jarvis_listening_fab"),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    // Audio Equalizer Visualizer inside the FAB during speech
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        // 4 dynamic equalizer bars synced to smoothAudioLevel
                        val bar1Height = (10.dp + 16.dp * (smoothAudioLevel * 0.9f))
                        val bar2Height = (14.dp + 22.dp * (smoothAudioLevel * 1.2f).coerceAtMost(1f))
                        val bar3Height = (12.dp + 18.dp * (smoothAudioLevel * 1.0f).coerceAtMost(1f))
                        val bar4Height = (8.dp + 14.dp * (smoothAudioLevel * 0.7f))

                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar1Height)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar2Height)
                                .clip(RoundedCornerShape(2.dp))
                                .background(EmeraldGlow)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar3Height)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar4Height)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyberCyan)
                        )
                    }
                } else {
                    // Idle Mic Icon
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Trigger Listening Mode",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

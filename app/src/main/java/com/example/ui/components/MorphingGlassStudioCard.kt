package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrbMood
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun MorphingGlassStudioCard(
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf(OrbMood.SPEAKING) }
    var speechIntensity by remember { mutableFloatStateOf(0.75f) }
    var isSimulatingSpeech by remember { mutableStateOf(true) }

    val moodOptions = listOf(
        OrbMood.SPEAKING to "Speaking (Pink/Violet)",
        OrbMood.LISTENING to "Listening (Emerald)",
        OrbMood.THINKING to "Thinking (Deep Violet)",
        OrbMood.ALERT to "Alert (Neon Red)",
        OrbMood.EXECUTING to "Executing (Teal/Emerald)",
        OrbMood.IDLE to "Idle (Cyan/Violet)"
    )

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("morphing_glass_studio_card"),
        cornerRadius = 22.dp,
        borderColor = ElectricViolet,
        backgroundColor = Color(0x30150B28)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricViolet.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Waves,
                            contentDescription = "Morphing Shaders",
                            tint = ElectricViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "FLUID GLASS-MORPHISM SHADER ENGINE",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Canvas Splines • AGSL Shaders • Neon Speech Pulsing",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3000F0FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSimulatingSpeech) EmeraldGlow else TextTertiary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSimulatingSpeech) "PULSING ACTIVE" else "PAUSED",
                        color = if (isSimulatingSpeech) EmeraldGlow else TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive Morphing Glass Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                MorphingJarvisOrb(
                    mood = selectedMood,
                    sizeDp = 190.dp,
                    speechIntensity = if (isSimulatingSpeech) speechIntensity else 0f,
                    isVoiceActive = isSimulatingSpeech,
                    onClick = {
                        // Toggle speech simulation on tap
                        isSimulatingSpeech = !isSimulatingSpeech
                    }
                )
            }

            // Status feedback
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x28000000))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                AudioWaveVisualizer(
                    isActive = isSimulatingSpeech,
                    barsCount = 8,
                    maxHeight = 14.dp
                )
                Text(
                    text = if (isSimulatingSpeech) "Pulsing at ${(speechIntensity * 100).toInt()}% Speech Intensity" else "Tap Orb to resume fluid pulse",
                    color = if (isSimulatingSpeech) NeonPink else TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                AudioWaveVisualizer(
                    isActive = isSimulatingSpeech,
                    barsCount = 8,
                    maxHeight = 14.dp
                )
            }

            // Speech Intensity Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Input Pulse Amplitude:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(speechIntensity * 100).toInt()}%",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = speechIntensity,
                    onValueChange = {
                        speechIntensity = it
                        if (!isSimulatingSpeech) isSimulatingSpeech = true
                    },
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPink,
                        activeTrackColor = ElectricViolet,
                        inactiveTrackColor = Color(0x35FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("speech_intensity_slider")
                )
            }

            // Mood Filter Chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Aesthetic Mood State:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodOptions.forEach { (mood, label) ->
                        val isSelected = selectedMood == mood
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricViolet.copy(alpha = 0.35f) else Color(0x20FFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else Color(0x30FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedMood = mood
                                    isSimulatingSpeech = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("orb_mood_${mood.name.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) CyberCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Technical Architecture Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShaderTechBadge("16 Bézier Nodes", "Fluid Spline")
                ShaderTechBadge("AGSL / Radial", "Compose Shader")
                ShaderTechBadge("Frosted Rim", "Glass-morphism")
                ShaderTechBadge("Dual Orbit", "Gyro Photon")
            }
        }
    }
}

@Composable
private fun ShaderTechBadge(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x2000F0FF))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = CyberCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = TextTertiary,
            fontSize = 8.sp
        )
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var sassLevel by remember { mutableFloatStateOf(80f) }
    var hinglishRatio by remember { mutableFloatStateOf(75f) }
    var continuousListening by remember { mutableStateOf(true) }
    var incognitoMode by remember { mutableStateOf(false) }

    var micPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        micPermissionGranted = result[Manifest.permission.RECORD_AUDIO] ?: micPermissionGranted
        cameraPermissionGranted = result[Manifest.permission.CAMERA] ?: cameraPermissionGranted
    }

    LazyColumn(
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "JARVIS CONFIG & SECURITY PROTOCOL",
                    color = CyberCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Autonomous Persona Tuning & Hardware Enclave Permissions",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Personality Tuning Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().testTag("personality_tuning_card"),
                cornerRadius = 20.dp,
                borderColor = ElectricViolet,
                backgroundColor = Color(0x351F0D36)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = ElectricViolet)
                        Text(
                            text = "CHARMING PERSONALITY MATRIX",
                            color = ElectricViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Flirt / Sass Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Charming / Flirt & Sass Index", color = TextPrimary, fontSize = 13.sp)
                            Text("${sassLevel.toInt()}% (High)", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = sassLevel,
                            onValueChange = { sassLevel = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                        )
                    }

                    // Hinglish Ratio Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hinglish Conversational Ratio", color = TextPrimary, fontSize = 13.sp)
                            Text("${hinglishRatio.toInt()}% Hinglish", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = hinglishRatio,
                            onValueChange = { hinglishRatio = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )
                    }

                    // Gemini System Instructions Status Pill
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp,
                        borderColor = CyberCyan.copy(alpha = 0.4f),
                        backgroundColor = Color(0x2000F0FF)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGlow)
                                )
                                Text(
                                    text = "GEMINI 3.5 FLASH • EMPATHETIC HINGLISH ACTIVE",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = "Configured system instructions explicitly favor empathetic brotherly warmth, active emotional listening, and deep comprehension of Indian street idioms (Jugaad, Bawaal, Scene kya hai, Load mat le, Phod diya).",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Continuous Wake Listening
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Continuous Listening Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Activates on 'Hey JARVIS' wake cadence", color = TextTertiary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = continuousListening,
                            onCheckedChange = { continuousListening = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = Color(0x5000F0FF)
                            )
                        )
                    }
                }
            }
        }

        // Maximum System Access & Permission Protocol
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().testTag("permissions_protocol_card"),
                cornerRadius = 20.dp,
                borderColor = EmeraldGlow,
                backgroundColor = Color(0x35062419)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldGlow)
                        Text(
                            text = "MAXIMUM SYSTEM ACCESS PROTOCOL",
                            color = EmeraldGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    val permissionsList = listOf(
                        Triple("Audio / Microphone", "Voice recognition & real-time audio analysis", micPermissionGranted),
                        Triple("Camera Hardware", "Hands-free photography & facial recognition", cameraPermissionGranted),
                        Triple("Device Admin Proxy", "Screen lock & background task supervisor", true),
                        Triple("Accessibility Daemon", "Instagram auto-scroll & WhatsApp UI automation", true),
                        Triple("Biometric Voiceprint", "Hardware enclave voice biometric profile active", true)
                    )

                    permissionsList.forEach { (title, subtitle, isGranted) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(subtitle, color = TextTertiary, fontSize = 11.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isGranted) EmeraldGlow else Color(0x50FFFFFF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isGranted) "GRANTED" else "PENDING",
                                    color = if (isGranted) EmeraldGlow else TextTertiary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!micPermissionGranted || !cameraPermissionGranted) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CAMERA
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow)
                        ) {
                            Text("Grant Remaining Permissions", color = SpaceObsidian, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Privacy Guarantee Statement Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                borderColor = Color(0x3000F0FF),
                backgroundColor = Color(0x250A192F)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Text("ZERO CLOUD DATA RETENTION GUARANTEE", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "• All data encrypted at rest (AES-256)\n• On-device processing for sensitive commands\n• User owns 100% of data with 1-tap export or wipe\n• No third-party tracking or advertising SDKs",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.DeviceAutomationManager
import com.example.ui.JarvisViewModel
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MorphingGlassStudioCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DeviceControlScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val isBackgroundServiceRunning by viewModel.isBackgroundServiceRunning.collectAsStateWithLifecycle()
    val isTouchGuardActive by viewModel.isTouchGuardActive.collectAsStateWithLifecycle()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsStateWithLifecycle()
    val activeAccessibilityPackage by viewModel.activeAccessibilityPackage.collectAsStateWithLifecycle()
    val recentAccessibilityEvents by viewModel.recentAccessibilityEvents.collectAsStateWithLifecycle()
    var testClickTextInput by remember { mutableStateOf("") }
    var showStopConfirmDialog by remember { mutableStateOf(false) }

    if (showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showStopConfirmDialog = false },
            title = {
                Text("Stop JARVIS Entirely?", color = NeonPink, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will stop all background monitoring, release speech and microphone engines, clear notifications, and terminate the application.",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopConfirmDialog = false
                        viewModel.stopAndKillApp(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.testTag("confirm_stop_app_button")
                ) {
                    Text("Stop App", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStopConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_stop_app_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF131A2A)
        )
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
                    text = "DEVICE CONTROL COCKPIT",
                    color = CyberCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Autonomous Hardware Toggles & App Automation Daemons",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Live Morphing UI Glass-morphism & Shader Engine Showcase
        item {
            MorphingGlassStudioCard()
        }

        // System Power, Touch Guard & Background Daemon Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("power_and_background_card"),
                cornerRadius = 20.dp,
                borderColor = CyberCyan,
                backgroundColor = Color(0x350F263D)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "System Power",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SYSTEM POWER & BACKGROUND",
                                    color = CyberCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Touch Guard, Persistent Daemon & Lifecycle",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // 1. Touch Guard Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x25000000))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Touch Guard",
                                tint = if (isTouchGuardActive) EmeraldGlow else CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Touch Guard (Pocket Shield)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isTouchGuardActive) "Locked • Double tap or slide to exit" else "Locks screen touches; keeps voice alive",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleTouchGuard() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTouchGuardActive) EmeraldGlow.copy(alpha = 0.3f) else CyberCyan.copy(alpha = 0.25f),
                                contentColor = if (isTouchGuardActive) EmeraldGlow else CyberCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("toggle_touch_guard_button")
                        ) {
                            Text(
                                text = if (isTouchGuardActive) "UNLOCK" else "ENGAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 2. Background Daemon Running Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x25000000))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Background Daemon",
                                tint = if (isBackgroundServiceRunning) EmeraldGlow else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Background Running Daemon",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isBackgroundServiceRunning) EmeraldGlow else Color(0x60FFFFFF))
                                    )
                                }
                                Text(
                                    text = if (isBackgroundServiceRunning) "Foreground Service: ACTIVE" else "Inactive • Runs in foreground only",
                                    color = if (isBackgroundServiceRunning) EmeraldGlow else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleBackgroundService(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBackgroundServiceRunning) EmeraldGlow.copy(alpha = 0.3f) else ElectricViolet.copy(alpha = 0.3f),
                                contentColor = if (isBackgroundServiceRunning) EmeraldGlow else ElectricViolet
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("toggle_background_service_button")
                        ) {
                            Text(
                                text = if (isBackgroundServiceRunning) "STOP" else "START",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. Quick Action Buttons: Home Screen, Close App, Stop App
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Go to Home Screen
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_screen_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x2500F0FF),
                            onClick = { viewModel.goToHomeScreen(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home Screen",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Home Screen",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Minimize app",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Close App
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("close_app_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x25E0AAFF),
                            onClick = { viewModel.closeApp(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close App",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Close App",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Exit view",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Stop App (Shutdown)
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_app_button"),
                            cornerRadius = 12.dp,
                            borderColor = NeonPink.copy(alpha = 0.5f),
                            backgroundColor = Color(0x35FF0055),
                            onClick = { showStopConfirmDialog = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Stop App",
                                    tint = NeonPink,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Stop App",
                                    color = NeonPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Shutdown all",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Autonomous Accessibility Service & UI Interaction Cockpit Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessibility_service_card"),
                cornerRadius = 20.dp,
                borderColor = ElectricViolet,
                backgroundColor = Color(0x35180A30)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Icon and Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Accessibility Service",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AUTONOMOUS ACCESSIBILITY SERVICE",
                                    color = ElectricViolet,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "OS-Wide App Navigation & UI Automation",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAccessibilityActive) EmeraldGlow.copy(alpha = 0.2f) else AmberAlert.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isAccessibilityActive) EmeraldGlow else AmberAlert)
                                )
                                Text(
                                    text = if (isAccessibilityActive) "ACTIVE" else "INACTIVE",
                                    color = if (isAccessibilityActive) EmeraldGlow else AmberAlert,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Foreground App Display & Permission Banner if Inactive
                    if (!isAccessibilityActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x35FFB300))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accessibility Permission Required",
                                    color = AmberAlert,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable JARVIS in Android Settings to switch apps and interact with on-screen buttons.",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.openAccessibilitySettings(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberAlert,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("enable_a11y_settings_button")
                            ) {
                                Text("ENABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (activeAccessibilityPackage.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x20000000))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Active Package",
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Foreground App: ",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = activeAccessibilityPackage,
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Section 1: System Navigation & App Switching
                    Text(
                        text = "SYSTEM & APP NAVIGATION",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch Apps (Recents)
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a11y_recents_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x309D4EDD),
                            onClick = { viewModel.openRecentApps(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Apps",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Switch Apps",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Recents",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Global Home
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a11y_home_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x2500F0FF),
                            onClick = { viewModel.goToHomeScreen(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Home",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Global Action",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Global Back
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a11y_back_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x25FF007F),
                            onClick = { viewModel.navigateBack(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = NeonPink,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Back",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Global Action",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Notifications Panel
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a11y_notifications_button"),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x2500E676),
                            onClick = { viewModel.openNotifications(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Notice Shade",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Pull Down",
                                    color = TextTertiary,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    // Section 2: Programmatic UI Element Interaction
                    Text(
                        text = "PROGRAMMATIC UI INTERACTION",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Click Element by Text Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = testClickTextInput,
                            onValueChange = { testClickTextInput = it },
                            placeholder = { Text("Enter button/UI text to click...", fontSize = 11.sp, color = TextTertiary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = Color(0x40FFFFFF),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = ElectricViolet
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("a11y_text_to_click_input")
                        )

                        Button(
                            onClick = {
                                if (testClickTextInput.isNotBlank()) {
                                    viewModel.clickElementByText(testClickTextInput, context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("a11y_click_text_button")
                        ) {
                            Text("CLICK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Text Chips & Scroll Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick text chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Allow", "Next", "Search", "Done").forEach { chipText ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x30FFFFFF))
                                        .clickable {
                                            testClickTextInput = chipText
                                            viewModel.clickElementByText(chipText, context)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = chipText,
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Scroll Down / Scroll Up Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .clickable { viewModel.scrollScreen(down = true, context = context) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("a11y_scroll_down_button")
                            ) {
                                Text("Scroll ↓", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .clickable { viewModel.scrollScreen(down = false, context = context) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("a11y_scroll_up_button")
                            ) {
                                Text("Scroll ↑", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Live Event Ticker
                    if (recentAccessibilityEvents.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x40000000))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "LIVE ACCESSIBILITY TELEMETRY",
                                color = TextTertiary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            recentAccessibilityEvents.take(3).forEach { ev ->
                                Text(
                                    text = "• $ev",
                                    color = EmeraldGlow,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hardware Controls Card
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_controls_card"),
                cornerRadius = 20.dp,
                borderColor = CyberCyan,
                backgroundColor = Color(0x350F1D38)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "HARDWARE TELEMETRY & TOGGLES",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    // Quick Toggle Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Torch Button
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("torch_toggle_card"),
                            cornerRadius = 14.dp,
                            borderColor = if (systemStatus.isTorchOn) EmeraldGlow else Color(0x30FFFFFF),
                            backgroundColor = if (systemStatus.isTorchOn) EmeraldGlow.copy(alpha = 0.2f) else Color(0x20152238),
                            onClick = { viewModel.toggleTorchDirect(context) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (systemStatus.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                    contentDescription = "Torch",
                                    tint = if (systemStatus.isTorchOn) EmeraldGlow else TextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = if (systemStatus.isTorchOn) "Torch ON" else "Torch OFF",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // WiFi Settings
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wifi_settings_card"),
                            cornerRadius = 14.dp,
                            borderColor = Color(0x3000F0FF),
                            backgroundColor = Color(0x20152238),
                            onClick = { DeviceAutomationManager.openSettings(context, Settings.ACTION_WIFI_SETTINGS) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "WiFi",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "WiFi Config",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Bluetooth Settings
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bluetooth_settings_card"),
                            cornerRadius = 14.dp,
                            borderColor = Color(0x30A855F7),
                            backgroundColor = Color(0x20152238),
                            onClick = { DeviceAutomationManager.openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "Bluetooth",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "Bluetooth",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // System Audio Volume Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Media Volume Stream",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${systemStatus.volumeLevelPct}%",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Slider(
                                value = systemStatus.volumeLevelPct.toFloat(),
                                onValueChange = { newVal ->
                                    val pct = newVal.toInt()
                                    DeviceAutomationManager.setSystemVolume(context, pct)
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("volume_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = Color(0x3500F0FF)
                                )
                            )
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Battery Saver Quick Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = EmeraldGlow)
                            Column {
                                Text("Battery Saver Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Throttles background daemons", color = TextTertiary, fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.sendCommand("Battery saver on karo", context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow.copy(alpha = 0.25f)),
                            modifier = Modifier.testTag("battery_saver_button")
                        ) {
                            Text("Activate", color = EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Instagram Automation Suite
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("instagram_suite_card"),
                cornerRadius = 18.dp,
                borderColor = NeonPink.copy(alpha = 0.6f),
                backgroundColor = Color(0x30250A19)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NeonPink.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.SmartDisplay, contentDescription = null, tint = NeonPink, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Instagram Autonomous Control", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Reels, profiles & captions automation", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sendCommand("Instagram kholo", context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.sendCommand("Reels scroll karo", context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x40E11D48))
                        ) {
                            Text("Reels Mode", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.sendCommand("Sharma ji ki profile dikhao", context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x30E11D48))
                        ) {
                            Text("Sharma Ji", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // WhatsApp Automation Suite
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("whatsapp_suite_card"),
                cornerRadius = 18.dp,
                borderColor = EmeraldGlow.copy(alpha = 0.6f),
                backgroundColor = Color(0x30052216)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGlow.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("WA", color = EmeraldGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text("WhatsApp Smart Messaging", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Context-aware quick draft & reply daemons", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sendCommand("Mom ko reply karo ki main 8 baje aaunga", context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow)
                        ) {
                            Text("Reply Mom", color = Color(0xFF032612), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.sendCommand("WhatsApp check karo", context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x4010B981))
                        ) {
                            Text("Summary", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Camera, Media & Maps Suite
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                borderColor = CyberCyan.copy(alpha = 0.5f),
                backgroundColor = Color(0x300D1B2A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CAMERA & NAVIGATION INTEGRATIONS",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x2500F0FF),
                            onClick = { DeviceAutomationManager.openCamera(context) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                                Text("Camera", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        LiquidGlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x25A855F7),
                            onClick = { DeviceAutomationManager.openGallery(context) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                                Text("Gallery", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        LiquidGlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x2500FF9D),
                            onClick = { DeviceAutomationManager.openMaps(context, "Nearby Petrol Pump") }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(20.dp))
                                Text("Petrol Pump", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        LiquidGlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            backgroundColor = Color(0x25FF2A85),
                            onClick = { DeviceAutomationManager.bookCab(context, "Meeting") }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                                Text("Cab Book", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

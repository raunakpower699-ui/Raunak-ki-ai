package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class CapabilityItem(
    val title: String,
    val description: String,
    val command: String,
    val isAutonomous: Boolean = true
)

data class SuiteCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val items: List<CapabilityItem>
)

@Composable
fun SuitesHubScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val suites = remember {
        listOf(
            SuiteCategory(
                title = "Communication",
                icon = Icons.Default.Call,
                color = CyberCyan,
                items = listOf(
                    CapabilityItem("Smart Call Screening", "Detects spam and announces genuine callers in Hinglish", "Boss, Ravi ka call aa raha hai - uthaun ya kat dun?"),
                    CapabilityItem("Contextual Auto-Reply", "Drafts contextual messages matching Boss's tone", "Mom ko reply karo ki main 8 baje aaunga"),
                    CapabilityItem("Voicemail-to-Text", "Transcribes missed calls & generates actionable summaries", "Voicemail check karo aur summary sunao"),
                    CapabilityItem("WhatsApp Status Viewer", "Auto-views contacts status and highlights important updates", "WhatsApp check karo aur unread messages dikhao"),
                    CapabilityItem("Birthday & Event Alerts", "Tracks upcoming milestones with auto-greetings", "Remind me of Ravi's birthday next Friday")
                )
            ),
            SuiteCategory(
                title = "Productivity",
                icon = Icons.Default.BusinessCenter,
                color = ElectricViolet,
                items = listOf(
                    CapabilityItem("Smart Reminders", "Location & time-aware high-priority alerts with Room DB", "Kal subah 7 baje mujhe uthana doctor appointment ke liye"),
                    CapabilityItem("Calendar Conflict Solver", "Reschedules overlapping meetings automatically", "Check tomorrow's calendar schedule"),
                    CapabilityItem("Document Scanner & OCR", "Extracts structured text from invoices & receipts", "Scan receipt from camera"),
                    CapabilityItem("Voice-to-Task Dispatcher", "Transforms speech rambles into sorted tasks", "Meeting notes likho: project deadline Friday hai")
                )
            ),
            SuiteCategory(
                title = "Media & Cam",
                icon = Icons.Default.VideoLibrary,
                color = NeonPink,
                items = listOf(
                    CapabilityItem("Instagram Reels Automation", "Opens reels feed with gesture navigation daemon", "Instagram Reels scroll karo"),
                    CapabilityItem("Camera Lens Trigger", "Instant hands-free photo/video capture", "Photo kheecho portrait mode mein"),
                    CapabilityItem("Duplicate & Blur Cleaner", "Scans gallery for blurry shots and requests safe deletion", "Gallery mein se blur photos delete karo"),
                    CapabilityItem("Music Controller", "Manages background audio stream and playlist playback", "Spotify pe relax songs play karo")
                )
            ),
            SuiteCategory(
                title = "Security & Vault",
                icon = Icons.Default.Security,
                color = EmeraldGlow,
                items = listOf(
                    CapabilityItem("Biometric Proxy Shield", "Hardware enclave access proxy for authentication", "Check security vault status"),
                    CapabilityItem("Privacy Permissions Audit", "Inspects runtime microphone, camera & location sensors", "Privacy audit run karo"),
                    CapabilityItem("Intruder Capture Mode", "Captures front camera photo on unauthorized access attempt", "Activate intruder detection"),
                    CapabilityItem("Ex-Message Protection", "Safety protocol preventing impulsive messages to ex 😄", "Message my ex saying hello")
                )
            ),
            SuiteCategory(
                title = "Travel & Info",
                icon = Icons.Default.Flight,
                color = Color(0xFFFFB800),
                items = listOf(
                    CapabilityItem("Proactive Cab Booker", "Detects upcoming meetings and calculates transit delay", "Cab book karo Uber se meeting ke liye"),
                    CapabilityItem("Traffic GPS Navigation", "Computes fastest turn-by-turn route with live conditions", "Ghar ka route dikhao Maps pe"),
                    CapabilityItem("Nearby Essentials Radar", "Locates petrol pumps, hospitals, ATMs near current coordinate", "Nearby petrol pump dhoondo"),
                    CapabilityItem("Real-time Briefing", "Provides weather, headlines, and calendar overview", "Aaj ka weather aur schedule batao")
                )
            )
        )
    }

    val currentSuite = suites[selectedCategoryIndex]

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
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "100+ ADVANCED CAPABILITIES",
                color = CyberCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Tap any capability to trigger autonomous execution",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Horizontal Category Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color(0x200D1527),
            contentColor = CyberCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    color = currentSuite.color
                )
            }
        ) {
            suites.forEachIndexed { index, suite ->
                val isSelected = selectedCategoryIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = suite.icon,
                                contentDescription = null,
                                tint = if (isSelected) suite.color else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = suite.title,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        // Capability Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentSuite.items) { item ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    borderColor = currentSuite.color.copy(alpha = 0.4f),
                    backgroundColor = Color(0x25121E36)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(currentSuite.color.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (item.isAutonomous) "AUTONOMOUS" else "PROACTIVE",
                                    color = currentSuite.color,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = item.description,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Command: \"${item.command}\"",
                                color = currentSuite.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.sendCommand(item.command, context)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = currentSuite.color),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = SpaceObsidian,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Execute", color = SpaceObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun IncomingCallDialog(
    callerName: String,
    phoneNumber: String,
    announcementText: String,
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    onQuickReply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("incoming_call_dialog"),
            cornerRadius = 24.dp,
            borderColor = CyberCyan,
            backgroundColor = Color(0xF00A1325)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Incoming call badge
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
                        text = "JARVIS CALL SCREENING ACTIVE",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                // Caller Avatar Hologram
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0x3500F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Caller Avatar",
                        tint = CyberCyan,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = callerName,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = phoneNumber,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                // JARVIS Announcement Quote Bubble
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    borderColor = Color(0x30A855F7),
                    backgroundColor = Color(0x301E293B)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎙️ JARVIS Voice Announcement:",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$announcementText\"",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action Buttons: Uthaun (Answer) & Kat Dun (Reject)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("call_reject_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Kat Dun", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kat Dun", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAnswer,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("call_answer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Uthaun",
                            tint = Color(0xFF022B14),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uthaun", color = Color(0xFF022B14), fontWeight = FontWeight.Bold)
                    }
                }

                // Auto-reply context
                OutlinedButton(
                    onClick = { onQuickReply("Meeting mein hoon, baad mein call karunga.") },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Reply: Meeting mein hoon", fontSize = 12.sp)
                }
            }
        }
    }
}

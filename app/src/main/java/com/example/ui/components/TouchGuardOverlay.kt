package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DeviceAutomationManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpaceObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Full-screen OLED Touch Guard overlay that shields against accidental touches
 * in pockets or bags while keeping background speech operations running.
 */
@Composable
fun TouchGuardOverlay(
    isActive: Boolean,
    isListening: Boolean,
    onUnlock: () -> Unit,
    onStopApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var tapHintMessage by remember { mutableStateOf("") }

    // Live clock update
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "touch_guard_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Handle double-tap unlock logic
    val handleLockTap = {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 500) {
            // Successful double tap
            DeviceAutomationManager.vibrateDevice(context, 60)
            onUnlock()
        } else {
            tapCount = 1
            lastTapTime = now
            tapHintMessage = "Tap once more to unlock"
            DeviceAutomationManager.vibrateDevice(context, 20)
            coroutineScope.launch {
                delay(1200)
                if (System.currentTimeMillis() - lastTapTime >= 1000) {
                    tapHintMessage = ""
                }
            }
        }
    }

    // Intercepts ALL touches so nothing reaches underneath
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030509))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Consume stray clicks
                handleLockTap()
            }
            .testTag("touch_guard_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic cyber grid backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            val gridAlpha = 0.04f
            val gridColor = CyberCyan.copy(alpha = gridAlpha)
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Clock and Guard Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x3300F0FF))
                        .border(1.dp, CyberCyan.copy(alpha = pulseAlpha), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Active",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "TOUCH GUARD ACTIVE • POCKET MODE",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentTimeString,
                    color = TextPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Text(
                    text = currentDateString,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Center Interactive Holographic Lock Core
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    CyberCyan.copy(alpha = 0.2f),
                                    Color(0x0500F0FF),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(2.dp, CyberCyan.copy(alpha = pulseAlpha), CircleShape)
                        .clickable { handleLockTap() }
                        .testTag("touch_guard_lock_core"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Double tap to unlock",
                        tint = CyberCyan,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Text(
                    text = if (tapHintMessage.isNotEmpty()) tapHintMessage else "Double Tap Lock or Slide below to Unlock",
                    color = if (tapHintMessage.isNotEmpty()) EmeraldGlow else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                if (isListening) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2500E676))
                            .border(1.dp, EmeraldGlow, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic is live",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "VOICE ENGINE LISTENING IN POCKET",
                            color = EmeraldGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Section: Slide to Unlock Track & Emergency Stop
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SlideToUnlockBar(
                    onUnlock = {
                        DeviceAutomationManager.vibrateDevice(context, 70)
                        onUnlock()
                    }
                )

                // Quick Emergency Stop/Exit
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStopApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x35FF0055),
                            contentColor = NeonPink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("touch_guard_stop_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Stop App",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STOP JARVIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cybernetic Slide to Unlock Component
 */
@Composable
private fun SlideToUnlockBar(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0x250A192F))
            .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxDragPx = with(density) { (maxWidth - 56.dp).toPx() }

        // Slide hint text in background
        Text(
            text = "SLIDE TO UNLOCK ❯❯❯",
            color = CyberCyan.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // Draggable Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(CyberCyan, Color(0xFF007799))))
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragPx)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        if (offsetX.value >= maxDragPx * 0.75f) {
                            coroutineScope.launch {
                                offsetX.animateTo(maxDragPx, tween(150))
                                onUnlock()
                                offsetX.snapTo(0f)
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, tween(200))
                            }
                        }
                    }
                )
                .testTag("slide_unlock_knob"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Slide Knob",
                tint = SpaceObsidian,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

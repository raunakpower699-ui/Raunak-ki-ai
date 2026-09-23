package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink

@Composable
fun AudioWaveVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barsCount: Int = 18,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 28.dp
) {
    val transition = rememberInfiniteTransition(label = "audio_bars")

    val anim1 = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar_1"
    )
    val anim2 = transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar_2"
    )
    val anim3 = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar_3"
    )

    val waveGradient = Brush.verticalGradient(
        colors = listOf(CyberCyan, ElectricViolet, NeonPink)
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barsCount) {
            val scale = if (!isActive) 0.15f else {
                when (i % 3) {
                    0 -> anim1.value
                    1 -> anim2.value
                    else -> anim3.value
                }
            }
            val heightDp = (maxHeight * scale).coerceAtLeast(4.dp)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(heightDp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(waveGradient)
            )
        }
    }
}

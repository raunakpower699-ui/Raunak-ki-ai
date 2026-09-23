package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.OrbMood
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonPink
import kotlin.math.cos
import kotlin.math.sin

private const val AGSL_FLUID_GLASS_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    uniform float uPulse;
    uniform float uSpeech;
    uniform float4 uColorPrimary;
    uniform float4 uColorSecondary;
    uniform float4 uColorAccent;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord - 0.5 * uResolution) / min(uResolution.x, uResolution.y);
        float d = length(uv);
        float angle = atan(uv.y, uv.x);

        // Fluid morphing wave displacement
        float wave1 = sin(angle * 3.0 + uTime * 2.5) * 0.05 * (1.0 + uSpeech * 1.2);
        float wave2 = cos(angle * 5.0 - uTime * 1.8) * 0.035 * (1.0 + uSpeech * 0.9);
        float wave3 = sin(angle * 7.0 + uTime * 3.2) * 0.02 * uSpeech;
        float morphRadius = 0.36 * uPulse + wave1 + wave2 + wave3;

        // Glass refraction rim & core mask
        float coreMask = 1.0 - smoothstep(morphRadius - 0.08, morphRadius, d);
        float rimLight = smoothstep(morphRadius - 0.04, morphRadius, d) * (1.0 - smoothstep(morphRadius, morphRadius + 0.04, d));

        // Pulsing Neon Glow falloff
        float glowFactor = exp(-max(0.0, d - morphRadius) * (13.0 - uSpeech * 6.0));

        // Dynamic fluid chromatic color mixing
        float4 baseColor = mix(uColorPrimary, uColorSecondary, sin(angle + uTime) * 0.5 + 0.5);
        baseColor = mix(baseColor, uColorAccent, coreMask * 0.5);

        // Specular glass reflection arc
        float2 specPos = uv - float2(-0.12, -0.12);
        float spec = pow(max(0.0, 1.0 - length(specPos) * 3.0), 3.5) * 0.85;

        float4 finalColor = baseColor * coreMask;
        finalColor += uColorPrimary * (rimLight * 2.2);
        finalColor += uColorSecondary * (glowFactor * (0.8 + uSpeech * 1.2));
        finalColor += float4(spec, spec, spec, spec * coreMask);

        float alpha = clamp(coreMask * 0.9 + rimLight + glowFactor * 0.85, 0.0, 1.0);
        return half4(finalColor.rgb * alpha, alpha);
    }
"""

@Composable
fun MorphingJarvisOrb(
    mood: OrbMood,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp,
    speechIntensity: Float = 0f,
    isVoiceActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isSpeakingOrListening = isVoiceActive || mood == OrbMood.LISTENING || mood == OrbMood.SPEAKING
    val activeSpeechValue = if (speechIntensity > 0f) speechIntensity else if (isSpeakingOrListening) 0.85f else 0.15f

    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_fluid_motion")

    // Rotation angle
    val rotationAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mood == OrbMood.THINKING) 3000 else if (isSpeakingOrListening) 5000 else 8500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing rhythm
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = if (isSpeakingOrListening) 1.18f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mood == OrbMood.LISTENING) 550 else if (mood == OrbMood.SPEAKING) 700 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Speech Neon Ripple Shockwave 1
    val rippleScale1 = infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSpeakingOrListening) 900 else 2200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    // Speech Neon Ripple Shockwave 2 (staggered)
    val rippleScale2 = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSpeakingOrListening) 1300 else 2800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )

    // Morph wave phase
    val wavePhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSpeakingOrListening) 2000 else 4500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val colorScheme = when (mood) {
        OrbMood.LISTENING -> Triple(EmeraldGlow, CyberCyan, Color(0xFF003847))
        OrbMood.SPEAKING -> Triple(NeonPink, ElectricViolet, CyberCyan)
        OrbMood.THINKING -> Triple(ElectricViolet, CyberCyan, Color(0xFF3B0764))
        OrbMood.ALERT -> Triple(Color(0xFFFF3B30), NeonPink, Color(0xFF500724))
        OrbMood.EXECUTING -> Triple(EmeraldGlow, CyberCyan, Color(0xFF004D40))
        OrbMood.IDLE -> Triple(CyberCyan, ElectricViolet, Color(0xFF030712))
    }

    // Android 13+ AGSL RuntimeShader initialization
    val runtimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                android.graphics.RuntimeShader(AGSL_FLUID_GLASS_SHADER)
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .testTag("jarvis_morphing_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.46f

            // 1. Multi-pass Pulsing Neon Glow during speech input
            drawSpeechNeonAuras(
                center = center,
                baseRadius = baseRadius,
                isSpeakingOrListening = isSpeakingOrListening,
                rippleScale1 = rippleScale1.value,
                rippleScale2 = rippleScale2.value,
                colors = colorScheme
            )

            // 2. Gyroscopic Holographic Orbital Particle Rings
            drawOrbitalRings(
                center = center,
                baseRadius = baseRadius,
                rotationAngle = rotationAngle.value,
                colors = colorScheme
            )

            // 3. AGSL Compose Shader Core (on API 33+) OR Advanced Spline Morphing Canvas
            var agslDrawn = false
            if (runtimeShader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    runtimeShader.setFloatUniform("uResolution", size.width, size.height)
                    runtimeShader.setFloatUniform("uTime", wavePhase.value)
                    runtimeShader.setFloatUniform("uPulse", pulseScale.value)
                    runtimeShader.setFloatUniform("uSpeech", activeSpeechValue)
                    runtimeShader.setFloatUniform(
                        "uColorPrimary",
                        colorScheme.first.red,
                        colorScheme.first.green,
                        colorScheme.first.blue,
                        colorScheme.first.alpha
                    )
                    runtimeShader.setFloatUniform(
                        "uColorSecondary",
                        colorScheme.second.red,
                        colorScheme.second.green,
                        colorScheme.second.blue,
                        colorScheme.second.alpha
                    )
                    runtimeShader.setFloatUniform(
                        "uColorAccent",
                        colorScheme.third.red,
                        colorScheme.third.green,
                        colorScheme.third.blue,
                        colorScheme.third.alpha
                    )

                    drawCircle(
                        brush = ShaderBrush(runtimeShader),
                        center = center,
                        radius = baseRadius * 1.35f
                    )
                    agslDrawn = true
                } catch (t: Throwable) {
                    agslDrawn = false
                }
            }

            // 4. Fluid Canvas Spline Morphing Core (acts as primary on API < 33 or glass-enhancement overlay)
            drawFluidMorphingGlassCore(
                center = center,
                baseRadius = baseRadius * pulseScale.value,
                wavePhase = wavePhase.value,
                isSpeakingOrListening = isSpeakingOrListening,
                colors = colorScheme,
                skipBackgroundFill = agslDrawn
            )

            // 5. Specular Glass Highlights & Caustic Arcs
            drawGlassSpecularHighlights(
                center = center,
                coreRadius = baseRadius * pulseScale.value,
                colors = colorScheme
            )

            // 6. Inner Cybernetic Energy Lattice
            drawCyberneticLattice(
                center = center,
                coreRadius = baseRadius * pulseScale.value,
                rotationAngle = rotationAngle.value,
                colors = colorScheme
            )
        }
    }
}

private fun DrawScope.drawSpeechNeonAuras(
    center: Offset,
    baseRadius: Float,
    isSpeakingOrListening: Boolean,
    rippleScale1: Float,
    rippleScale2: Float,
    colors: Triple<Color, Color, Color>
) {
    if (isSpeakingOrListening) {
        // Shockwave 1: Intense Primary Neon Halo
        val alpha1 = (1f - (rippleScale1 - 0.45f) / 1.0f).coerceIn(0f, 0.75f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.first.copy(alpha = alpha1 * 0.45f),
                    colors.second.copy(alpha = alpha1 * 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * rippleScale1
            ),
            center = center,
            radius = baseRadius * rippleScale1
        )

        // Shockwave 2: Staggered Secondary Chromatic Glow
        val alpha2 = (1f - (rippleScale2 - 0.35f) / 1.0f).coerceIn(0f, 0.6f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.second.copy(alpha = alpha2 * 0.35f),
                    colors.third.copy(alpha = alpha2 * 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * rippleScale2
            ),
            center = center,
            radius = baseRadius * rippleScale2
        )
    }

    // Steady ambient glass back-glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.first.copy(alpha = if (isSpeakingOrListening) 0.5f else 0.3f),
                colors.second.copy(alpha = if (isSpeakingOrListening) 0.25f else 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 1.25f
        ),
        center = center,
        radius = baseRadius * 1.25f
    )
}

private fun DrawScope.drawOrbitalRings(
    center: Offset,
    baseRadius: Float,
    rotationAngle: Float,
    colors: Triple<Color, Color, Color>
) {
    val ringRadius1 = baseRadius * 1.32f
    drawCircle(
        color = colors.first.copy(alpha = 0.32f),
        radius = ringRadius1,
        center = center,
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    // Orbital Photon Nodes (Ring 1)
    val ringParticles = 8
    for (i in 0 until ringParticles) {
        val angleRad = Math.toRadians((rotationAngle + (i * 360f / ringParticles)).toDouble())
        val px = center.x + (ringRadius1 * cos(angleRad)).toFloat()
        val py = center.y + (ringRadius1 * sin(angleRad)).toFloat()
        drawCircle(
            color = if (i % 2 == 0) colors.first else colors.second,
            radius = if (i % 2 == 0) 4.5.dp.toPx() else 3.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // Counter-rotating Inner Ring 2
    val ringRadius2 = baseRadius * 1.14f
    drawCircle(
        color = colors.second.copy(alpha = 0.22f),
        radius = ringRadius2,
        center = center,
        style = Stroke(width = 1.2.dp.toPx())
    )

    for (i in 0 until 6) {
        val angleRad = Math.toRadians((-rotationAngle * 1.4f + (i * 60f)).toDouble())
        val px = center.x + (ringRadius2 * cos(angleRad)).toFloat()
        val py = center.y + (ringRadius2 * sin(angleRad)).toFloat()
        drawCircle(
            color = colors.second.copy(alpha = 0.75f),
            radius = 2.5.dp.toPx(),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawFluidMorphingGlassCore(
    center: Offset,
    baseRadius: Float,
    wavePhase: Float,
    isSpeakingOrListening: Boolean,
    colors: Triple<Color, Color, Color>,
    skipBackgroundFill: Boolean
) {
    val pointCount = 16
    val speechMultiplier = if (isSpeakingOrListening) 1.8f else 0.8f
    val radii = FloatArray(pointCount)
    val points = Array(pointCount) { Offset.Zero }

    for (i in 0 until pointCount) {
        val angle = (i * 2.0 * Math.PI / pointCount).toFloat()
        val wave1 = sin(angle * 3f + wavePhase * 1.2f) * 0.06f * speechMultiplier
        val wave2 = cos(angle * 5f - wavePhase * 0.9f) * 0.04f * speechMultiplier
        val wave3 = sin(angle * 7f + wavePhase * 1.5f) * 0.025f * speechMultiplier

        val r = baseRadius * (1f + wave1 + wave2 + wave3)
        radii[i] = r
        points[i] = Offset(
            center.x + r * cos(angle),
            center.y + r * sin(angle)
        )
    }

    // Build smooth cubic spline path
    val morphPath = Path()
    morphPath.moveTo((points[0].x + points[pointCount - 1].x) / 2f, (points[0].y + points[pointCount - 1].y) / 2f)

    for (i in 0 until pointCount) {
        val current = points[i]
        val next = points[(i + 1) % pointCount]
        val midX = (current.x + next.x) / 2f
        val midY = (current.y + next.y) / 2f
        morphPath.quadraticTo(current.x, current.y, midX, midY)
    }
    morphPath.close()

    if (!skipBackgroundFill) {
        // Deep Liquid Glass Gradient fill
        drawPath(
            path = morphPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.92f),
                    colors.first,
                    colors.second,
                    colors.third.copy(alpha = 0.88f)
                ),
                center = Offset(center.x - baseRadius * 0.22f, center.y - baseRadius * 0.22f),
                radius = baseRadius * 1.15f
            ),
            style = Fill
        )
    }

    // Glowing Neon Glass Rim (Glass-morphism edge refraction)
    drawPath(
        path = morphPath,
        brush = Brush.sweepGradient(
            colors = listOf(
                colors.first,
                colors.second,
                Color.White.copy(alpha = 0.85f),
                colors.first,
                colors.third,
                colors.first
            ),
            center = center
        ),
        style = Stroke(
            width = if (isSpeakingOrListening) 3.5.dp.toPx() else 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawGlassSpecularHighlights(
    center: Offset,
    coreRadius: Float,
    colors: Triple<Color, Color, Color>
) {
    // Primary Top-Left Curved Glass Highlight Arc
    val specRadius = coreRadius * 0.72f
    val specCenter = Offset(center.x - coreRadius * 0.14f, center.y - coreRadius * 0.18f)

    drawArc(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.35f),
                Color.Transparent
            ),
            start = Offset(specCenter.x - specRadius, specCenter.y - specRadius),
            end = Offset(specCenter.x + specRadius, specCenter.y + specRadius)
        ),
        startAngle = 180f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(specCenter.x - specRadius, specCenter.y - specRadius),
        size = Size(specRadius * 2f, specRadius * 2f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Secondary subtle bottom-right caustic reflection
    val causticRadius = coreRadius * 0.82f
    drawArc(
        color = colors.first.copy(alpha = 0.4f),
        startAngle = 30f,
        sweepAngle = 75f,
        useCenter = false,
        topLeft = Offset(center.x - causticRadius, center.y - causticRadius),
        size = Size(causticRadius * 2f, causticRadius * 2f),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawCyberneticLattice(
    center: Offset,
    coreRadius: Float,
    rotationAngle: Float,
    colors: Triple<Color, Color, Color>
) {
    // Inner Cybernetic Reticle Ring
    val reticleRadius = coreRadius * 0.52f
    drawCircle(
        color = Color.White.copy(alpha = 0.35f),
        radius = reticleRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Dynamic rotating crosshair ticks
    val tickLength = 7.dp.toPx()
    for (i in 0 until 4) {
        val angleRad = Math.toRadians((rotationAngle * 0.8f + (i * 90f)).toDouble())
        val startX = center.x + ((reticleRadius - tickLength) * cos(angleRad)).toFloat()
        val startY = center.y + ((reticleRadius - tickLength) * sin(angleRad)).toFloat()
        val endX = center.x + ((reticleRadius + tickLength) * cos(angleRad)).toFloat()
        val endY = center.y + ((reticleRadius + tickLength) * sin(angleRad)).toFloat()

        drawLine(
            color = colors.first.copy(alpha = 0.6f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Glowing core nucleus photon
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                colors.first,
                Color.Transparent
            ),
            center = center,
            radius = coreRadius * 0.28f
        ),
        radius = coreRadius * 0.28f,
        center = center
    )
}

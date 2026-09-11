package com.softphone.studio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MonoLuxurySplashScreen(
    onFinish: () -> Unit
) {
    var animationProgress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("INITIALIZING ENCRYPTED TLS SESSION...") }

    val infiniteTransition = rememberInfiniteTransition(label = "MonoLuxuryInfinite")
    
    // Rotating radar reticle (Clockwise)
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    // Orbiting photon angle (Counter-Clockwise)
    val photonAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhotonAngle"
    )

    // Breathing glow aura
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    // Wave broadcast pulse
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePulse"
    )

    // Intro Scale & Fade Transitions
    val introScale by animateFloatAsState(
        targetValue = if (animationProgress > 0.1f) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "IntroScale"
    )
    val introAlpha by animateFloatAsState(
        targetValue = if (animationProgress > 0.1f) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "IntroAlpha"
    )

    LaunchedEffect(Unit) {
        delay(100)
        animationProgress = 0.25f
        delay(600)
        statusText = "VERIFYING WARSAW VOIP NODE [+48]..."
        animationProgress = 0.65f
        delay(700)
        statusText = "AUTHENTICATED // READY"
        animationProgress = 1.0f
        delay(700)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinish
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(introScale)
                .alpha(introAlpha)
                .padding(24.dp)
        ) {
            // Master Canvas Emblem
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawMonoLuxuryEmblem(
                        radarAngle = radarAngle,
                        photonAngle = photonAngle,
                        auraAlpha = auraAlpha,
                        wavePulse = wavePulse
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Typography Lockup
            Text(
                text = "PHANTOMLINE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "BY TREXHAUSTED // DISCORD",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress Indicator
            LinearProgressIndicator(
                progress = { animationProgress },
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp),
                color = TextPrimary,
                trackColor = OledSurfaceElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = TextTertiary
            )
        }

        // Skip prompt at bottom
        Text(
            text = "TAP TO ENTER",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = OledBorderMedium,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

private fun DrawScope.drawMonoLuxuryEmblem(
    radarAngle: Float,
    photonAngle: Float,
    auraAlpha: Float,
    wavePulse: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = size.width / 2f

    // 1. Core Ambient Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.18f * auraAlpha), Color.Transparent),
            center = center,
            radius = maxRadius * 0.95f
        ),
        radius = maxRadius * 0.95f,
        center = center
    )

    // 2. Outer Squircle Shield Chassis (Pure Pitch Black with Metallic Gradient Rim)
    val squircleSize = size.width * 0.88f
    val squircleTopLeft = Offset(
        center.x - squircleSize / 2f,
        center.y - squircleSize / 2f
    )
    val squircleCorner = CornerRadius(50f, 50f)

    drawRoundRect(
        color = Color(0xFF070707),
        topLeft = squircleTopLeft,
        size = Size(squircleSize, squircleSize),
        cornerRadius = squircleCorner
    )

    // Specular Rim
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.40f)
            ),
            start = squircleTopLeft,
            end = squircleTopLeft + Offset(squircleSize, squircleSize)
        ),
        topLeft = squircleTopLeft,
        size = Size(squircleSize, squircleSize),
        cornerRadius = squircleCorner,
        style = Stroke(width = 1.5f)
    )

    // 3. Precision Radar Telemetry Ring
    val ringRadius = maxRadius * 0.65f
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = ringRadius,
        center = center,
        style = Stroke(width = 1f)
    )

    // Rotating Radar Arc
    val sweepAngleRad = Math.toRadians(radarAngle.toDouble())
    val startAngle = radarAngle
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.7f)),
            center = center
        ),
        startAngle = startAngle,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2f, ringRadius * 2f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // Telemetry Cardinal Ticks
    val tickLength = 7f
    for (i in 0 until 12) {
        val angle = Math.toRadians((i * 30).toDouble())
        val isMajor = i % 3 == 0
        val len = if (isMajor) tickLength * 1.6f else tickLength
        val p1 = Offset(
            center.x + (cos(angle) * (ringRadius - len)).toFloat(),
            center.y + (sin(angle) * (ringRadius - len)).toFloat()
        )
        val p2 = Offset(
            center.x + (cos(angle) * ringRadius).toFloat(),
            center.y + (sin(angle) * ringRadius).toFloat()
        )
        drawLine(
            color = if (isMajor) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
            start = p1,
            end = p2,
            strokeWidth = if (isMajor) 1.8f else 1f
        )
    }

    // 4. Orbiting Platinum Photon Satellites
    val radPhoton = Math.toRadians(photonAngle.toDouble())
    val photonPos1 = Offset(
        center.x + (cos(radPhoton) * ringRadius).toFloat(),
        center.y + (sin(radPhoton) * ringRadius).toFloat()
    )
    val photonPos2 = Offset(
        center.x - (cos(radPhoton) * ringRadius).toFloat(),
        center.y - (sin(radPhoton) * ringRadius).toFloat()
    )

    drawCircle(
        color = Color.White,
        radius = 3.5f,
        center = photonPos1
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = 2.5f,
        center = photonPos2
    )

    // 5. Broadcast Radio Pulse Waves (Emanating outwards)
    val waveRadius = ringRadius * 0.35f + (ringRadius * 0.45f * wavePulse)
    val waveAlpha = (1f - wavePulse).coerceIn(0f, 1f) * 0.6f
    drawCircle(
        color = Color.White.copy(alpha = waveAlpha),
        radius = waveRadius,
        center = center,
        style = Stroke(width = 1.2f)
    )

    // 6. Sculpted Phone Handset Silhouette & 2NR Monogram Core
    // Handset receiver vector path
    val scale = maxRadius / 140f
    val path = Path().apply {
        // Scaled phone receiver curve
        moveTo(center.x - 28f * scale, center.y - 30f * scale)
        cubicTo(
            center.x - 20f * scale, center.y - 38f * scale,
            center.x - 5f * scale, center.y - 30f * scale,
            center.x - 2f * scale, center.y - 18f * scale
        )
        lineTo(center.x + 8f * scale, center.y + 2f * scale)
        cubicTo(
            center.x + 12f * scale, center.y + 10f * scale,
            center.x + 8f * scale, center.y + 20f * scale,
            center.x + 0f * scale, center.y + 26f * scale
        )
        lineTo(center.x - 10f * scale, center.y + 35f * scale)
        cubicTo(
            center.x + 10f * scale, center.y + 55f * scale,
            center.x + 30f * scale, center.y + 70f * scale,
            center.x + 52f * scale, center.y + 76f * scale
        )
        lineTo(center.x + 60f * scale, center.y + 64f * scale)
        cubicTo(
            center.x + 66f * scale, center.y + 58f * scale,
            center.x + 76f * scale, center.y + 55f * scale,
            center.x + 84f * scale, center.y + 60f * scale
        )
        lineTo(center.x + 104f * scale, center.y + 72f * scale)
        cubicTo(
            center.x + 115f * scale, center.y + 80f * scale,
            center.x + 120f * scale, center.y + 92f * scale,
            center.x + 115f * scale, center.y + 102f * scale
        )
        cubicTo(
            center.x + 108f * scale, center.y + 115f * scale,
            center.x + 90f * scale, center.y + 125f * scale,
            center.x + 75f * scale, center.y + 125f * scale
        )
        cubicTo(
            center.x + 15f * scale, center.y + 125f * scale,
            center.x - 40f * scale, center.y + 70f * scale,
            center.x - 40f * scale, center.y + 10f * scale
        )
        cubicTo(
            center.x - 40f * scale, center.y - 5f * scale,
            center.x - 30f * scale, center.y - 22f * scale,
            center.x - 28f * scale, center.y - 30f * scale
        )
        close()
    }

    // Offset handset to center
    val handsetOffset = Offset(-38f * scale, -42f * scale)
    val centeredPath = Path().apply {
        addPath(path, handsetOffset)
    }

    // Draw Handset
    drawPath(
        path = centeredPath,
        color = Color.Black
    )
    drawPath(
        path = centeredPath,
        color = Color.White,
        style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner Platinum Accent Line
    val accentPath = Path().apply {
        moveTo(center.x - 18f * scale, center.y - 20f * scale)
        lineTo(center.x + 2f * scale, center.y + 8f * scale)
        moveTo(center.x + 36f * scale, center.y + 45f * scale)
        lineTo(center.x + 58f * scale, center.y + 60f * scale)
    }
    drawPath(
        path = accentPath,
        color = Color.White.copy(alpha = 0.75f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )

    // 7. Core Status Beacon Dot
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = center
    )
}

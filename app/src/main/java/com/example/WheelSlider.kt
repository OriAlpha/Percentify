package com.example

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun WheelProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentValue by rememberUpdatedState(value)
    val haptic = LocalHapticFeedback.current

    // Dragging state for animations
    var isDragging by remember { mutableStateOf(false) }

    // Haptic tick logging
    var lastTick by remember { mutableIntStateOf(((value / 100f) * 36f).toInt()) }

    // Spring Animations for high-fidelity micro-interactions
    val thumbRadiusDp by animateDpAsState(
        targetValue = if (isDragging) 11.dp else 7.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumb_radius"
    )

    val ambientAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.08f else 0.03f,
        animationSpec = tween(durationMillis = 200),
        label = "ambient_alpha"
    )

    val textScale by animateFloatAsState(
        targetValue = if (isDragging) 1.08f else 1.00f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "text_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(175.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var lastAngle = 0f
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val pos = change.position
                                val dx = pos.x - cx
                                val dy = pos.y - cy
                                val distance = Math.hypot(dx.toDouble(), dy.toDouble())
                                val deadZonePx = 20.dp.toPx()

                                if (distance > deadZonePx) {
                                    val currentAngleDegrees = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()

                                    if (change.pressed) {
                                        if (!change.previousPressed || !isDragging) {
                                            // Gesture Started: snap to initial touch and establish absolute tracking source
                                            isDragging = true
                                            lastAngle = currentAngleDegrees
                                            val normalizedAngle = (currentAngleDegrees + 90f + 360f) % 360f
                                            val pct = (normalizedAngle / 360f) * 100f
                                            val finalPct = pct.coerceIn(0f, 100f)
                                            currentOnValueChange(finalPct)

                                            // Fire click on snap
                                            val tick = ((finalPct / 100f) * 36f).toInt()
                                            if (tick != lastTick) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                lastTick = tick
                                            }
                                        } else {
                                            // Actively dragging: perform seamless relative rotational scaling
                                            var delta = currentAngleDegrees - lastAngle
                                            if (delta > 180f) delta -= 360f
                                            else if (delta < -180f) delta += 360f

                                            // 1.15f is optimized rotation speed for hand-eye drag coordination
                                            val sensitivity = 1.15f
                                            val deltaPct = (delta / 360f) * 100f * sensitivity
                                            val finalPct = (currentValue + deltaPct).coerceIn(0f, 100f)
                                            currentOnValueChange(finalPct)
                                            lastAngle = currentAngleDegrees

                                            // Fire discrete click tactile feeling on each visual tick item transition
                                            val tick = ((finalPct / 100f) * 36f).toInt()
                                            if (tick != lastTick) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                lastTick = tick
                                            }
                                        }
                                    } else {
                                        // Release contact
                                        isDragging = false
                                    }
                                } else {
                                    // Handle coordinate entering deadzone while held down
                                    if (!change.pressed) {
                                        isDragging = false
                                    }
                                }
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val outerRadius = (size.width / 2f) - 10.dp.toPx()
            val innerRadius = outerRadius - 16.dp.toPx()
            val numTicks = 36
            val tickWidth = 3.dp.toPx()

            // Draw center radial ambient glow of active tracker accent color
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = ambientAlpha), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = outerRadius
                )
            )

            // Draw radial ticked ring track
            for (i in 0 until numTicks) {
                val angleDegrees = -90f + (i * (360f / numTicks))
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                val cosVal = Math.cos(angleRad).toFloat()
                val sinVal = Math.sin(angleRad).toFloat()

                // Highlight tick based on active percentage value
                val isHighlighted = i < (value / 100f) * numTicks
                val tickColor = if (isHighlighted) color else color.copy(alpha = 0.15f)

                val startX = centerX + innerRadius * cosVal
                val startY = centerY + innerRadius * sinVal
                val endX = centerX + outerRadius * cosVal
                val endY = centerY + outerRadius * sinVal

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw Glowing interaction thumb selector
            val currentAngleRad = Math.toRadians((-90f + (value / 100f) * 360f).toDouble())
            val thumbRadiusPx = thumbRadiusDp.toPx()
            val thumbX = centerX + ((outerRadius + innerRadius) / 2f) * Math.cos(currentAngleRad).toFloat()
            val thumbY = centerY + ((outerRadius + innerRadius) / 2f) * Math.sin(currentAngleRad).toFloat()

            // Glow backing ring indicator
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx,
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = color,
                radius = Math.max(1f, thumbRadiusPx - 2.5f.dp.toPx()),
                center = Offset(thumbX, thumbY)
            )
        }

        // Inner central numeric content styled with dynamic scale factor
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(
                scaleX = textScale,
                scaleY = textScale
            )
        ) {
            Text(
                text = "${value.toInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "ROTATE TO SET",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFCAC4D0).copy(alpha = 0.6f)
            )
        }
    }
}

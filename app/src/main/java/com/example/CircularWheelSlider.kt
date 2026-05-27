package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class WheelStyle {
    SLEEK_ARC,
    SEGMENTED_DIAL,
    NEON_HALO
}

@Composable
fun CircularWheelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    wheelStyle: WheelStyle = WheelStyle.SLEEK_ARC,
    onWheelStyleChange: (WheelStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val haptic = LocalHapticFeedback.current
    val lastHapticValue = remember { mutableStateOf(-1) }

    fun triggerHapticForValue(newValue: Float) {
        val rounded = newValue.roundToInt()
        if (rounded != lastHapticValue.value) {
            lastHapticValue.value = rounded
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (e: Exception) {
                // Safeguard against missing service
            }
        }
    }

    fun updateValueFromPosition(x: Float, y: Float) {
        if (size.width == 0 || size.height == 0) return
        val dx = x - centerX
        val dy = y - centerY
        val angleRad = atan2(dy, dx)
        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        // 12 o'clock (-90 degrees) represents 0. Clockwise rotation increases the percentage
        var angle = angleDeg + 90f
        if (angle < 0) {
            angle += 360f
        }

        val percentage = (angle / 360f) * 100f
        val coerced = percentage.coerceIn(0f, 100f)
        triggerHapticForValue(coerced)
        onValueChange(coerced)
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(centerX, centerY) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)
                        val maxDimension = size.width.coerceAtMost(size.height)
                        // Trigger only outside the central readout card to allow clicking inside
                        if (distance >= (maxDimension / 2f) * 0.45f) {
                            updateValueFromPosition(offset.x, offset.y)
                        }
                    }
                ) { change, _ ->
                    change.consume()
                    updateValueFromPosition(change.position.x, change.position.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.width.coerceAtMost(size.height) - strokeWidth * 2.5f
            val radius = diameter / 2f

            when (wheelStyle) {
                WheelStyle.SLEEK_ARC -> {
                    // Backing Track Ring - Soft M3 visual style
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Core Arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = (value / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Large Elevated Knob Thumb
                    val angleRad = Math.toRadians(((value / 100f) * 360f - 90f).toDouble())
                    val handleX = centerX + radius * cos(angleRad).toFloat()
                    val handleY = centerY + radius * sin(angleRad).toFloat()

                    // Glow or shadow drop
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.25f),
                        radius = 16.dp.toPx(),
                        center = Offset(handleX, handleY)
                    )

                    // Outer border
                    drawCircle(
                        color = accentColor,
                        radius = 12.dp.toPx(),
                        center = Offset(handleX, handleY)
                    )

                    // White active center pill
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(handleX, handleY)
                    )
                }

                WheelStyle.SEGMENTED_DIAL -> {
                    // Dynamic M3 Tac dial
                    val tickCount = 36
                    val innerRadius = radius - 10.dp.toPx()
                    val outerRadius = radius + 4.dp.toPx()

                    for (i in 0 until tickCount) {
                        val tickAngleDeg = (i * 360f / tickCount) - 90f
                        val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())

                        val tickPercent = (i * 100f) / tickCount
                        val isActive = tickPercent <= value

                        val color = if (isActive) accentColor else Color.White.copy(alpha = 0.08f)
                        val width = if (isActive) 4.dp.toPx() else 2.5.dp.toPx()

                        val startX = centerX + innerRadius * cos(tickAngleRad).toFloat()
                        val startY = centerY + innerRadius * sin(tickAngleRad).toFloat()
                        val endX = centerX + outerRadius * cos(tickAngleRad).toFloat()
                        val endY = centerY + outerRadius * sin(tickAngleRad).toFloat()

                        drawLine(
                            color = color,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = width,
                            cap = StrokeCap.Round
                        )
                    }

                    // Hover dot accent over the active percentage
                    val activeAngleRad = Math.toRadians(((value / 100f) * 360f - 90f).toDouble())
                    val pointerX = centerX + (radius + 12.dp.toPx()) * cos(activeAngleRad).toFloat()
                    val pointerY = centerY + (radius + 12.dp.toPx()) * sin(activeAngleRad).toFloat()

                    drawCircle(
                        color = accentColor,
                        radius = 4.5.dp.toPx(),
                        center = Offset(pointerX, pointerY)
                    )
                }

                WheelStyle.NEON_HALO -> {
                    // Minimal High-End M3 Double Ring
                    val thinTrackWidth = 4.dp.toPx()

                    // Low contrast support ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius,
                        style = Stroke(width = thinTrackWidth)
                    )

                    // High contrast slider arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = (value / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = thinTrackWidth, cap = StrokeCap.Round)
                    )

                    // Outer tracking halo ring
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f),
                        radius = radius + 8.dp.toPx(),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Crosshair controller handle
                    val angleRad = Math.toRadians(((value / 100f) * 360f - 90f).toDouble())
                    val handleX = centerX + radius * cos(angleRad).toFloat()
                    val handleY = centerY + radius * sin(angleRad).toFloat()

                    drawCircle(
                        color = accentColor.copy(alpha = 0.2f),
                        radius = 14.dp.toPx(),
                        center = Offset(handleX, handleY)
                    )

                    val crosshairLen = 12.dp.toPx()
                    drawLine(
                        color = accentColor,
                        start = Offset(handleX - crosshairLen / 2, handleY),
                        end = Offset(handleX + crosshairLen / 2, handleY),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(handleX, handleY - crosshairLen / 2),
                        end = Offset(handleX, handleY + crosshairLen / 2),
                        strokeWidth = 2.dp.toPx()
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(handleX, handleY)
                    )
                }
            }
        }

        // Concentric M3 Container Card inside the center of the wheel.
        // It provides outstanding visual readability over any kind of custom wallpaper
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .size(90.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val currentOrdinal = wheelStyle.ordinal
                    val totalStyles = WheelStyle.entries.size
                    val nextStyle = WheelStyle.entries[(currentOrdinal + 1) % totalStyles]
                    onWheelStyleChange(nextStyle)
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        // ignore
                    }
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "${value.roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (wheelStyle) {
                        WheelStyle.SLEEK_ARC -> "ARC DIAL"
                        WheelStyle.SEGMENTED_DIAL -> "GEAR DIAL"
                        WheelStyle.NEON_HALO -> "NEON HALO"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "TAP TO SWAP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 7.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

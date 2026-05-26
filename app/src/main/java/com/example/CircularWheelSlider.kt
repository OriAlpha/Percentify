package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@Composable
fun CircularWheelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val centerX = size.width / 2f
    val centerY = size.height / 2f

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
        onValueChange(percentage.coerceIn(0f, 100f))
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(centerX, centerY) {
                detectDragGestures(
                    onDragStart = { offset ->
                        updateValueFromPosition(offset.x, offset.y)
                    }
                ) { change, _ ->
                    change.consume()
                    updateValueFromPosition(change.position.x, change.position.y)
                }
            }
            .pointerInput(centerX, centerY) {
                detectTapGestures { offset ->
                    updateValueFromPosition(offset.x, offset.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.width.coerceAtMost(size.height) - strokeWidth * 2
            val radius = diameter / 2f

            // 1. Draw track backing ring
            drawCircle(
                color = Color(0xFF2B2930),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // 2. Draw active progress arc clockwise starting at top
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = (value / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Calculate position of knob handle
            val angleRad = Math.toRadians(((value / 100f) * 360f - 90f).toDouble())
            val handleX = centerX + radius * cos(angleRad).toFloat()
            val handleY = centerY + radius * sin(angleRad).toFloat()

            // Outer handle halo
            drawCircle(
                color = accentColor.copy(alpha = 0.35f),
                radius = 12.dp.toPx(),
                center = Offset(handleX, handleY)
            )

            // Inner handle bullet
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(handleX, handleY)
            )
        }

        // Mid-concentric label display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${value.roundToInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    fontSize = 24.sp
                )
            )
            Text(
                text = "ROTATE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFD0BCFF).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

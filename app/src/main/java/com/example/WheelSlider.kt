package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WheelProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(175.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(value) {
                    var lastAngleDegrees = 0f
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    detectDragGestures(
                        onDragStart = { offset ->
                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            lastAngleDegrees = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        },
                        onDrag = { change, _ ->
                            val pos = change.position
                            val dx = pos.x - cx
                            val dy = pos.y - cy
                            
                            val currentAngleDegrees = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            var delta = currentAngleDegrees - lastAngleDegrees
                            
                            // Proper angle wrap-around normalize
                            if (delta > 180f) delta -= 360f
                            else if (delta < -180f) delta += 360f

                            // Sensitivity factor: 0.5f halves the scroll speed, making rotation highly precise
                            val sensitivity = 0.5f
                            val deltaValue = (delta / 360f) * 100f * sensitivity

                            onValueChange((value + deltaValue).coerceIn(0f, 100f))
                            lastAngleDegrees = currentAngleDegrees
                            change.consume()
                        }
                    )
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val outerRadius = (size.width / 2f) - 10.dp.toPx()
            val innerRadius = outerRadius - 16.dp.toPx()
            val numTicks = 36
            val tickWidth = 3.dp.toPx()

            for (i in 0 until numTicks) {
                val angleDegrees = -90f + (i * (360f / numTicks))
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                val cosVal = Math.cos(angleRad).toFloat()
                val sinVal = Math.sin(angleRad).toFloat()

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

            // Glowing handle/knob
            val currentAngleRad = Math.toRadians((-90f + (value / 100f) * 360f).toDouble())
            val thumbRadius = 7.dp.toPx()
            val thumbX = centerX + ((outerRadius + innerRadius) / 2f) * Math.cos(currentAngleRad).toFloat()
            val thumbY = centerY + ((outerRadius + innerRadius) / 2f) * Math.sin(currentAngleRad).toFloat()

            // Glow ring indicator
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = color,
                radius = thumbRadius - 2.5f.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

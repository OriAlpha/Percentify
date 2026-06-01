package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

object WidgetBitmapRenderer {
    fun drawWheelProgress(percentage: Int, hexColor: String, label: String? = null): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Parse chosen color
        val baseColor = try {
            android.graphics.Color.parseColor(hexColor)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#10B981")
        }

        // Active and inactive paints
        val activePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = baseColor
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
        }

        val inactivePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = baseColor
            alpha = 38 // ~15% alpha
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
        }

        val centerX = size / 2f
        val centerY = size / 2f
        val outerRadius = (size / 2f) - 16f
        val innerRadius = outerRadius - 20f

        val numTicks = 36
        for (i in 0 until numTicks) {
            val angleDegrees = -90f + (i * (360f / numTicks))
            val angleRad = Math.toRadians(angleDegrees.toDouble())
            val cosVal = Math.cos(angleRad).toFloat()
            val sinVal = Math.sin(angleRad).toFloat()

            val isHighlighted = i < (percentage / 100f) * numTicks
            val paint = if (isHighlighted) activePaint else inactivePaint

            val startX = centerX + innerRadius * cosVal
            val startY = centerY + innerRadius * sinVal
            val endX = centerX + outerRadius * cosVal
            val endY = centerY + outerRadius * sinVal

            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Center numeric text
        val numPaint = Paint().apply {
            isAntiAlias = true
            color = baseColor
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Draw percentage number slightly offset upwards if label fits underneath
        val hasLabel = !label.isNullOrBlank()
        val numY = if (hasLabel) (size / 2f) + 8f else (size / 2f) + 16f

        canvas.drawText("$percentage%", size / 2f, numY, numPaint)

        if (hasLabel) {
            val labelPaint = Paint().apply {
                isAntiAlias = true
                color = baseColor
                alpha = 200
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            // Draw truncated label at the bottom inside the circle
            val cleanLabel = if (label.length > 12) label.take(10) + ".." else label
            canvas.drawText(cleanLabel, size / 2f, (size / 2f) + 40f, labelPaint)
        }

        return bitmap
    }

    fun drawStandaloneCircle(percentage: Int, hexColor: String, strokeWidth: Float = 12f, size: Int = 120, isBgOnColor: Boolean = false): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = try {
            android.graphics.Color.parseColor(hexColor)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#10B981")
        }

        val bgPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = if (isBgOnColor) android.graphics.Color.BLACK else baseColor
            alpha = if (isBgOnColor) 55 else 40
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        val progressPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = if (isBgOnColor) android.graphics.Color.WHITE else baseColor
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        val margin = strokeWidth / 2f + 4f
        val rect = RectF(margin, margin, size - margin, size - margin)

        canvas.drawArc(rect, 0f, 360f, false, bgPaint)
        val sweepAngle = (percentage / 100f) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        return bitmap
    }

    fun drawElegantRing(percentage: Int, hexColor: String, label: String? = null): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = try {
            android.graphics.Color.parseColor(hexColor)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#10B981")
        }

        val bgRingPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = baseColor
            alpha = 30
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }

        val progressPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = baseColor
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }

        val margin = 12f
        val rect = RectF(margin, margin, size - margin, size - margin)

        canvas.drawArc(rect, 0f, 360f, false, bgRingPaint)
        val sweepAngle = (percentage / 100f) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        val numPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val numY = (size / 2f) + 16f
        canvas.drawText("$percentage%", size / 2f, numY, numPaint)

        if (!label.isNullOrBlank()) {
            val labelPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                alpha = 150
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            val cleanLabel = if (label.length > 14) label.take(12) + ".." else label
            canvas.drawText(cleanLabel, size / 2f, size - 14f, labelPaint)
        }

        return bitmap
    }
}

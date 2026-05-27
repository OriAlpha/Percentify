package com.example

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class WidgetStyle {
    CIRCLE,
    LINEAR,
    MINIMAL,
    GLOW,
    CORNER_CIRCLE,
    SOLID_FILL
}

enum class WidgetColor(val label: String, val hex: String, val composeColor: Long) {
    EMERALD("Emerald", "#10B981", 0xFF10B981),
    AMETHYST("Amethyst", "#8B5CF6", 0xFF8B5CF6),
    AMBER("Amber", "#F59E0B", 0xFFF59E0B),
    CORAL("Coral", "#F43F5E", 0xFFF43F5E),
    DEEP_BLUE("Deep Blue", "#3B82F6", 0xFF3B82F6);

    companion object {
        fun fromName(name: String?): WidgetColor {
            return entries.find { it.label.equals(name, ignoreCase = true) } ?: EMERALD
        }
    }
}

object WidgetStateKeys {
    val LABEL = stringPreferencesKey("widget_label")
    val VALUE = intPreferencesKey("widget_value")
    val STYLE = stringPreferencesKey("widget_style")
    val COLOR = stringPreferencesKey("widget_color")
    val BACKGROUND_URI = stringPreferencesKey("widget_background_uri")
}

fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri, fileName: String): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            if (bytes.isEmpty()) return null

            // 1. Decode bounds to inspect dimensions safely (low-memory)
            val optionsSize = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, optionsSize)

            // Limit image dimensions to a safe, crisp size (512x512) for home screen widgets
            val maxDimension = 512
            var inSampleSize = 1
            if (optionsSize.outHeight > maxDimension || optionsSize.outWidth > maxDimension) {
                val halfHeight = optionsSize.outHeight / 2
                val halfWidth = optionsSize.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 2. Decode the downscaled bitmap
            val optionsDecode = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, optionsDecode)
                ?: return null

            // 3. Further scale precisely if needed
            val finalBitmap = if (decodedBitmap.width > maxDimension || decodedBitmap.height > maxDimension) {
                val ratio = decodedBitmap.width.toFloat() / decodedBitmap.height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1f) {
                    maxDimension to (maxDimension / ratio).toInt()
                } else {
                    (maxDimension * ratio).toInt() to maxDimension
                }
                android.graphics.Bitmap.createScaledBitmap(decodedBitmap, newWidth, newHeight, true)
            } else {
                decodedBitmap
            }

            // 4. Save highly compressed JPEG to file
            val file = java.io.File(context.filesDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
            }

            // Recycle native bitmap allocations immediately
            if (finalBitmap != decodedBitmap) {
                finalBitmap.recycle()
            }
            decodedBitmap.recycle()

            file.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

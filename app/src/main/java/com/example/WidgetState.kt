package com.example

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class WidgetStyle {
    WHEEL,
    LINEAR,
    MINIMAL,
    GLOW,
    CORNER_CIRCLE,
    SOLID_FILL
}

class WidgetColor(val label: String, val hex: String, val composeColor: Long) {
    companion object {
        val EMERALD = WidgetColor("Emerald", "#10B981", 0xFF10B981)
        val AMETHYST = WidgetColor("Amethyst", "#8B5CF6", 0xFF8B5CF6)
        val AMBER = WidgetColor("Amber", "#F59E0B", 0xFFF59E0B)
        val CORAL = WidgetColor("Coral", "#F43F5E", 0xFFF43F5E)
        val DEEP_BLUE = WidgetColor("Deep Blue", "#3B82F6", 0xFF3B82F6)

        val entries = listOf(EMERALD, AMETHYST, AMBER, CORAL, DEEP_BLUE)

        fun fromName(name: String?): WidgetColor {
            if (name == null || name.isBlank()) return EMERALD
            
            // If it is a hex code
            if (name.startsWith("#")) {
                return fromHex(name)
            }
            
            // Check if match any default preset labels
            val match = entries.find { it.label.equals(name, ignoreCase = true) }
            if (match != null) return match
            
            // Could be a raw hex code without '#'
            if (name.length in 6..8 && name.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                return fromHex("#$name")
            }
            
            return EMERALD
        }

        fun fromHex(hexString: String): WidgetColor {
            try {
                val cleaned = hexString.removePrefix("#").trim()
                val parsedLong = when (cleaned.length) {
                    6 -> ("FF" + cleaned).toLong(16)
                    8 -> cleaned.toLong(16)
                    else -> 0xFF10B981
                }
                val hexRepresentation = "#" + cleaned.uppercase()
                return WidgetColor(hexRepresentation, hexRepresentation, parsedLong)
            } catch (e: Exception) {
                return EMERALD
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WidgetColor) return false
        return hex.lowercase() == other.hex.lowercase()
    }

    override fun hashCode(): Int {
        return hex.lowercase().hashCode()
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

            // Limit image dimensions to a safe, crisp size (300x300) for home screen widgets
            val maxDimension = 300
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

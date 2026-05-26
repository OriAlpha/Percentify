package com.example

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class WidgetStyle {
    CIRCLE,
    LINEAR,
    MINIMAL,
    GLOW,
    CORNER_CIRCLE,
    SOLID_FILL,
    HOLLOW_RING
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
            val file = java.io.File(context.filesDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

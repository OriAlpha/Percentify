package com.example

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.lifecycleScope
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class EditPercentageActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        // If launched without an ID (e.g. from app directly), fall back gracefully
        setContent {
            MyApplicationTheme {
                EditWidgetDialogScreen(
                    appWidgetId = appWidgetId,
                    onDismiss = { finish() },
                    onSaved = { label, value, style, color, bgPath ->
                        saveAndDismiss(label, value, style, color, bgPath)
                    }
                )
            }
        }
    }

    private fun saveAndDismiss(
        label: String,
        value: Int,
        style: WidgetStyle,
        color: WidgetColor,
        bgPath: String?
    ) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "Success: Saved preview settings!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val context = this@EditPercentageActivity
                val manager = GlanceAppWidgetManager(context)
                val glanceId = manager.getGlanceIdBy(appWidgetId)

                // 1. Update Glance DataStore keys for this specific widget instance
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetStateKeys.LABEL] = label
                        this[WidgetStateKeys.VALUE] = value
                        this[WidgetStateKeys.STYLE] = style.name
                        this[WidgetStateKeys.COLOR] = color.label
                        if (bgPath != null) {
                            this[WidgetStateKeys.BACKGROUND_URI] = bgPath
                        } else {
                            remove(WidgetStateKeys.BACKGROUND_URI)
                        }
                    }
                }

                // 2. Refresh widget instance rendering
                PercentifyWidget().update(context, glanceId)

                Toast.makeText(context, "Widget updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditPercentageActivity, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}

@Composable
fun EditWidgetDialogScreen(
    appWidgetId: Int,
    onDismiss: () -> Unit,
    onSaved: (String, Int, WidgetStyle, WidgetColor, String?) -> Unit
) {
    val context = LocalContext.current

    var labelState by remember { mutableStateOf("Progress Meter") }
    var valueState by remember { mutableFloatStateOf(50f) }
    var styleState by remember { mutableStateOf(WidgetStyle.CIRCLE) }
    var colorState by remember { mutableStateOf(WidgetColor.EMERALD) }
    var bgPathState by remember { mutableStateOf<String?>(null) }
    var isLoaded by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) }

    // Load existing settings if valid widget instance
    LaunchedEffect(appWidgetId) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
                
                prefs[WidgetStateKeys.LABEL]?.let { labelState = it }
                prefs[WidgetStateKeys.VALUE]?.let { valueState = it.toFloat() }
                prefs[WidgetStateKeys.STYLE]?.let {
                    styleState = WidgetStyle.valueOf(it)
                }
                prefs[WidgetStateKeys.COLOR]?.let {
                    colorState = WidgetColor.fromName(it)
                }
                prefs[WidgetStateKeys.BACKGROUND_URI]?.let {
                    bgPathState = it
                }
            } catch (e: Exception) {
                // Ignore loading error, keep default
            }
        }
        isLoaded = true
    }

    // Material 3 Dialog styled overlay
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1C1B1F), // Immersive Dark background
            border = BorderStroke(1.dp, Color(0x0CFFFFFF)), // custom border border-white/5
            tonalElevation = 6.dp
        ) {
            if (!isLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(colorState.composeColor))
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Area
                    Column {
                        Text(
                            text = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "Create Percentify" else "Update Percentage",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "Customize your home screen tracker" else "Quickly adjust tracker progress value",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCAC4D0),
                                fontSize = 13.sp
                            )
                        )
                    }

                    // 1. Slider & Instant Number Input Section (ALWAYS VISIBLE, AT THE TOP)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF25232A))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CURRENT PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF),
                                    letterSpacing = 1.sp
                                )
                            )

                            // Editable numeric Text Field so they can type the numbers directly
                            var percentInput by remember(valueState.toInt()) { mutableStateOf(valueState.toInt().toString()) }
                            OutlinedTextField(
                                value = percentInput,
                                onValueChange = { input ->
                                    val cleaned = input.filter { it.isDigit() }
                                    if (cleaned.length <= 3) {
                                        percentInput = cleaned
                                        val nv = cleaned.toIntOrNull()
                                        if (nv != null) {
                                            valueState = nv.coerceIn(0, 100).toFloat()
                                        } else if (cleaned.isEmpty()) {
                                            valueState = 0f
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(colorState.composeColor),
                                    unfocusedTextColor = Color(colorState.composeColor),
                                    focusedContainerColor = Color(0xFF1C1B1F),
                                    unfocusedContainerColor = Color(0xFF1C1B1F),
                                    focusedBorderColor = Color(colorState.composeColor),
                                    unfocusedBorderColor = Color(0xFF49454F)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                suffix = { Text("%", color = Color(colorState.composeColor), fontWeight = FontWeight.Bold) },
                                modifier = Modifier.width(85.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Interactive circular gesture progress wheel / dial
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularWheelSlider(
                                value = valueState,
                                onValueChange = { valueState = it },
                                accentColor = Color(colorState.composeColor),
                                modifier = Modifier.size(130.dp)
                            )
                        }
                    }

                    // 2. Toggle Advanced Design Customizer if modifying an existing widget
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        Surface(
                            onClick = { showAdvanced = !showAdvanced },
                            color = Color(0xFF2B2930),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Design & Label Customization",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFD0BCFF)
                                    )
                                )
                                Text(
                                    text = if (showAdvanced) "▴ Hide" else "▾ Expand",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCAC4D0)
                                    )
                                )
                            }
                        }
                    }

                    // 3. Collapsible Design Properties
                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Label Input section
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "WIDGET LABEL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        letterSpacing = 1.sp
                                    )
                                )
                                OutlinedTextField(
                                    value = labelState,
                                    onValueChange = { if (it.length <= 25) labelState = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFFE6E1E5),
                                        unfocusedTextColor = Color(0xFFCAC4D0),
                                        focusedContainerColor = Color(0xFF2B2930),
                                        unfocusedContainerColor = Color(0xFF2B2930),
                                        focusedBorderColor = Color(colorState.composeColor),
                                        unfocusedBorderColor = Color(0xFF49454F)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            // Style Grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "WIDGET STYLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        letterSpacing = 1.sp
                                    )
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    val styleChunks = WidgetStyle.entries.chunked(2)
                                    styleChunks.forEach { rowStyles ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowStyles.forEach { s ->
                                                val isSelected = styleState == s
                                                val friendlyName = when (s) {
                                                    WidgetStyle.CIRCLE -> "Classic Circle"
                                                    WidgetStyle.GLOW -> "Glow Ambient"
                                                    WidgetStyle.CORNER_CIRCLE -> "Corner Ring"
                                                    WidgetStyle.SOLID_FILL -> "Solid Accent"
                                                    WidgetStyle.HOLLOW_RING -> "Thin Hollow"
                                                    WidgetStyle.LINEAR -> "Bar Progress"
                                                    WidgetStyle.MINIMAL -> "Minimal %"
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(if (isSelected) Color(colorState.composeColor) else Color(0xFF49454F))
                                                        .clickable { styleState = s }
                                                        .padding(12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = friendlyName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) {
                                                                if (colorState == WidgetColor.AMBER) Color.Black else Color.White
                                                            } else Color.White
                                                        )
                                                    )
                                                }
                                            }
                                            if (rowStyles.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // Color Palette Picker
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "ACCENT COLOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        letterSpacing = 1.sp
                                    )
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF2B2930))
                                        .padding(12.dp)
                                ) {
                                    WidgetColor.entries.forEach { c ->
                                        val isSelected = colorState == c
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(c.composeColor))
                                                .border(
                                                    width = if (isSelected) 3.dp else 0.dp,
                                                    color = Color.White,
                                                    shape = CircleShape
                                                )
                                                .clickable { colorState = c }
                                        )
                                    }
                                }
                            }

                            // Custom Background Image
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "WIDGET BACKGROUND",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        letterSpacing = 1.sp
                                    )
                                )

                                val pickerLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetContent()
                                ) { uri ->
                                    if (uri != null) {
                                        val targetFileName = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "bg_preview.jpg" else "bg_widget_${appWidgetId}.jpg"
                                        val path = copyUriToInternalStorage(context, uri, targetFileName)
                                        if (path != null) {
                                            bgPathState = path
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { pickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2B2930),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (bgPathState == null) "Select Photo" else "Change Photo",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    if (bgPathState != null) {
                                        Button(
                                            onClick = { bgPathState = null },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF8C1D18),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Clear",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color(0xFFD0BCFF),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaved(
                                    labelState.ifBlank { "Progress" },
                                    valueState.toInt(),
                                    styleState,
                                    colorState,
                                    bgPathState
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(colorState.composeColor)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Save",
                                color = if (colorState == WidgetColor.AMBER) Color.Black else Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

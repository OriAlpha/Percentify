package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0F0F12) // Immersive deep background
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ambient blurred spots (Top-Left lavender, Bottom-Right blue/teal)
                        Box(
                            modifier = Modifier
                                .offset(x = (-30).dp, y = 40.dp)
                                .size(260.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFD0BCFF).copy(alpha = 0.12f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 50.dp, y = (-80).dp)
                                .size(340.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.09f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )

                        PercentifyDashboardScreen(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PercentifyDashboardScreen(modifier: Modifier = Modifier) {
    var label by remember { mutableStateOf("Water tracker") }
    var value by remember { mutableFloatStateOf(70f) }
    var style by remember { mutableStateOf(WidgetStyle.CIRCLE) }
    var selectedColor by remember { mutableStateOf(WidgetColor.EMERALD) }
    var wheelStyle by remember { mutableStateOf(WheelStyle.SLEEK_ARC) }
    var bgPath by remember { mutableStateOf<String?>(null) }

    val bgBitmap = remember(bgPath) {
        if (!bgPath.isNullOrEmpty()) {
            try {
                android.graphics.BitmapFactory.decodeFile(bgPath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Header Group
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Percentify",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE6E1E5), // Immersive high-contrast text
                    letterSpacing = (-1.5).sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier.testTag("app_title")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Home Screen Progress Widgets",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFCAC4D0), // Subtitle text grey
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // Live Widget Preview Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Immersive Card Dark Base
            border = BorderStroke(1.dp, Color(0x0CFFFFFF)), // Subtle white/5 outer border
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("widget_preview_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Live Preview",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(selectedColor.composeColor),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // The preview container matching a 2x2 widget ratio
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (style == WidgetStyle.SOLID_FILL && bgBitmap == null) Color(selectedColor.composeColor)
                            else Color(0xFF0F0F12)
                        ) // Dark mock workspace
                        .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bgBitmap != null) {
                        Image(
                            bitmap = bgBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                    }

                    // Padded inner container overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (style == WidgetStyle.SOLID_FILL && bgBitmap == null) 0.dp else 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (style) {
                        WidgetStyle.CIRCLE -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val strokeColor = Color(selectedColor.composeColor)
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    // Draw background low alpha ring
                                    drawCircle(
                                        color = strokeColor.copy(alpha = 0.15f),
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // Draw actual progress arc
                                    drawArc(
                                        color = strokeColor,
                                        startAngle = -90f,
                                        sweepAngle = (value / 100f) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = strokeColor,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    if (label.isNotBlank()) {
                                        Text(
                                            text = if (label.length > 10) label.take(8) + ".." else label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFCAC4D0)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        WidgetStyle.LINEAR -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label.ifBlank { "Progress" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(selectedColor.composeColor),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { value / 100f },
                                    color = Color(selectedColor.composeColor),
                                    trackColor = Color(0xFF49454F),
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                            }
                        }
                        WidgetStyle.MINIMAL -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${value.toInt()}%",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = Color(selectedColor.composeColor),
                                        fontWeight = FontWeight.Black
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFCAC4D0),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                        WidgetStyle.GLOW -> {
                            val opacity = 0.15f + (value / 100f) * 0.50f
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(selectedColor.composeColor).copy(alpha = opacity))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label.ifBlank { "Goal" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                            }
                        }
                        WidgetStyle.CORNER_CIRCLE -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                val strokeColor = Color(selectedColor.composeColor)
                                Canvas(modifier = Modifier.size(54.dp).padding(4.dp)) {
                                    drawCircle(
                                        color = strokeColor.copy(alpha = 0.15f),
                                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = strokeColor,
                                        startAngle = -90f,
                                        sweepAngle = (value / 100f) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = label.ifBlank { "Goal Tracker" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFFCAC4D0),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        WidgetStyle.SOLID_FILL -> {
                            val onColor = if (selectedColor == WidgetColor.AMBER) Color.Black else Color.White
                            val subColor = if (selectedColor == WidgetColor.AMBER) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                            val ringBgColor = if (selectedColor == WidgetColor.AMBER) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.2f)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                val strokeColor = Color(selectedColor.composeColor)
                                Canvas(modifier = Modifier.size(54.dp).padding(4.dp)) {
                                    drawCircle(
                                        color = ringBgColor,
                                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = onColor,
                                        startAngle = -90f,
                                        sweepAngle = (value / 100f) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = label.ifBlank { "Goal Tracker" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = onColor,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = subColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        WidgetStyle.HOLLOW_RING -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val strokeColor = Color(selectedColor.composeColor)
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    drawCircle(
                                        color = strokeColor.copy(alpha = 0.12f),
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = strokeColor,
                                        startAngle = -90f,
                                        sweepAngle = (value / 100f) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${value.toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    if (label.isNotBlank()) {
                                        Text(
                                            text = if (label.length > 12) label.take(10) + ".." else label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFCAC4D0),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        // Custom Widget Realtime Playground Customizer
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            border = BorderStroke(1.dp, Color(0x0CFFFFFF)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(selectedColor.composeColor)
                    )
                    Text(
                        text = "Customize Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFE6E1E5),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Input Label Field
                OutlinedTextField(
                    value = label,
                    onValueChange = { if (it.length <= 25) label = it },
                    label = { Text("What are you tracking?", color = Color(0xFFCAC4D0)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFCAC4D0),
                        focusedContainerColor = Color(0xFF2B2930),
                        unfocusedContainerColor = Color(0xFF2B2930),
                        focusedBorderColor = Color(selectedColor.composeColor),
                        unfocusedBorderColor = Color(0xFF49454F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preview_label_input"),
                    singleLine = true
                )

                // Numeric Wheel dial Setting
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Touch & Spin to Set Value",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWheelSlider(
                            value = value,
                            onValueChange = { value = it },
                            accentColor = Color(selectedColor.composeColor),
                            wheelStyle = wheelStyle,
                            onWheelStyleChange = { wheelStyle = it },
                            modifier = Modifier.size(135.dp)
                        )
                    }

                    // Material 3 Interactive Design Style Segmented Control
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        WheelStyle.entries.forEach { styleOpt ->
                            val isSelected = wheelStyle == styleOpt
                            val name = when (styleOpt) {
                                WheelStyle.SLEEK_ARC -> "Sleek Arc"
                                WheelStyle.SEGMENTED_DIAL -> "Gear Dial"
                                WheelStyle.NEON_HALO -> "Neon Halo"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(selectedColor.composeColor) else Color.Transparent)
                                    .clickable { wheelStyle = styleOpt }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSelected) {
                                            if (selectedColor == WidgetColor.AMBER) Color.Black else Color.White
                                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Style Grid Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Layout Style",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val styleChunks = WidgetStyle.entries.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        styleChunks.forEach { rowStyles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowStyles.forEach { s ->
                                    val isSelected = style == s
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(selectedColor.composeColor) else Color(0xFF2B2930))
                                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFF49454F), RoundedCornerShape(12.dp))
                                            .clickable { style = s }
                                            .padding(vertical = 12.dp)
                                            .testTag("style_button_${s.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friendlyName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (isSelected) {
                                                    if (selectedColor == WidgetColor.AMBER) Color.Black else Color.White
                                                } else Color(0xFFCAC4D0),
                                                fontWeight = FontWeight.Bold
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

                // Choose Color Palette
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Customize Asset Color",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Bold
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
                            val isSelected = selectedColor == c
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
                                    .clickable { selectedColor = c }
                                    .testTag("color_button_${c.label.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Choose Background Image
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Background Image Selector",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val pickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            val path = copyUriToInternalStorage(context, uri, "bg_preview.jpg")
                            if (path != null) {
                                bgPath = path
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
                            modifier = Modifier.weight(1f).testTag("select_photo_button")
                        ) {
                            Text(
                                text = if (bgPath == null) "Select Photo" else "Change Photo",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (bgPath != null) {
                            Button(
                                onClick = { bgPath = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8C1D18),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("clear_photo_button")
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

        // Quick Installation Directions Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            border = BorderStroke(1.dp, Color(0x0CFFFFFF)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(selectedColor.composeColor)
                    )
                    Text(
                        text = "How to add home screen widgets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFE6E1E5),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                HorizontalDivider(color = Color(0x0CFFFFFF))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuideStep(index = "1", text = "Go to your Android home screen.", accentColor = Color(selectedColor.composeColor))
                    GuideStep(index = "2", text = "Long press empty area and tap widgets.", accentColor = Color(selectedColor.composeColor))
                    GuideStep(index = "3", text = "Scroll search 'Percentify', drop a widget model onto your screen.", accentColor = Color(selectedColor.composeColor))
                    GuideStep(index = "4", text = "Tap inside the active widget to rename tracker or update progress state instantly anytime!", accentColor = Color(selectedColor.composeColor))
                }
            }
        }
    }
}

@Composable
fun GuideStep(index: String, text: String, accentColor: Color) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFE6E1E5),
                lineHeight = 18.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

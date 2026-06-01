package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Force-refresh all home screen widget instances upon launching the main app.
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(350)
                PercentifyWidget().updateAll(applicationContext)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0F0F12) // Immersive deep dark background
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ambient blurred spots (Top-Left lavender, Bottom-Right blue/teal) for visual depth
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Inject Room Database ViewModel
    val app = context.applicationContext as Application
    val trackerViewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModelFactory(app)
    )
    val trackers by trackerViewModel.allTrackers.collectAsStateWithLifecycle()

    // Playground state variables removed to simplify the dashboard and focus strictly on active trackers.

    // Dialog state controllers
    var showAddDialog by remember { mutableStateOf(false) }
    var trackerToEdit by remember { mutableStateOf<Tracker?>(null) }

    // Dynamic Glance update triggering helper using compose coroutines
    val triggerWidgetUpdate = {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                PercentifyWidget().updateAll(context.applicationContext)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
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
                    color = Color(0xFFE6E1E5),
                    letterSpacing = (-1.5).sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier.testTag("app_title")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Home Screen Progress Widgets & Manager",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFCAC4D0),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // PERSISTENT DATA LIST: "MY ACTIVE TRACKERS" SECTION
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Goal Trackers",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Store and manage multiple progress items",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCAC4D0)
                        )
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2930),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tracker", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            if (trackers.isEmpty()) {
                // Empty state card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0x0CFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No goal trackers yet!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the 'Add' button above to build lists of custom trackers.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFCAC4D0),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                // Persistent Grid Tracker Items List
                val trackersChunked = trackers.chunked(2)
                trackersChunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { tracker ->
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardTrackerCard(
                                    tracker = tracker,
                                    onEditClicked = { trackerToEdit = tracker },
                                    onIncrement = {
                                        val nv = (tracker.value + 5).coerceIn(0, 100)
                                        trackerViewModel.updateTrackerValue(tracker, nv)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        triggerWidgetUpdate()
                                    },
                                    onDecrement = {
                                        val nv = (tracker.value - 5).coerceIn(0, 100)
                                        trackerViewModel.updateTrackerValue(tracker, nv)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        triggerWidgetUpdate()
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
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
                        tint = Color(0xFFD0BCFF)
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

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GuideStep(index = "1", text = "Go to your Android home screen.", accentColor = Color(0xFFD0BCFF))
                    GuideStep(index = "2", text = "Long press empty area and tap widgets.", accentColor = Color(0xFFD0BCFF))
                    GuideStep(index = "3", text = "Scroll search 'Percentify', drop a widget model onto your screen.", accentColor = Color(0xFFD0BCFF))
                    GuideStep(index = "4", text = "Tap inside the active widget to pre-fill configuration fields from your Database Trackers preset and apply beautifully!", accentColor = Color(0xFFD0BCFF))
                }
            }
        }
    }

    // ANDROID PROCESS MODEL DIALOGS: CREATE DIALOG
    if (showAddDialog) {
        TrackerEditDialog(
            tracker = null,
            onDismiss = { showAddDialog = false },
            onSave = { label, value, style, color, bgPath ->
                trackerViewModel.addTracker(label, value, style, color, bgPath)
                showAddDialog = false
                Toast.makeText(context, "Goal added to dashboard!", Toast.LENGTH_SHORT).show()
                triggerWidgetUpdate()
            },
            onDelete = {}
        )
    }

    // ANDROID PROCESS MODEL DIALOGS: EDIT DIALOG
    trackerToEdit?.let { item ->
        TrackerEditDialog(
            tracker = item,
            onDismiss = { trackerToEdit = null },
            onSave = { label, value, style, color, bgPath ->
                val updatedTracker = item.copy(
                    label = label,
                    value = value,
                    style = style.name,
                    color = color.label,
                    bgPath = bgPath
                )
                trackerViewModel.updateTracker(updatedTracker)
                trackerToEdit = null
                Toast.makeText(context, "Goal settings updated!", Toast.LENGTH_SHORT).show()
                triggerWidgetUpdate()
            },
            onDelete = {
                trackerViewModel.deleteTracker(item)
                trackerToEdit = null
                Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show()
                triggerWidgetUpdate()
            }
        )
    }
}

// REUSABLE LAYOUT PREVIEW FOR BOTH PREVIEWS & ACTIVE TILES
@Composable
fun TrackerLayoutPreview(
    label: String,
    value: Int,
    style: WidgetStyle,
    color: WidgetColor,
    bgPath: String?,
    modifier: Modifier = Modifier
) {
    val bgBitmap = remember(bgPath) {
        if (!bgPath.isNullOrEmpty()) {
            try {
                android.graphics.BitmapFactory.decodeFile(bgPath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (style == WidgetStyle.SOLID_FILL && bgBitmap == null) Color(color.composeColor)
                else Color(0xFF0F0F12)
            ),
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (style == WidgetStyle.SOLID_FILL && bgBitmap == null) 8.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (style) {
                WidgetStyle.WHEEL -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeColor = Color(color.composeColor)
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val outerRadius = (size.width / 2f) - 4.dp.toPx()
                            val innerRadius = outerRadius - 10.dp.toPx()
                            val numTicks = 36
                            val tickWidth = 2.dp.toPx()

                            for (i in 0 until numTicks) {
                                val angleDegrees = -90f + (i * (360f / numTicks))
                                val angleRad = Math.toRadians(angleDegrees.toDouble())
                                val cosVal = Math.cos(angleRad).toFloat()
                                val sinVal = Math.sin(angleRad).toFloat()

                                val isHighlighted = i < (value / 100f) * numTicks
                                val tickColor = if (isHighlighted) strokeColor else strokeColor.copy(alpha = 0.15f)

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
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$value%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = strokeColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            if (label.isNotBlank()) {
                                Text(
                                    text = if (label.length > 10) label.take(8) + ".." else label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFCAC4D0),
                                        fontSize = 11.sp
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
                                text = "$value%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(color.composeColor),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { value / 100f },
                            color = Color(color.composeColor),
                            trackColor = Color(0xFF49454F),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
                WidgetStyle.CORNER_CIRCLE -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val strokeColor = Color(color.composeColor)
                        Canvas(modifier = Modifier.size(46.dp).padding(4.dp)) {
                            drawCircle(
                                color = strokeColor.copy(alpha = 0.15f),
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
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = label.ifBlank { "Goal Tracker" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$value%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCAC4D0),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                WidgetStyle.SOLID_FILL -> {
                    val onColor = if (color == WidgetColor.AMBER) Color.Black else Color.White
                    val subColor = if (color == WidgetColor.AMBER) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                    val ringBgColor = if (color == WidgetColor.AMBER) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.2f)

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Canvas(modifier = Modifier.size(46.dp).padding(4.dp)) {
                            drawCircle(
                                color = ringBgColor,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = onColor,
                                startAngle = -90f,
                                sweepAngle = (value / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = label.ifBlank { "Goal Tracker" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = onColor,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$value%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = subColor,
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

// DASHBOARD TILE COMPOSABLE CARD LISTING WITH PLUS & MINUS CONTROLS
@Composable
fun DashboardTrackerCard(
    tracker: Tracker,
    onEditClicked: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val style = remember(tracker.style) {
        try { WidgetStyle.valueOf(tracker.style) } catch (e: Exception) { WidgetStyle.WHEEL }
    }
    val color = remember(tracker.color) {
        WidgetColor.fromName(tracker.color)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row of the card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Tracker",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(color.composeColor),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                IconButton(
                    onClick = onEditClicked,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Goal Layout",
                        tint = Color(0xFFCAC4D0),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Embedded live style preview inside the dashboard list card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                TrackerLayoutPreview(
                    label = tracker.label,
                    value = tracker.value,
                    style = style,
                    color = color,
                    bgPath = tracker.bgPath
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Standard Quick Increment/Decrement Buttons Row supporting Accessibility touch sizes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDecrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2930),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("-5%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(color.composeColor),
                        contentColor = if (color == WidgetColor.AMBER) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("+5%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// PRETTY PROGRESS SETUP GUIDE COMPOSABLE
@Composable
fun GuideStep(index: String, text: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(accentColor.copy(alpha = 0.15f), shape = CircleShape)
                .border(1.dp, accentColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFCAC4D0),
                lineHeight = 18.sp
            )
        )
    }
}

// DETAILED TRACKER CREATION / MODIFICATION OVERLAY MODAL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerEditDialog(
    tracker: Tracker?,
    onDismiss: () -> Unit,
    onSave: (label: String, value: Int, style: WidgetStyle, color: WidgetColor, bgPath: String?) -> Unit,
    onDelete: () -> Unit
) {
    var labelState by remember { mutableStateOf(tracker?.label ?: "") }
    var valueState by remember { mutableFloatStateOf((tracker?.value ?: 50).toFloat()) }
    var styleState by remember {
        mutableStateOf(
            tracker?.style?.let {
                try { WidgetStyle.valueOf(it) } catch (e: Exception) { WidgetStyle.WHEEL }
            } ?: WidgetStyle.WHEEL
        )
    }
    var colorState by remember {
        mutableStateOf(
            tracker?.color?.let { WidgetColor.fromName(it) } ?: WidgetColor.EMERALD
        )
    }
    var bgPathState by remember { mutableStateOf(tracker?.bgPath) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var lastHapticValue by remember { mutableIntStateOf((tracker?.value ?: 50)) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .padding(vertical = 16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1C1B1F),
            border = BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (tracker == null) "Create Tracker" else "Edit Tracker",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                // Label Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Label name",
                        color = Color(0xFFCAC4D0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = labelState,
                        onValueChange = { if (it.length <= 25) labelState = it },
                        placeholder = { Text("Daily Water Intake...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFCAC4D0),
                            focusedContainerColor = Color(0xFF2B2930),
                            unfocusedContainerColor = Color(0xFF2B2930),
                            focusedBorderColor = Color(colorState.composeColor),
                            unfocusedBorderColor = Color(0xFF49454F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_label_input")
                    )
                }

                // Value Slider and Manual Input box
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Target progress percentage",
                        color = Color(0xFFCAC4D0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = valueState,
                            onValueChange = { newValue ->
                                valueState = newValue
                                val currentIntValue = newValue.toInt()
                                if (currentIntValue != lastHapticValue) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastHapticValue = currentIntValue
                                }
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(colorState.composeColor),
                                activeTrackColor = Color(colorState.composeColor),
                                inactiveTrackColor = Color(0xFF49454F)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        var currentTextValue by remember(valueState.toInt()) { mutableStateOf(valueState.toInt().toString()) }
                        OutlinedTextField(
                            value = currentTextValue,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() }
                                if (cleaned.length <= 3) {
                                    currentTextValue = cleaned
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
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color(0xFF2B2930),
                                unfocusedContainerColor = Color(0xFF2B2930),
                                focusedBorderColor = Color(colorState.composeColor)
                            ),
                            modifier = Modifier.width(65.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Layout Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Layout Style",
                        color = Color(0xFFCAC4D0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WidgetStyle.entries.chunked(2).forEach { group ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                group.forEach { s ->
                                    val isSelected = styleState == s
                                    val friendlyName = when (s) {
                                        WidgetStyle.WHEEL -> "Wheel"
                                        WidgetStyle.CORNER_CIRCLE -> "Ring"
                                        WidgetStyle.SOLID_FILL -> "Solid"
                                        WidgetStyle.LINEAR -> "Bar"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(colorState.composeColor) else Color(0xFF2B2930))
                                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFF49454F), RoundedCornerShape(10.dp))
                                            .clickable { styleState = s }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friendlyName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isSelected) {
                                                    if (colorState == WidgetColor.AMBER) Color.Black else Color.White
                                                } else Color(0xFFCAC4D0),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Color Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accent Color",
                        color = Color(0xFFCAC4D0),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val presetColors = remember {
                        listOf(
                            WidgetColor.EMERALD,
                            WidgetColor.AMETHYST,
                            WidgetColor.AMBER,
                            WidgetColor.CORAL
                        )
                    }

                    val isCustomSelected = remember(colorState) {
                        colorState !in presetColors
                    }

                    val rainbowBrush = remember {
                        androidx.compose.ui.graphics.Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFFF0000),
                                Color(0xFFFFFF00),
                                Color(0xFF00FF00),
                                Color(0xFF00FFFF),
                                Color(0xFF0000FF),
                                Color(0xFFFF00FF),
                                Color(0xFFFF0000)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2B2930))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Render the 4 preset circles
                        presetColors.forEach { preset ->
                            val isSelected = colorState == preset
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.composeColor))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { colorState = preset }
                                    .testTag("color_button_${preset.label.lowercase()}")
                            )
                        }

                        // Render the 5th "Custom" circle
                        val customCircleBackground = if (isCustomSelected) {
                            Color(colorState.composeColor)
                        } else {
                            Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isCustomSelected) {
                                        Modifier.background(customCircleBackground)
                                    } else {
                                        Modifier.background(rainbowBrush)
                                    }
                                )
                                .border(
                                    width = if (isCustomSelected) 3.dp else 1.dp,
                                    color = if (isCustomSelected) Color.White else Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val randomHue = (0..359).random().toFloat()
                                    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(randomHue, 0.85f, 0.95f))
                                    val hexString = String.format("#%06X", 0xFFFFFF and hsvColor)
                                    colorState = WidgetColor.fromHex(hexString)
                                }
                                .testTag("color_button_custom"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Color Selector",
                                tint = if (isCustomSelected) {
                                    if (colorState == WidgetColor.AMBER) Color.Black else Color.White
                                } else {
                                    Color.White
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isCustomSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(colorState.composeColor.toInt(), hsv)
                        val hue = hsv[0]

                        val horizontalRainbowBrush = remember {
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF0000),
                                    Color(0xFFFFFF00),
                                    Color(0xFF00FF00),
                                    Color(0xFF00FFFF),
                                    Color(0xFF0000FF),
                                    Color(0xFFFF00FF),
                                    Color(0xFFFF0000)
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hue Spectrum Slider",
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Selected: ${colorState.hex}",
                                    color = Color(colorState.composeColor),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(horizontalRainbowBrush)
                            )

                            Slider(
                                value = hue,
                                onValueChange = { newHue ->
                                    val hsvColor = android.graphics.Color.HSVToColor(floatArrayOf(newHue, 0.85f, 0.95f))
                                    val hexString = String.format("#%06X", 0xFFFFFF and hsvColor)
                                    colorState = WidgetColor.fromHex(hexString)
                                },
                                valueRange = 0f..360f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(colorState.composeColor),
                                    activeTrackColor = Color.Transparent,
                                    inactiveTrackColor = Color.Transparent
                                )
                            )

                            var hexInputState by remember(colorState.hex) { mutableStateOf(colorState.hex) }
                            OutlinedTextField(
                                value = hexInputState,
                                onValueChange = { input ->
                                    val filtered = input.take(7)
                                    hexInputState = filtered
                                    if (filtered.startsWith("#") && filtered.length == 7) {
                                        colorState = WidgetColor.fromHex(filtered)
                                    } else if (!filtered.startsWith("#") && filtered.length == 6) {
                                        colorState = WidgetColor.fromHex("#$filtered")
                                    }
                                },
                                label = { Text("Custom Color Hex", color = Color(0xFFCAC4D0)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFFE6E1E5),
                                    unfocusedTextColor = Color(0xFFCAC4D0),
                                    focusedContainerColor = Color(0xFF2B2930),
                                    unfocusedContainerColor = Color(0xFF2B2930),
                                    focusedBorderColor = Color(colorState.composeColor),
                                    unfocusedBorderColor = Color(0xFF49454F)
                                )
                            )
                        }
                    }
                }

                // Image Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Background Image",
                        color = Color(0xFFCAC4D0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val pickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            val idSuffix = tracker?.id ?: "new"
                            val path = copyUriToInternalStorage(context, uri, "bg_tracker_${idSuffix}.jpg")
                            if (path != null) {
                                bgPathState = path
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { pickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B2930),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (bgPathState == null) "Choose Photo" else "Change Photo",
                                fontSize = 13.sp
                            )
                        }
                        if (bgPathState != null) {
                            Button(
                                onClick = { bgPathState = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8C1D18),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Clear", fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Delete, Cancel, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tracker != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .background(Color(0xFF8C1D18).copy(alpha = 0.15f), shape = CircleShape)
                                .border(1.dp, Color(0xFF8C1D18), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Tracker",
                                tint = Color(0xFFF2B8B5)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFFD0BCFF))
                        }
                        Button(
                            onClick = {
                                if (labelState.isBlank()) {
                                    Toast.makeText(context, "Please enter a label name!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSave(labelState, valueState.toInt(), styleState, colorState, bgPathState)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(colorState.composeColor)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Save",
                                color = if (colorState == WidgetColor.AMBER) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

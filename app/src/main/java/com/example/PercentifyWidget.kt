package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val appWidgetIdKey = ActionParameters.Key<Int>("appWidgetId")

class PercentifyWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val label = prefs[WidgetStateKeys.LABEL] ?: "Goal Progress"
            val value = prefs[WidgetStateKeys.VALUE] ?: 50
            val styleStr = prefs[WidgetStateKeys.STYLE] ?: WidgetStyle.CIRCLE.name
            val style = try {
                WidgetStyle.valueOf(styleStr)
            } catch (e: Exception) {
                WidgetStyle.CIRCLE
            }
            val colorStr = prefs[WidgetStateKeys.COLOR] ?: WidgetColor.EMERALD.label
            val widgetColor = WidgetColor.fromName(colorStr)
            val bgUri = prefs[WidgetStateKeys.BACKGROUND_URI]

            val appWidgetId = try {
                GlanceAppWidgetManager(context).getAppWidgetId(id)
            } catch (e: Exception) {
                AppWidgetManager.INVALID_APPWIDGET_ID
            }

            WidgetContent(
                context = context,
                appWidgetId = appWidgetId,
                label = label,
                value = value,
                style = style,
                widgetColor = widgetColor,
                bgUri = bgUri
            )
        }
    }
}

@Composable
fun WidgetContent(
    context: Context,
    appWidgetId: Int,
    label: String,
    value: Int,
    style: WidgetStyle,
    widgetColor: WidgetColor,
    bgUri: String?
) {
    val bgBitmap = remember(bgUri) {
        if (!bgUri.isNullOrEmpty()) {
            try {
                android.graphics.BitmapFactory.decodeFile(bgUri)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Determine dynamic card background and contents based on the chosen design style
    val cardBackground = when (style) {
        WidgetStyle.SOLID_FILL -> {
            if (bgBitmap != null) ColorProvider(Color.Transparent)
            else ColorProvider(Color(widgetColor.composeColor))
        }
        WidgetStyle.GLOW -> {
            val baseAlphaFactor = 0.15f + (value / 100f) * 0.40f
            ColorProvider(Color(widgetColor.composeColor).copy(alpha = baseAlphaFactor))
        }
        else -> {
            if (bgBitmap != null) ColorProvider(Color.Transparent)
            else ColorProvider(Color(0xFF1C1B1F)) // Elegant custom background
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(cardBackground)
            .cornerRadius(18.dp)
            .clickable(
                actionStartActivity<EditPercentageActivity>(
                    actionParametersOf(appWidgetIdKey to appWidgetId)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bgBitmap != null) {
            Image(
                provider = ImageProvider(bgBitmap),
                contentDescription = "Widget Background",
                modifier = GlanceModifier.fillMaxSize().cornerRadius(18.dp),
                contentScale = ContentScale.Crop
            )
            // Translucent black scrim/tint over the custom background image for excellent visual contrast
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.Black.copy(alpha = 0.45f)))
                    .cornerRadius(18.dp)
            ) {}
        }

        // Inner wrap for widget content padding and styling alignment
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (style) {
            WidgetStyle.CIRCLE -> {
                val circleBitmap = WidgetBitmapRenderer.drawCircleProgress(
                    percentage = value,
                    hexColor = widgetColor.hex,
                    label = label
                )
                Image(
                    provider = ImageProvider(circleBitmap),
                    contentDescription = "Circle Progress Tracker $value%",
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
            WidgetStyle.LINEAR -> {
                val textColor = if (style == WidgetStyle.SOLID_FILL && widgetColor == WidgetColor.AMBER) Color.Black else Color.White
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (label.length > 15) label.take(13) + ".." else label,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Text(
                            text = "$value%",
                            style = TextStyle(
                                color = ColorProvider(if (style == WidgetStyle.SOLID_FILL) textColor else Color(widgetColor.composeColor)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = value / 100f,
                        modifier = GlanceModifier.fillMaxWidth().height(10.dp).cornerRadius(5.dp),
                        color = ColorProvider(if (style == WidgetStyle.SOLID_FILL && widgetColor == WidgetColor.AMBER) Color.Black else Color(widgetColor.composeColor)),
                        backgroundColor = ColorProvider(if (style == WidgetStyle.SOLID_FILL) Color.Black.copy(alpha = 0.2f) else Color(0xFF49454F))
                    )
                }
            }
            WidgetStyle.MINIMAL -> {
                val textColor = if (widgetColor == WidgetColor.AMBER) Color(0xFFE5A93C) else Color(widgetColor.composeColor)
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$value%",
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = label.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFCAC4D0)),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                }
            }
            WidgetStyle.GLOW -> {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (label.length > 14) label.take(12) + ".." else label,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "$value%",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )
                    )
                }
            }
            WidgetStyle.CORNER_CIRCLE -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val circleBitmap = WidgetBitmapRenderer.drawStandaloneCircle(
                        percentage = value,
                        hexColor = widgetColor.hex,
                        size = 110,
                        isBgOnColor = false
                    )
                    Image(
                        provider = ImageProvider(circleBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.size(56.dp)
                    )

                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (label.length > 14) label.take(12) + ".." else label,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "$value%",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFCAC4D0)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
            WidgetStyle.SOLID_FILL -> {
                val onColor = if (widgetColor == WidgetColor.AMBER) Color.Black else Color.White
                val subColor = if (widgetColor == WidgetColor.AMBER) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val circleBitmap = WidgetBitmapRenderer.drawStandaloneCircle(
                        percentage = value,
                        hexColor = widgetColor.hex,
                        size = 110,
                        isBgOnColor = true
                    )
                    Image(
                        provider = ImageProvider(circleBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.size(56.dp)
                    )

                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (label.length > 14) label.take(12) + ".." else label,
                            style = TextStyle(
                                color = ColorProvider(onColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "$value%",
                            style = TextStyle(
                                color = ColorProvider(subColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
        }
    }
}

package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppNameInResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Percentify", appName)
    }

    @Test
    fun testMainActivityLaunchesWithoutCrashing() {
        org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup().get()
    }

    @Test
    fun testDashboardLaunchesAndHasAppTitle() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PercentifyDashboardScreen()
            }
        }
        
        composeTestRule.onNodeWithTag("app_title").assertExists()
        composeTestRule.onNodeWithTag("app_title").assertTextEquals("Percentify")
    }

    @Test
    fun testAllWidgetStylesRenderWithoutCrashing() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PercentifyDashboardScreen()
            }
        }

        // Cycle through all available layout styles to guarantee clean styling values
        WidgetStyle.entries.forEach { style ->
            val tag = "style_button_${style.name.lowercase()}"
            composeTestRule.onNodeWithTag(tag)
                .assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            // Confirm preview container is drawn successfully without crashing
            composeTestRule.onNodeWithTag("widget_preview_card").assertExists()
        }
    }

    @Test
    fun testLabelInputUpdatesState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PercentifyDashboardScreen()
            }
        }

        val testText = "Gym tracker"
        composeTestRule.onNodeWithTag("preview_label_input")
            .assertExists()
            .performTextReplacement(testText)

        composeTestRule.waitForIdle()
        // Re-verify that changed state was preserved in input
        composeTestRule.onNodeWithTag("preview_label_input").assertTextContains(testText)
    }

    @Test
    fun testColorPickerInteractivity() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PercentifyDashboardScreen()
            }
        }

        // Test picking a custom color - Deep Blue
        val colorTag = "color_button_deep_blue"
        composeTestRule.onNodeWithTag(colorTag)
            .assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        // Main view remains healthy
        composeTestRule.onNodeWithTag("widget_preview_card").assertExists()
    }

    @Test
    fun testBackgroundSelectorExists() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PercentifyDashboardScreen()
            }
        }

        // Verify select photo button is rendered successfully and interactive
        composeTestRule.onNodeWithTag("select_photo_button")
            .assertExists()
            .assertHasClickAction()
    }
}

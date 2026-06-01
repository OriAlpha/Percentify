package com.example

import android.appwidget.AppWidgetManager
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
    fun testTrackerEditDialogRendersAndInputsText() {
        composeTestRule.setContent {
            MyApplicationTheme {
                TrackerEditDialog(
                    tracker = null,
                    onDismiss = {},
                    onSave = { _, _, _, _, _ -> },
                    onDelete = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()

        // Verify label input exists and type text safely
        composeTestRule.onNodeWithTag("dialog_label_input").assertExists()
        composeTestRule.onNodeWithTag("dialog_label_input").performTextInput("Gym Habits")
        composeTestRule.onNodeWithTag("dialog_label_input").assertTextContains("Gym Habits")
    }

    @Test
    fun testTrackerEditDialogColorPicking() {
        composeTestRule.setContent {
            MyApplicationTheme {
                TrackerEditDialog(
                    tracker = null,
                    onDismiss = {},
                    onSave = { _, _, _, _, _ -> },
                    onDelete = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify standard picker buttons are clickable without issue
        val emeraldTag = "color_button_emerald"
        composeTestRule.onNodeWithTag(emeraldTag).assertExists().performClick()
    }

    @Test
    fun testEditWidgetDialogRendersSuccessfully() {
        composeTestRule.setContent {
            MyApplicationTheme {
                EditWidgetDialogScreen(
                    appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID,
                    onDismiss = {},
                    onSaved = { _, _, _, _, _ -> }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify that custom interactive wheel progress slider rendering is successful with unmerged tree check
        composeTestRule.onNodeWithTag("value_wheel_slider", useUnmergedTree = true).assertExists()
    }
}

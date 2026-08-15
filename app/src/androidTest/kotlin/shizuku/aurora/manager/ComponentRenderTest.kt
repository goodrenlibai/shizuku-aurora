package shizuku.aurora.manager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import shizuku.aurora.design.components.AuroraListItem
import shizuku.aurora.design.components.EmptyState
import shizuku.aurora.design.components.MetricCard
import shizuku.aurora.design.components.StatusPill
import shizuku.aurora.design.theme.AuroraTheme
import shizuku.aurora.design.theme.StatusTone

/**
 * 渲染测试（instrumented）：验证设计系统组件在真实设备/模拟器上正确渲染。
 */
@RunWith(AndroidJUnit4::class)
class ComponentRenderTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun statusPill_rendersLabel() {
        compose.setContent {
            AuroraTheme { StatusPill(StatusTone.SUCCESS, "RUNNING") }
        }
        compose.onNodeWithText("RUNNING").assertIsDisplayed()
    }

    @Test
    fun metricCard_rendersLabelAndValue() {
        compose.setContent {
            AuroraTheme { MetricCard(label = "CPU", value = "42", unit = "%") }
        }
        compose.onNodeWithText("CPU").assertIsDisplayed()
        compose.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun emptyState_rendersTitle() {
        compose.setContent {
            AuroraTheme {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Filled.Info,
                    title = "No data",
                    description = "Nothing here yet",
                )
            }
        }
        compose.onNodeWithText("No data").assertIsDisplayed()
    }

    @Test
    fun listItem_clickTriggersCallback() {
        var clicked = false
        compose.setContent {
            AuroraTheme {
                AuroraListItem(title = "Item", onClick = { clicked = true })
            }
        }
        compose.onNodeWithText("Item").performClick()
        assertTrue(clicked)
    }
}

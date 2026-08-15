package shizuku.aurora.manager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import shizuku.aurora.design.theme.AuroraTheme
import shizuku.aurora.manager.ui.onboarding.OnboardingScreen

/**
 * 用户流程模拟测试（instrumented）：验证引导流程的完整交互闭环——
 * 翻页 → 下一步 → 完成回调。
 */
@RunWith(AndroidJUnit4::class)
class UserFlowTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun onboarding_swipesThroughPages_andFinishes() {
        var finished = false

        compose.setContent {
            AuroraTheme {
                OnboardingScreen(
                    padding = PaddingValues(0.dp),
                    onFinish = { finished = true },
                )
            }
        }

        // 第 1 页
        compose.onNodeWithText("Elevate your apps").assertIsDisplayed()

        // 前进到第 2 页
        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("Pair wirelessly").assertIsDisplayed()

        // 前进到第 3 页
        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("Full control center").assertIsDisplayed()

        // 完成
        compose.onNodeWithText("Get started").performClick()
        assertTrue(finished)
    }

    @Test
    fun onboarding_skipFinishesImmediately() {
        var finished = false

        compose.setContent {
            AuroraTheme {
                OnboardingScreen(
                    padding = PaddingValues(0.dp),
                    onFinish = { finished = true },
                )
            }
        }

        compose.onNodeWithText("Skip").performClick()
        assertTrue(finished)
    }
}

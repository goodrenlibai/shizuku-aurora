package shizuku.aurora.design.theme

import androidx.compose.ui.unit.dp

/**
 * 设计令牌 · 尺寸与间距（4dp 基准网格）
 * ------------------------------------------------------------------
 * 所有间距严格取自 4dp 网格，杜绝随意取值；卡片内边距、页面边距、
 * 元素间隙全部在此声明，保证跨页面的一致性。
 */
object AuroraDimensions {
    val grid0 = 0.dp
    val grid1 = 4.dp
    val grid2 = 8.dp
    val grid3 = 12.dp
    val grid4 = 16.dp
    val grid5 = 20.dp
    val grid6 = 24.dp
    val grid8 = 32.dp
    val grid10 = 40.dp
    val grid12 = 48.dp

    val screenPadding = 16.dp
    val cardPadding = 16.dp
    val listItemMinHeight = 56.dp
    val chipHeight = 32.dp
    val navBarHeight = 80.dp
    val topBarHeight = 64.dp

    val elevationFlat = 0.dp
    val elevationResting = 1.dp
    val elevationRaised = 3.dp
    val elevationOverlay = 8.dp

    val maxContentWidth = 640.dp
}

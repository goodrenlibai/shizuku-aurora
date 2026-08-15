package shizuku.aurora.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 设计令牌 · 形状体系
 * ------------------------------------------------------------------
 * 统一圆角语言：小控件 8dp、卡片 16dp、大型容器 24dp、全圆用于徽章/头像。
 * 圆角半径构成连续曲线，保证视觉节奏一致。
 */
val AuroraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

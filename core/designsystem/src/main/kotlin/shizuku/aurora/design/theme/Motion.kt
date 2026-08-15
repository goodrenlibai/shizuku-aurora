package shizuku.aurora.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring

/**
 * 设计令牌 · 动效体系
 * ------------------------------------------------------------------
 * 采用 Material 3 标准缓动曲线，并统一时长令牌，保证全应用动效节奏一致：
 *   - 短交互（波纹/按压）≈ 100ms
 *   - 中动效（展开/切换）≈ 250ms
 *   - 大动效（页面转场）≈ 350ms
 * 列表项位移使用物理弹簧，避免生硬。
 */
object AuroraMotion {
    val standardEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val emphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val decelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val accelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

    const val durationShort = 100
    const val durationMedium = 250
    const val durationLong = 350

    fun <T> short(initial: T) = tween<T>(
        durationMillis = durationShort,
        easing = standardEasing,
    )

    fun <T> medium(initial: T) = tween<T>(
        durationMillis = durationMedium,
        easing = standardEasing,
    )

    fun <T> long(initial: T) = tween<T>(
        durationMillis = durationLong,
        easing = standardEasing,
    )

    fun <T> listItem() = spring<T>(
        dampingRatio = 0.8f,
        stiffness = 380f,
    )
}

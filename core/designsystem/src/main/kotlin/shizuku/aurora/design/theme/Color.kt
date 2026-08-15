package shizuku.aurora.design.theme

import androidx.compose.ui.graphics.Color

/**
 * 设计令牌 · 色彩体系
 * ------------------------------------------------------------------
 * 品牌基准：沿用官方 Shizuku 的 Indigo 血统（#3F51B5），并将其升华为
 * 一套完整的 Material 3 色调板（Tonal Palette），同时叠加：
 *   - 动态取色（Dynamic Color，Android 12+ 由系统壁纸派生）
 *   - 纯黑 AMOLED 模式（省电 + 观感）
 * 所有语义色（success/warning/info）统一纳入令牌，供状态徽章、图表复用。
 */

// ---------------------------------------------------------------------------
// 品牌色（Indigo 血统）
// ---------------------------------------------------------------------------
val BrandIndigo = Color(0xFF3F51B5)

// ---------------------------------------------------------------------------
// 浅色主题 · Material 3 色调板
// ---------------------------------------------------------------------------
object LightPalette {
    val primary = Color(0xFF3F51B5)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFFDEE0FF)
    val onPrimaryContainer = Color(0xFF00115C)

    val secondary = Color(0xFF5B5D72)
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFE0E1F9)
    val onSecondaryContainer = Color(0xFF181A2C)

    val tertiary = Color(0xFF75546F)
    val onTertiary = Color(0xFFFFFFFF)
    val tertiaryContainer = Color(0xFFFFD7F2)
    val onTertiaryContainer = Color(0xFF2B1229)

    val error = Color(0xFFBA1A1A)
    val onError = Color(0xFFFFFFFF)
    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF410002)

    val background = Color(0xFFFBF8FF)
    val onBackground = Color(0xFF1B1B21)
    val surface = Color(0xFFFBF8FF)
    val onSurface = Color(0xFF1B1B21)
    val surfaceVariant = Color(0xFFE2E1EC)
    val onSurfaceVariant = Color(0xFF45464F)
    val outline = Color(0xFF757680)
    val outlineVariant = Color(0xFFC6C6D0)

    val surfaceContainerLowest = Color(0xFFFFFFFF)
    val surfaceContainerLow = Color(0xFFF5F5FA)
    val surfaceContainer = Color(0xFFEFEFF4)
    val surfaceContainerHigh = Color(0xFFE9E9EF)
    val surfaceContainerHighest = Color(0xFFE3E3E9)

    val inverseSurface = Color(0xFF303036)
    val inverseOnSurface = Color(0xFFF2F0F7)
    val inversePrimary = Color(0xFFBAC3FF)
    val scrim = Color(0xFF000000)

    // 语义扩展
    val success = Color(0xFF2E7D32)
    val onSuccess = Color(0xFFFFFFFF)
    val successContainer = Color(0xFFB7F0B9)
    val onSuccessContainer = Color(0xFF002105)

    val warning = Color(0xFF8A5300)
    val onWarning = Color(0xFFFFFFFF)
    val warningContainer = Color(0xFFFFDCC2)
    val onWarningContainer = Color(0xFF2C1600)

    val info = Color(0xFF00639B)
    val onInfo = Color(0xFFFFFFFF)
    val infoContainer = Color(0xFFCEE5FF)
    val onInfoContainer = Color(0xFF001D33)
}

// ---------------------------------------------------------------------------
// 深色主题 · Material 3 色调板
// ---------------------------------------------------------------------------
object DarkPalette {
    val primary = Color(0xFFBAC3FF)
    val onPrimary = Color(0xFF08218A)
    val primaryContainer = Color(0xFF263B8F)
    val onPrimaryContainer = Color(0xFFDEE0FF)

    val secondary = Color(0xFFC4C5DD)
    val onSecondary = Color(0xFF2D2F42)
    val secondaryContainer = Color(0xFF444559)
    val onSecondaryContainer = Color(0xFFE0E1F9)

    val tertiary = Color(0xFFE4BAD7)
    val onTertiary = Color(0xFF432739)
    val tertiaryContainer = Color(0xFF5B3D53)
    val onTertiaryContainer = Color(0xFFFFD7F2)

    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)

    val background = Color(0xFF121318)
    val onBackground = Color(0xFFE3E2E9)
    val surface = Color(0xFF121318)
    val onSurface = Color(0xFFE3E2E9)
    val surfaceVariant = Color(0xFF45464F)
    val onSurfaceVariant = Color(0xFFC6C6D0)
    val outline = Color(0xFF90909B)
    val outlineVariant = Color(0xFF45464F)

    val surfaceContainerLowest = Color(0xFF0D0E13)
    val surfaceContainerLow = Color(0xFF1B1B21)
    val surfaceContainer = Color(0xFF1F1F25)
    val surfaceContainerHigh = Color(0xFF29292F)
    val surfaceContainerHighest = Color(0xFF34343A)

    val inverseSurface = Color(0xFFE3E2E9)
    val inverseOnSurface = Color(0xFF303036)
    val inversePrimary = Color(0xFF3F51B5)
    val scrim = Color(0xFF000000)

    val success = Color(0xFF7FE08A)
    val onSuccess = Color(0xFF00390A)
    val successContainer = Color(0xFF005316)
    val onSuccessContainer = Color(0xFFB7F0B9)

    val warning = Color(0xFFFFB870)
    val onWarning = Color(0xFF4A2900)
    val warningContainer = Color(0xFF693D00)
    val onWarningContainer = Color(0xFFFFDCC2)

    val info = Color(0xFF93CCFF)
    val onInfo = Color(0xFF003353)
    val infoContainer = Color(0xFF004A76)
    val onInfoContainer = Color(0xFFCEE5FF)
}

// ---------------------------------------------------------------------------
// AMOLED 纯黑模式：仅覆盖最底层的背景/表面，其余沿用 DarkPalette
// ---------------------------------------------------------------------------
object AmoledPalette {
    val background = Color(0xFF000000)
    val onBackground = Color(0xFFE3E2E9)
    val surface = Color(0xFF000000)
    val onSurface = Color(0xFFE3E2E9)
    val surfaceContainerLowest = Color(0xFF000000)
    val surfaceContainerLow = Color(0xFF050505)
    val surfaceContainer = Color(0xFF0A0A0C)
    val surfaceContainerHigh = Color(0xFF101014)
    val surfaceContainerHighest = Color(0xFF16161B)
}

/**
 * 状态语义色令牌（供状态徽章统一取色）。
 * 每种状态提供 前景/容器 两个值，适配浅/深主题。
 */
enum class StatusTone { SUCCESS, WARNING, ERROR, INFO, NEUTRAL }

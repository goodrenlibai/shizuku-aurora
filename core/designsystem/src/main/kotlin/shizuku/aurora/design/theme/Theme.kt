package shizuku.aurora.design.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 设计令牌 · 主题入口
 * ------------------------------------------------------------------
 * 主题模式由 [AuroraThemeMode] 决定：
 *   SYSTEM   跟随系统（Android 12+ 支持动态取色）
 *   LIGHT    强制浅色
 *   DARK     强制深色
 *   AMOLED   纯黑深色
 * 动态取色仅在高版本系统且非 AMOLED 时启用，保证低版本兼容。
 */
enum class AuroraThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

private val LightColors = lightColorScheme(
    primary = LightPalette.primary,
    onPrimary = LightPalette.onPrimary,
    primaryContainer = LightPalette.primaryContainer,
    onPrimaryContainer = LightPalette.onPrimaryContainer,
    secondary = LightPalette.secondary,
    onSecondary = LightPalette.onSecondary,
    secondaryContainer = LightPalette.secondaryContainer,
    onSecondaryContainer = LightPalette.onSecondaryContainer,
    tertiary = LightPalette.tertiary,
    onTertiary = LightPalette.onTertiary,
    tertiaryContainer = LightPalette.tertiaryContainer,
    onTertiaryContainer = LightPalette.onTertiaryContainer,
    error = LightPalette.error,
    onError = LightPalette.onError,
    errorContainer = LightPalette.errorContainer,
    onErrorContainer = LightPalette.onErrorContainer,
    background = LightPalette.background,
    onBackground = LightPalette.onBackground,
    surface = LightPalette.surface,
    onSurface = LightPalette.onSurface,
    surfaceVariant = LightPalette.surfaceVariant,
    onSurfaceVariant = LightPalette.onSurfaceVariant,
    outline = LightPalette.outline,
    outlineVariant = LightPalette.outlineVariant,
    surfaceContainerLowest = LightPalette.surfaceContainerLowest,
    surfaceContainerLow = LightPalette.surfaceContainerLow,
    surfaceContainer = LightPalette.surfaceContainer,
    surfaceContainerHigh = LightPalette.surfaceContainerHigh,
    surfaceContainerHighest = LightPalette.surfaceContainerHighest,
    inverseSurface = LightPalette.inverseSurface,
    inverseOnSurface = LightPalette.inverseOnSurface,
    inversePrimary = LightPalette.inversePrimary,
    scrim = LightPalette.scrim,
)

private val DarkColors = darkColorScheme(
    primary = DarkPalette.primary,
    onPrimary = DarkPalette.onPrimary,
    primaryContainer = DarkPalette.primaryContainer,
    onPrimaryContainer = DarkPalette.onPrimaryContainer,
    secondary = DarkPalette.secondary,
    onSecondary = DarkPalette.onSecondary,
    secondaryContainer = DarkPalette.secondaryContainer,
    onSecondaryContainer = DarkPalette.onSecondaryContainer,
    tertiary = DarkPalette.tertiary,
    onTertiary = DarkPalette.onTertiary,
    tertiaryContainer = DarkPalette.tertiaryContainer,
    onTertiaryContainer = DarkPalette.onTertiaryContainer,
    error = DarkPalette.error,
    onError = DarkPalette.onError,
    errorContainer = DarkPalette.errorContainer,
    onErrorContainer = DarkPalette.onErrorContainer,
    background = DarkPalette.background,
    onBackground = DarkPalette.onBackground,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.onSurface,
    surfaceVariant = DarkPalette.surfaceVariant,
    onSurfaceVariant = DarkPalette.onSurfaceVariant,
    outline = DarkPalette.outline,
    outlineVariant = DarkPalette.outlineVariant,
    surfaceContainerLowest = DarkPalette.surfaceContainerLowest,
    surfaceContainerLow = DarkPalette.surfaceContainerLow,
    surfaceContainer = DarkPalette.surfaceContainer,
    surfaceContainerHigh = DarkPalette.surfaceContainerHigh,
    surfaceContainerHighest = DarkPalette.surfaceContainerHighest,
    inverseSurface = DarkPalette.inverseSurface,
    inverseOnSurface = DarkPalette.inverseOnSurface,
    inversePrimary = DarkPalette.inversePrimary,
    scrim = DarkPalette.scrim,
)

private val AmoledColors = DarkColors.copy(
    background = AmoledPalette.background,
    onBackground = AmoledPalette.onBackground,
    surface = AmoledPalette.surface,
    onSurface = AmoledPalette.onSurface,
    surfaceContainerLowest = AmoledPalette.surfaceContainerLowest,
    surfaceContainerLow = AmoledPalette.surfaceContainerLow,
    surfaceContainer = AmoledPalette.surfaceContainer,
    surfaceContainerHigh = AmoledPalette.surfaceContainerHigh,
    surfaceContainerHighest = AmoledPalette.surfaceContainerHighest,
)

/**
 * 主题唯一入口。传入用户选择的模式与动态取色开关。
 */
@Composable
fun AuroraTheme(
    mode: AuroraThemeMode = AuroraThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (mode) {
        AuroraThemeMode.SYSTEM -> isSystemInDarkTheme()
        AuroraThemeMode.LIGHT -> false
        AuroraThemeMode.DARK, AuroraThemeMode.AMOLED -> true
    }

    val colorScheme: ColorScheme = when {
        mode == AuroraThemeMode.AMOLED -> AmoledColors
        mode == AuroraThemeMode.LIGHT -> LightColors
        mode == AuroraThemeMode.DARK -> DarkColors
        else -> {
            val context = LocalContext.current
            val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            when {
                supportsDynamic && dynamicColor -> if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColors
                else -> LightColors
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuroraTypography,
        shapes = AuroraShapes,
        content = content,
    )
}

/**
 * 语义状态色解析：把 [StatusTone] 映射为 前景/容器 一对颜色。
 */
@Composable
fun statusToneColors(tone: StatusTone): Pair<Color, Color> {
    val light = !isSystemInDarkTheme()
    return when (tone) {
        StatusTone.SUCCESS -> if (light) {
            LightPalette.successContainer to LightPalette.onSuccessContainer
        } else {
            DarkPalette.successContainer to DarkPalette.onSuccessContainer
        }
        StatusTone.WARNING -> if (light) {
            LightPalette.warningContainer to LightPalette.onWarningContainer
        } else {
            DarkPalette.warningContainer to DarkPalette.onWarningContainer
        }
        StatusTone.ERROR -> if (light) {
            LightPalette.errorContainer to LightPalette.onErrorContainer
        } else {
            DarkPalette.errorContainer to DarkPalette.onErrorContainer
        }
        StatusTone.INFO -> if (light) {
            LightPalette.infoContainer to LightPalette.onInfoContainer
        } else {
            DarkPalette.infoContainer to DarkPalette.onInfoContainer
        }
        StatusTone.NEUTRAL -> if (light) {
            LightPalette.surfaceContainerHighest to LightPalette.onSurfaceVariant
        } else {
            DarkPalette.surfaceContainerHighest to DarkPalette.onSurfaceVariant
        }
    }
}

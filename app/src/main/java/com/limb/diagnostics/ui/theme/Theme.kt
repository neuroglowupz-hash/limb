package com.limb.diagnostics.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class LimbCustomColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val statusPass: Color,
    val statusPassBg: Color,
    val statusWarning: Color,
    val statusWarningBg: Color,
    val statusFail: Color,
    val statusFailBg: Color,
    val statusInfo: Color,
    val statusNeutral: Color,
    val statusNeutralBg: Color
)

val LocalLimbColors = staticCompositionLocalOf {
    LimbCustomColors(
        background = LimbDarkBackground,
        surface = LimbDarkSurface,
        surfaceElevated = LimbDarkSurfaceElevated,
        border = LimbDarkBorder,
        textPrimary = LimbDarkTextPrimary,
        textSecondary = LimbDarkTextSecondary,
        textTertiary = LimbDarkTextTertiary,
        accent = LimbDarkAccent,
        statusPass = LimbStatusPass,
        statusPassBg = LimbStatusPassBgDark,
        statusWarning = LimbStatusWarning,
        statusWarningBg = LimbStatusWarningBgDark,
        statusFail = LimbStatusFail,
        statusFailBg = LimbStatusFailBgDark,
        statusInfo = LimbStatusInfo,
        statusNeutral = LimbStatusNeutral,
        statusNeutralBg = LimbStatusNeutralBg
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = LimbDarkAccent,
    onPrimary = LimbDarkBackground,
    background = LimbDarkBackground,
    onBackground = LimbDarkTextPrimary,
    surface = LimbDarkSurface,
    onSurface = LimbDarkTextPrimary,
    surfaceVariant = LimbDarkSurfaceElevated,
    onSurfaceVariant = LimbDarkTextSecondary,
    outline = LimbDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LimbLightAccent,
    onPrimary = LimbLightBackground,
    background = LimbLightBackground,
    onBackground = LimbLightTextPrimary,
    surface = LimbLightSurface,
    onSurface = LimbLightTextPrimary,
    surfaceVariant = LimbLightSurfaceElevated,
    onSurfaceVariant = LimbLightTextSecondary,
    outline = LimbLightBorder
)

@Composable
fun LimbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        LimbCustomColors(
            background = LimbDarkBackground,
            surface = LimbDarkSurface,
            surfaceElevated = LimbDarkSurfaceElevated,
            border = LimbDarkBorder,
            textPrimary = LimbDarkTextPrimary,
            textSecondary = LimbDarkTextSecondary,
            textTertiary = LimbDarkTextTertiary,
            accent = LimbDarkAccent,
            statusPass = LimbStatusPass,
            statusPassBg = LimbStatusPassBgDark,
            statusWarning = LimbStatusWarning,
            statusWarningBg = LimbStatusWarningBgDark,
            statusFail = LimbStatusFail,
            statusFailBg = LimbStatusFailBgDark,
            statusInfo = LimbStatusInfo,
            statusNeutral = LimbStatusNeutral,
            statusNeutralBg = LimbStatusNeutralBg
        )
    } else {
        LimbCustomColors(
            background = LimbLightBackground,
            surface = LimbLightSurface,
            surfaceElevated = LimbLightSurfaceElevated,
            border = LimbLightBorder,
            textPrimary = LimbLightTextPrimary,
            textSecondary = LimbLightTextSecondary,
            textTertiary = LimbLightTextTertiary,
            accent = LimbLightAccent,
            statusPass = LimbStatusPass,
            statusPassBg = LimbStatusPassBgLight,
            statusWarning = LimbStatusWarning,
            statusWarningBg = LimbStatusWarningBgLight,
            statusFail = LimbStatusFail,
            statusFailBg = LimbStatusFailBgLight,
            statusInfo = LimbStatusInfo,
            statusNeutral = LimbStatusNeutral,
            statusNeutralBg = LimbStatusNeutralBg
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = customColors.background.toArgb()
                it.navigationBarColor = customColors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLimbColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LimbTypography,
            content = content
        )
    }
}

object LimbAppTheme {
    val colors: LimbCustomColors
        @Composable
        get() = LocalLimbColors.current
}

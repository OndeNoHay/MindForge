package com.mindforge.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = OnPrimaryDark,
    secondary = SecondaryGreen,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryGreenLight,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = TertiaryOrange,
    onTertiary = OnPrimaryLight,
    tertiaryContainer = TertiaryOrangeLight,
    onTertiaryContainer = OnPrimaryDark,
    error = ErrorRed,
    onError = OnPrimaryLight,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    outlineVariant = Gray200,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = SecondaryGreenLight,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryGreenDark,
    onSecondaryContainer = SecondaryGreenLight,
    tertiary = TertiaryOrangeLight,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = TertiaryOrangeDark,
    onTertiaryContainer = TertiaryOrangeLight,
    error = ErrorRedLight,
    onError = OnPrimaryDark,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRedLight,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray800,
)

/**
 * MindForge theme with Material 3 support
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic colors (Android 12+)
 * @param content Composable content
 */
@Composable
fun MindForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

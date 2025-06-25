package com.studywise.ai.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.studywise.ai.data.local.preferences.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = OnSecondary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = PrimaryHighContrast,
    onPrimary = OnPrimaryHighContrast,
    primaryContainer = PrimaryHighContrast,
    onPrimaryContainer = OnPrimaryHighContrast,
    secondary = SecondaryHighContrast,
    onSecondary = OnSecondaryHighContrast,
    secondaryContainer = SecondaryHighContrast,
    onSecondaryContainer = OnSecondaryHighContrast,
    background = BackgroundHighContrast,
    onBackground = OnBackgroundHighContrast,
    surface = SurfaceHighContrast,
    onSurface = OnSurfaceHighContrast,
    error = PrimaryHighContrast,
    onError = OnPrimaryHighContrast
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = OnPrimaryHighContrast,
    onPrimary = PrimaryHighContrast,
    primaryContainer = OnPrimaryHighContrast,
    onPrimaryContainer = PrimaryHighContrast,
    secondary = OnSecondaryHighContrast,
    onSecondary = SecondaryHighContrast,
    secondaryContainer = OnSecondaryHighContrast,
    onSecondaryContainer = SecondaryHighContrast,
    background = PrimaryHighContrast,
    onBackground = OnBackgroundHighContrast,
    surface = PrimaryHighContrast,
    onSurface = OnSurfaceHighContrast,
    error = OnPrimaryHighContrast,
    onError = PrimaryHighContrast
)

data class StudyWiseThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: Float = 1.0f,
    val highContrast: Boolean = false
)

val LocalThemeSettings = staticCompositionLocalOf { StudyWiseThemeSettings() }

@Composable
fun StudyWiseTheme(
    themeSettings: StudyWiseThemeSettings = StudyWiseThemeSettings(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeSettings.highContrast -> {
            if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
        }
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

    CompositionLocalProvider(LocalThemeSettings provides themeSettings) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getScaledTypography(themeSettings.textScale),
            content = content
        )
    }
}
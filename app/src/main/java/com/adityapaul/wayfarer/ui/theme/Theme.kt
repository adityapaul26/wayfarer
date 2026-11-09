package com.adityapaul.wayfarer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WayfarerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    palette: AppColorPalette = AppColorPalette.SLATE_BLUE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primaryDark,
            onPrimary = Slate900,
            primaryContainer = palette.accentDark.copy(alpha = 0.25f),
            onPrimaryContainer = palette.primaryDark,
            secondary = palette.accentDark,
            onSecondary = Slate900,
            secondaryContainer = Slate800,
            onSecondaryContainer = palette.accentDark,
            background = Slate950,
            onBackground = Slate50,
            surface = Slate900,
            onSurface = Slate50,
            surfaceVariant = Slate800,
            onSurfaceVariant = Slate400,
            outline = Slate700
        )
    } else {
        lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = Color.White,
            primaryContainer = palette.accentLight.copy(alpha = 0.15f),
            onPrimaryContainer = palette.accentLight,
            secondary = palette.accentLight,
            onSecondary = Color.White,
            secondaryContainer = Slate100,
            onSecondaryContainer = Slate800,
            background = Color.White,
            onBackground = Slate900,
            surface = Color.White,
            onSurface = Slate900,
            surfaceVariant = Slate100,
            onSurfaceVariant = Slate600,
            outline = Slate200
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

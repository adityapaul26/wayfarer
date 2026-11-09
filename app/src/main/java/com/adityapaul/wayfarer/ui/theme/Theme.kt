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
    palette: AppColorPalette = AppColorPalette.LAVENDER_PURPLE,
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
            onPrimary = palette.onPrimaryDark,
            primaryContainer = palette.primaryContainerDark,
            onPrimaryContainer = palette.onPrimaryContainerDark,
            secondary = palette.secondaryDark,
            onSecondary = Color(0xFF1D192B),
            secondaryContainer = palette.secondaryContainerDark,
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = palette.tertiaryDark,
            onTertiary = Color(0xFF492532),
            tertiaryContainer = palette.tertiaryContainerDark,
            onTertiaryContainer = Color(0xFFFFD8E4),
            background = palette.surfaceDark,
            onBackground = Color(0xFFE6E1E5),
            surface = palette.surfaceDark,
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = palette.surfaceContainerDark,
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F96),
            outlineVariant = Color(0xFF49454F)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = palette.onPrimaryLight,
            primaryContainer = palette.primaryContainerLight,
            onPrimaryContainer = palette.onPrimaryContainerLight,
            secondary = palette.secondaryLight,
            onSecondary = Color.White,
            secondaryContainer = palette.secondaryContainerLight,
            onSecondaryContainer = Color(0xFF1D192B),
            tertiary = palette.tertiaryLight,
            onTertiary = Color.White,
            tertiaryContainer = palette.tertiaryContainerLight,
            onTertiaryContainer = Color(0xFF31111D),
            background = palette.surfaceLight,
            onBackground = Color(0xFF1C1B1F),
            surface = palette.surfaceLight,
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = palette.surfaceContainerLight,
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0)
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

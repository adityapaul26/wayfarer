package com.adityapaul.wayfarer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val ExpressiveShapes = Shapes(
    extraLarge = RoundedCornerShape(32.dp),
    large = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(18.dp),
    small = CircleShape
)

@Composable
fun WayfarerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    palette: AppColorPalette = AppColorPalette.LAVENDER_PURPLE,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamicColor && supportsDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor && supportsDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = palette.primaryDark,
            onPrimary = palette.onPrimaryDark,
            primaryContainer = palette.primaryContainerDark,
            onPrimaryContainer = palette.onPrimaryContainerDark,
            secondary = palette.secondaryDark,
            onSecondary = OnAcidLime,
            secondaryContainer = palette.secondaryContainerDark,
            onSecondaryContainer = AcidLime,
            tertiary = palette.tertiaryDark,
            onTertiary = OnPastelCoral,
            tertiaryContainer = palette.tertiaryContainerDark,
            onTertiaryContainer = palette.tertiaryDark,
            background = ObsidianPlum,
            onBackground = SoftLilacWhite,
            surface = ObsidianPlum,
            onSurface = SoftLilacWhite,
            surfaceVariant = DarkSurfaceContainer,
            onSurfaceVariant = Color(0xFFE4D1FF),
            surfaceContainer = DarkSurfaceContainer,
            surfaceContainerHigh = DarkSurfaceContainerHigh,
            outline = Color(0xFF9981B5),
            outlineVariant = Color(0xFF4A3A5E)
        )
        else -> lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = palette.onPrimaryLight,
            primaryContainer = palette.primaryContainerLight,
            onPrimaryContainer = palette.onPrimaryContainerLight,
            secondary = palette.secondaryLight,
            onSecondary = Color.White,
            secondaryContainer = palette.secondaryContainerLight,
            onSecondaryContainer = palette.primaryLight,
            tertiary = palette.tertiaryLight,
            onTertiary = Color.White,
            tertiaryContainer = palette.tertiaryContainerLight,
            onTertiaryContainer = Color(0xFF400010),
            background = SoftLilacWhite,
            onBackground = ObsidianPlum,
            surface = SoftLilacWhite,
            onSurface = ObsidianPlum,
            surfaceVariant = LightSurfaceContainer,
            onSurfaceVariant = Color(0xFF4A3A5E),
            surfaceContainer = LightSurfaceContainer,
            surfaceContainerHigh = LightSurfaceContainerHigh,
            outline = Color(0xFF816D99),
            outlineVariant = Color(0xFFE4D1FF)
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
        shapes = ExpressiveShapes,
        content = content
    )
}

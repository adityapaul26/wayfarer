package com.adityapaul.wayfarer.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AppColorPalette(
    val id: String,
    val displayName: String,
    val primaryLight: Color,
    val accentLight: Color,
    val primaryDark: Color,
    val accentDark: Color,
    val previewColor: Color
) {
    SLATE_BLUE(
        id = "slate_blue",
        displayName = "Slate Blue",
        primaryLight = Color(0xFF0F172A),
        accentLight = Color(0xFF2563EB),
        primaryDark = Color(0xFFF8FAFC),
        accentDark = Color(0xFF3B82F6),
        previewColor = Color(0xFF2563EB)
    ),
    EMERALD_FOREST(
        id = "emerald_forest",
        displayName = "Emerald",
        primaryLight = Color(0xFF064E3B),
        accentLight = Color(0xFF059669),
        primaryDark = Color(0xFFECFDF5),
        accentDark = Color(0xFF10B981),
        previewColor = Color(0xFF059669)
    ),
    SUNSET_CRIMSON(
        id = "sunset_crimson",
        displayName = "Crimson",
        primaryLight = Color(0xFF881337),
        accentLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFFFF1F2),
        accentDark = Color(0xFFF43F5E),
        previewColor = Color(0xFFE11D48)
    ),
    ROYAL_VIOLET(
        id = "royal_violet",
        displayName = "Violet",
        primaryLight = Color(0xFF3B0764),
        accentLight = Color(0xFF7C3AED),
        primaryDark = Color(0xFFF5F3FF),
        accentDark = Color(0xFF8B5CF6),
        previewColor = Color(0xFF7C3AED)
    ),
    AMBER_WARMTH(
        id = "amber_warmth",
        displayName = "Amber",
        primaryLight = Color(0xFF78350F),
        accentLight = Color(0xFFD97706),
        primaryDark = Color(0xFFFFFBEB),
        accentDark = Color(0xFFF59E0B),
        previewColor = Color(0xFFD97706)
    ),
    CYAN_OCEAN(
        id = "cyan_ocean",
        displayName = "Cyan",
        primaryLight = Color(0xFF164E63),
        accentLight = Color(0xFF0891B2),
        primaryDark = Color(0xFFECFEFF),
        accentDark = Color(0xFF06B6D4),
        previewColor = Color(0xFF0891B2)
    )
}

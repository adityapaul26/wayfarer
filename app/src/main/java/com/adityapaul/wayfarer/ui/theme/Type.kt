package com.adityapaul.wayfarer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.adityapaul.wayfarer.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val DisplayFont = GoogleFont("Bricolage Grotesque")
val DisplayFontFamily = FontFamily(
    Font(resId = R.font.bricolage_grotesque, weight = FontWeight.Bold),
    Font(resId = R.font.bricolage_grotesque, weight = FontWeight.ExtraBold),
    Font(resId = R.font.bricolage_grotesque, weight = FontWeight.Black),
    Font(googleFont = DisplayFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = DisplayFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = DisplayFont, fontProvider = fontProvider, weight = FontWeight.Black)
)

val BodyFont = GoogleFont("Inter")
val BodyFontFamily = FontFamily(
    Font(resId = R.font.inter, weight = FontWeight.Normal),
    Font(resId = R.font.inter, weight = FontWeight.Medium),
    Font(resId = R.font.inter, weight = FontWeight.SemiBold),
    Font(resId = R.font.inter, weight = FontWeight.Bold),
    Font(googleFont = BodyFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = BodyFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = BodyFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = BodyFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val MonoFont = GoogleFont("JetBrains Mono")
val MonoFontFamily = FontFamily(
    Font(resId = R.font.jetbrains_mono, weight = FontWeight.Medium),
    Font(resId = R.font.jetbrains_mono, weight = FontWeight.Bold),
    Font(resId = R.font.jetbrains_mono, weight = FontWeight.ExtraBold),
    Font(googleFont = MonoFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = MonoFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = MonoFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
)

// Helper Telemetry TextStyles for tabular numeric readouts
val TelemetryLarge = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 44.sp,
    lineHeight = 50.sp,
    letterSpacing = (-1.5).sp,
    fontFeatureSettings = "tnum"
)

val TelemetryMedium = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

val TelemetrySmall = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
    fontFeatureSettings = "tnum"
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 58.sp,
        letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.75).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.75).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = "tnum"
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = "tnum"
    )
)

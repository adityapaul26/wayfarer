package com.adityapaul.wayfarer.data.settings

import android.content.Context
import androidx.core.content.edit
import com.adityapaul.wayfarer.ui.map.MapStyleType
import com.adityapaul.wayfarer.ui.theme.AppColorPalette
import com.adityapaul.wayfarer.ui.theme.AppThemeMode

enum class SpeedUnit(val displayName: String, val label: String, val maxSpeed: Float) {
    KMH("Metric (km/h)", "KM/H", 120f),
    MPH("Imperial (mph)", "MPH", 75f)
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.LAVENDER_PURPLE,
    val useDynamicColor: Boolean = false,
    val locationUpdateIntervalSeconds: Int = 3,
    val mapStyleType: MapStyleType = MapStyleType.LIGHT,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val showSpeedometerInFreeDrive: Boolean = true
)

interface UserPreferencesRepository {
    fun getUserPreferences(): UserPreferences
    fun saveThemeMode(themeMode: AppThemeMode)
    fun saveColorPalette(colorPalette: AppColorPalette)
    fun saveUseDynamicColor(useDynamicColor: Boolean)
    fun saveLocationUpdateIntervalSeconds(intervalSeconds: Int)
    fun saveMapStyleType(mapStyleType: MapStyleType)
    fun saveSpeedUnit(speedUnit: SpeedUnit)
    fun saveShowSpeedometerInFreeDrive(show: Boolean)
}

class SharedPreferencesUserPreferencesRepository(
    context: Context
) : UserPreferencesRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getUserPreferences(): UserPreferences {
        val themeModeName = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val themeMode = try {
            AppThemeMode.valueOf(themeModeName ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val paletteName = prefs.getString(KEY_COLOR_PALETTE, AppColorPalette.LAVENDER_PURPLE.name)
        val colorPalette = try {
            AppColorPalette.valueOf(paletteName ?: AppColorPalette.LAVENDER_PURPLE.name)
        } catch (_: Exception) {
            AppColorPalette.LAVENDER_PURPLE
        }

        val useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val locationInterval = prefs.getInt(KEY_LOCATION_INTERVAL, 3).coerceIn(1, 8)

        val styleName = prefs.getString(KEY_MAP_STYLE, MapStyleType.LIGHT.name)
        val mapStyleType = try {
            MapStyleType.valueOf(styleName ?: MapStyleType.LIGHT.name)
        } catch (_: Exception) {
            MapStyleType.LIGHT
        }

        val unitName = prefs.getString(KEY_SPEED_UNIT, SpeedUnit.KMH.name)
        val speedUnit = try {
            SpeedUnit.valueOf(unitName ?: SpeedUnit.KMH.name)
        } catch (_: Exception) {
            SpeedUnit.KMH
        }

        val showSpeedometer = prefs.getBoolean(KEY_SHOW_SPEEDOMETER_FREE_DRIVE, true)

        return UserPreferences(
            themeMode = themeMode,
            colorPalette = colorPalette,
            useDynamicColor = useDynamicColor,
            locationUpdateIntervalSeconds = locationInterval,
            mapStyleType = mapStyleType,
            speedUnit = speedUnit,
            showSpeedometerInFreeDrive = showSpeedometer
        )
    }

    override fun saveThemeMode(themeMode: AppThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, themeMode.name) }
    }

    override fun saveColorPalette(colorPalette: AppColorPalette) {
        prefs.edit { putString(KEY_COLOR_PALETTE, colorPalette.name) }
    }

    override fun saveUseDynamicColor(useDynamicColor: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, useDynamicColor) }
    }

    override fun saveLocationUpdateIntervalSeconds(intervalSeconds: Int) {
        prefs.edit { putInt(KEY_LOCATION_INTERVAL, intervalSeconds.coerceIn(1, 8)) }
    }

    override fun saveMapStyleType(mapStyleType: MapStyleType) {
        prefs.edit { putString(KEY_MAP_STYLE, mapStyleType.name) }
    }

    override fun saveSpeedUnit(speedUnit: SpeedUnit) {
        prefs.edit { putString(KEY_SPEED_UNIT, speedUnit.name) }
    }

    override fun saveShowSpeedometerInFreeDrive(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_SPEEDOMETER_FREE_DRIVE, show) }
    }

    companion object {
        private const val PREFS_NAME = "wayfarer_user_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_PALETTE = "color_palette"
        private const val KEY_DYNAMIC_COLOR = "use_dynamic_color"
        private const val KEY_LOCATION_INTERVAL = "location_interval_seconds"
        private const val KEY_MAP_STYLE = "map_style_type"
        private const val KEY_SPEED_UNIT = "speed_unit"
        private const val KEY_SHOW_SPEEDOMETER_FREE_DRIVE = "show_speedometer_free_drive"
    }
}

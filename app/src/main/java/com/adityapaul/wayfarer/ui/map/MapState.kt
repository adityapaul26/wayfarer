package com.adityapaul.wayfarer.ui.map

import android.location.Location
import com.adityapaul.wayfarer.data.route.RouteResult
import com.adityapaul.wayfarer.data.search.SearchResult
import com.adityapaul.wayfarer.ui.theme.AppColorPalette
import com.adityapaul.wayfarer.ui.theme.AppThemeMode

enum class MapStyleType(val displayName: String, val url: String) {
    LIGHT("Light", "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"),
    DARK("Dark", "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"),
    LIBRE("Libre", "https://demotiles.maplibre.org/style.json")
}

enum class RouteSelectionStep {
    NONE,           // Normal map mode
    SELECT_ORIGIN,  // Selecting starting location
    SELECT_DEST     // Selecting destination location
}

data class RouteState(
    val step: RouteSelectionStep = RouteSelectionStep.NONE,
    val origin: SearchResult? = null,
    val destination: SearchResult? = null,
    val isCalculatingRoute: Boolean = false,
    val activeRoute: RouteResult? = null,
    val routeError: String? = null
)

data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val isExpanded: Boolean = false
)

data class MapState(
    val latitude: Double = 22.5726,
    val longitude: Double = 88.3639,
    val zoom: Double = 12.0,
    val styleType: MapStyleType = MapStyleType.LIGHT,
    val attributionText: String = "© OpenStreetMap contributors, © CARTO",
    val userLocation: Location? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val userMessage: String? = null,
    val searchState: SearchState = SearchState(),
    val routeState: RouteState = RouteState(),
    val selectedPinLocation: SearchResult? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.LAVENDER_PURPLE,
    val isSettingsOpen: Boolean = false
)

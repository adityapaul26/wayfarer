package com.adityapaul.wayfarer.ui.map

import android.location.Location
import com.adityapaul.wayfarer.data.search.SearchResult

enum class MapStyleType(val displayName: String, val url: String) {
    LIGHT("Light", "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"),
    DARK("Dark", "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"),
    LIBRE("Libre", "https://demotiles.maplibre.org/style.json")
}

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
    val selectedPinLocation: SearchResult? = null
)

package com.adityapaul.wayfarer.ui.map

enum class MapStyleType(val displayName: String, val url: String) {
    LIGHT("Light", "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"),
    DARK("Dark", "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"),
    LIBRE("Libre", "https://demotiles.maplibre.org/style.json")
}

data class MapState(
    val latitude: Double = 22.5726,
    val longitude: Double = 88.3639,
    val zoom: Double = 12.0,
    val styleType: MapStyleType = MapStyleType.LIGHT,
    val attributionText: String = "© OpenStreetMap contributors, © CARTO"
)

package com.adityapaul.wayfarer.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    fun zoomIn() {
        _uiState.update { currentState ->
            val newZoom = (currentState.zoom + 1.0).coerceAtMost(20.0)
            currentState.copy(zoom = newZoom)
        }
    }

    fun zoomOut() {
        _uiState.update { currentState ->
            val newZoom = (currentState.zoom - 1.0).coerceAtLeast(1.0)
            currentState.copy(zoom = newZoom)
        }
    }

    fun recenterToKolkata() {
        _uiState.update { currentState ->
            currentState.copy(
                latitude = 22.5726,
                longitude = 88.3639,
                zoom = 12.0
            )
        }
    }

    fun setStyleType(styleType: MapStyleType) {
        _uiState.update { currentState ->
            currentState.copy(styleType = styleType)
        }
    }

    fun toggleStyle() {
        _uiState.update { currentState ->
            val nextStyle = when (currentState.styleType) {
                MapStyleType.LIGHT -> MapStyleType.DARK
                MapStyleType.DARK -> MapStyleType.LIBRE
                MapStyleType.LIBRE -> MapStyleType.LIGHT
            }
            currentState.copy(styleType = nextStyle)
        }
    }

    fun onCameraMoved(latitude: Double, longitude: Double, zoom: Double) {
        _uiState.update { currentState ->
            currentState.copy(
                latitude = latitude,
                longitude = longitude,
                zoom = zoom
            )
        }
    }
}

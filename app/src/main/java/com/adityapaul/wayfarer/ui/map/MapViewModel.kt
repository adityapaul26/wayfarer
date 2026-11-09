package com.adityapaul.wayfarer.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adityapaul.wayfarer.data.location.DefaultLocationClient
import com.adityapaul.wayfarer.data.location.LocationClient
import com.adityapaul.wayfarer.data.location.hasLocationPermission
import com.adityapaul.wayfarer.data.search.GeocodingRepository
import com.adityapaul.wayfarer.data.search.NominatimGeocodingRepository
import com.adityapaul.wayfarer.data.search.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val geocodingRepository: GeocodingRepository = NominatimGeocodingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    private var locationClient: LocationClient? = null
    private var locationJob: Job? = null
    private var searchJob: Job? = null

    fun checkAndStartLocation(context: Context) {
        val hasPermission = hasLocationPermission(context)
        _uiState.update { it.copy(isLocationPermissionGranted = hasPermission) }

        if (hasPermission) {
            startLocationUpdates(context)
        }
    }

    fun onPermissionResult(isGranted: Boolean, context: Context) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
        if (isGranted) {
            startLocationUpdates(context)
            _uiState.update { it.copy(userMessage = "Location permission granted") }
        } else {
            stopLocationUpdates()
            _uiState.update { it.copy(userMessage = "Location permission denied") }
        }
    }

    fun startLocationUpdates(context: Context) {
        val client = locationClient ?: DefaultLocationClient(context.applicationContext).also {
            locationClient = it
        }

        val isGpsEnabled = client.isGpsEnabled()
        _uiState.update { it.copy(isGpsEnabled = isGpsEnabled) }

        if (!isGpsEnabled) {
            _uiState.update { it.copy(userMessage = "GPS is disabled on device") }
        }

        locationJob?.cancel()
        locationJob = client.getLocationUpdates(intervalMs = 3000L)
            .catch { exception ->
                _uiState.update { it.copy(userMessage = exception.message ?: "Location error") }
            }
            .onEach { location ->
                _uiState.update { currentState ->
                    currentState.copy(
                        userLocation = location,
                        isGpsEnabled = true
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    fun recenterToUserLocation(): Boolean {
        val userLoc = _uiState.value.userLocation
        return if (userLoc != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    latitude = userLoc.latitude,
                    longitude = userLoc.longitude,
                    zoom = 15.0
                )
            }
            true
        } else {
            val isGps = _uiState.value.isGpsEnabled
            val isPerm = _uiState.value.isLocationPermissionGranted
            val msg = when {
                !isPerm -> "Location permission required"
                !isGps -> "GPS is disabled"
                else -> "Acquiring current location..."
            }
            _uiState.update { it.copy(userMessage = msg) }
            false
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

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchState = currentState.searchState.copy(
                    query = newQuery,
                    errorMessage = null
                )
            )
        }
    }

    fun submitSearch() {
        val query = _uiState.value.searchState.query.trim()
        if (query.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    searchState = currentState.searchState.copy(
                        errorMessage = "Please enter a location or place to search.",
                        hasSearched = true,
                        isExpanded = true,
                        results = emptyList()
                    )
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                searchState = currentState.searchState.copy(
                    isSearching = true,
                    errorMessage = null,
                    hasSearched = true,
                    isExpanded = true
                )
            )
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = geocodingRepository.searchPlaces(query)
            result.onSuccess { places ->
                _uiState.update { currentState ->
                    currentState.copy(
                        searchState = currentState.searchState.copy(
                            isSearching = false,
                            results = places,
                            errorMessage = if (places.isEmpty()) "No matching places found for \"$query\"." else null
                        )
                    )
                }
            }.onFailure { exception ->
                _uiState.update { currentState ->
                    currentState.copy(
                        searchState = currentState.searchState.copy(
                            isSearching = false,
                            results = emptyList(),
                            errorMessage = exception.message ?: "Network error. Failed to fetch search results."
                        )
                    )
                }
            }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        _uiState.update { currentState ->
            currentState.copy(
                latitude = result.latitude,
                longitude = result.longitude,
                zoom = 15.0,
                selectedPinLocation = result,
                searchState = currentState.searchState.copy(
                    isExpanded = false
                )
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                searchState = SearchState()
            )
        }
    }

    fun clearSelectedPin() {
        _uiState.update { currentState ->
            currentState.copy(selectedPinLocation = null)
        }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        searchJob?.cancel()
    }
}

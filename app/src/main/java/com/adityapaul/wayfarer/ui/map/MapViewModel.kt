package com.adityapaul.wayfarer.ui.map

import android.content.Context
import android.location.Location
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adityapaul.wayfarer.data.location.DefaultLocationClient
import com.adityapaul.wayfarer.data.location.LocationClient
import com.adityapaul.wayfarer.data.location.hasLocationPermission
import com.adityapaul.wayfarer.data.route.NavigationUtils
import com.adityapaul.wayfarer.data.route.OsrmRouteRepository
import com.adityapaul.wayfarer.data.route.RouteRepository
import com.adityapaul.wayfarer.data.route.TransportMode
import com.adityapaul.wayfarer.data.search.GeocodingRepository
import com.adityapaul.wayfarer.data.search.NominatimGeocodingRepository
import com.adityapaul.wayfarer.data.search.RecentSearchRepository
import com.adityapaul.wayfarer.data.search.SearchResult
import com.adityapaul.wayfarer.data.search.SharedPreferencesRecentSearchRepository
import com.adityapaul.wayfarer.data.settings.SharedPreferencesUserPreferencesRepository
import com.adityapaul.wayfarer.data.settings.SpeedUnit
import com.adityapaul.wayfarer.data.settings.UserPreferencesRepository
import com.adityapaul.wayfarer.ui.theme.AppColorPalette
import com.adityapaul.wayfarer.ui.theme.AppThemeMode
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapViewModel(
    private val geocodingRepository: GeocodingRepository = NominatimGeocodingRepository(),
    private val routeRepository: RouteRepository = OsrmRouteRepository(),
    private var userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    private var locationClient: LocationClient? = null
    private var appContext: Context? = null
    private var locationJob: Job? = null
    private var searchJob: Job? = null
    private var routeJob: Job? = null

    fun initUserPreferences(context: Context, isSystemDark: Boolean = false) {
        if (userPreferencesRepository == null) {
            val repo = SharedPreferencesUserPreferencesRepository(context.applicationContext)
            userPreferencesRepository = repo
        }
        userPreferencesRepository?.let { repo ->
            val prefs = repo.getUserPreferences()
            val initialStyle = when (prefs.themeMode) {
                AppThemeMode.DARK -> MapStyleType.DARK
                AppThemeMode.LIGHT -> MapStyleType.LIGHT
                AppThemeMode.SYSTEM -> if (isSystemDark) MapStyleType.DARK else prefs.mapStyleType
            }
            _uiState.update { currentState ->
                currentState.copy(
                    themeMode = prefs.themeMode,
                    colorPalette = prefs.colorPalette,
                    useDynamicColor = prefs.useDynamicColor,
                    locationUpdateIntervalSeconds = prefs.locationUpdateIntervalSeconds,
                    styleType = initialStyle,
                    speedUnit = prefs.speedUnit,
                    showSpeedometerInFreeDrive = prefs.showSpeedometerInFreeDrive
                )
            }
        }
    }

    private var lastLocation: Location? = null
    private var smoothedSpeedMps: Float = 0f

    private fun processSpeedTelemetry(
        location: Location,
        currentState: MapState
    ): Triple<Float, String, Float> {
        val rawSpeed = if (location.hasSpeed() && location.speed > 0f) {
            location.speed
        } else {
            NavigationUtils.calculateSpeedMps(lastLocation, location)
        }

        val accuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {
            location.speedAccuracyMetersPerSecond
        } else 0f

        val filteredSpeed = if (rawSpeed < 0.35f || (accuracy > 0f && accuracy > rawSpeed * 1.5f)) {
            0f
        } else {
            rawSpeed
        }

        val alpha = 0.35f
        smoothedSpeedMps = smoothedSpeedMps * (1f - alpha) + filteredSpeed * alpha
        if (smoothedSpeedMps < 0.05f) {
            smoothedSpeedMps = 0f
        }

        val speedUnit = currentState.speedUnit
        val speedInUnit = when (speedUnit) {
            SpeedUnit.KMH -> smoothedSpeedMps * 3.6f
            SpeedUnit.MPH -> smoothedSpeedMps * 2.23694f
        }

        val formattedSpeed = String.format(Locale.US, "%.0f", speedInUnit)
        val progress = (speedInUnit / speedUnit.maxSpeed).coerceIn(0f, 1f)

        lastLocation = location

        return Triple(smoothedSpeedMps, formattedSpeed, progress)
    }

    fun setSpeedUnit(speedUnit: SpeedUnit) {
        userPreferencesRepository?.saveSpeedUnit(speedUnit)
        _uiState.update { currentState ->
            val mps = currentState.currentSpeedMps
            val speedInUnit = when (speedUnit) {
                SpeedUnit.KMH -> mps * 3.6f
                SpeedUnit.MPH -> mps * 2.23694f
            }
            val formatted = String.format(Locale.US, "%.0f", speedInUnit)
            val progress = (speedInUnit / speedUnit.maxSpeed).coerceIn(0f, 1f)

            currentState.copy(
                speedUnit = speedUnit,
                currentSpeedFormatted = formatted,
                speedProgress = progress
            )
        }
    }

    fun setShowSpeedometerInFreeDrive(show: Boolean) {
        userPreferencesRepository?.saveShowSpeedometerInFreeDrive(show)
        _uiState.update { it.copy(showSpeedometerInFreeDrive = show) }
    }

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
        appContext = context.applicationContext
        val client = locationClient ?: DefaultLocationClient(context.applicationContext).also {
            locationClient = it
        }

        val isGpsEnabled = client.isGpsEnabled()
        _uiState.update { it.copy(isGpsEnabled = isGpsEnabled) }

        if (!isGpsEnabled) {
            _uiState.update { it.copy(userMessage = "GPS is disabled on device") }
        }

        val intervalMs = _uiState.value.locationUpdateIntervalSeconds * 1000L
        locationJob?.cancel()
        locationJob = client.getLocationUpdates(intervalMs = intervalMs)
            .catch { exception ->
                _uiState.update { it.copy(userMessage = exception.message ?: "Location error") }
            }
            .onEach { location ->
                _uiState.update { currentState ->
                    val (mps, formatted, progress) = processSpeedTelemetry(location, currentState)
                    val updatedNavState = processNavigationProgress(location, currentState)
                    currentState.copy(
                        userLocation = location,
                        currentSpeedMps = mps,
                        currentSpeedFormatted = formatted,
                        speedProgress = progress,
                        isGpsEnabled = true,
                        routeState = currentState.routeState.copy(
                            navigationState = updatedNavState
                        )
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
        userPreferencesRepository?.saveMapStyleType(styleType)
        _uiState.update { currentState ->
            currentState.copy(styleType = styleType)
        }
    }

    fun toggleStyle() {
        val nextStyle = when (_uiState.value.styleType) {
            MapStyleType.LIGHT -> MapStyleType.DARK
            MapStyleType.DARK -> MapStyleType.LIBRE
            MapStyleType.LIBRE -> MapStyleType.LIGHT
        }
        userPreferencesRepository?.saveMapStyleType(nextStyle)
        _uiState.update { currentState ->
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

    private fun addGeekLog(tag: GeekLogType, title: String, details: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val entry = GeekLogEntry(timestamp = timestamp, tag = tag, title = title, details = details)
        _uiState.update { currentState ->
            val updatedLogs = (listOf(entry) + currentState.geekLogs).take(30)
            currentState.copy(geekLogs = updatedLogs)
        }
    }

    fun openGeekLogs() {
        _uiState.update { currentState ->
            if (currentState.geekLogs.isEmpty()) {
                val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
                val initialLog = GeekLogEntry(
                    timestamp = timestamp,
                    tag = GeekLogType.SYS,
                    title = "LOGS FOR GEEKS INITIALIZED",
                    details = "Developer Telemetry Log Window. Captures HTTP REST requests, GPS satellite metrics, and OSRM geometry calculation parameters."
                )
                currentState.copy(
                    isGeekLogsOpen = true,
                    geekLogs = listOf(initialLog)
                )
            } else {
                currentState.copy(isGeekLogsOpen = true)
            }
        }
    }

    fun closeGeekLogs() {
        _uiState.update { currentState ->
            currentState.copy(isGeekLogsOpen = false)
        }
    }

    fun clearGeekLogs() {
        _uiState.update { currentState ->
            currentState.copy(geekLogs = emptyList())
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

        val encodedQuery = try { URLEncoder.encode(query, "UTF-8") } catch (e: Exception) { query }
        addGeekLog(
            GeekLogType.NET,
            "HTTP GET nominatim.openstreetmap.org/search",
            "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=10"
        )

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
                addGeekLog(
                    GeekLogType.NET,
                    "Nominatim Response 200 OK",
                    "Parsed ${places.size} places for query \"$query\""
                )
                if (places.isNotEmpty()) {
                    saveRecentSearch(places.first())
                }
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
                addGeekLog(
                    GeekLogType.NET,
                    "Nominatim Network Error",
                    exception.message ?: "Failed to fetch place search"
                )
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

    private var recentSearchRepository: RecentSearchRepository? = null

    fun initRecentSearchRepository(context: Context) {
        if (recentSearchRepository == null) {
            val repo = SharedPreferencesRecentSearchRepository(context.applicationContext)
            recentSearchRepository = repo
            val list = repo.getRecentSearches()
            _uiState.update { currentState ->
                currentState.copy(
                    searchState = currentState.searchState.copy(
                        recentSearches = list
                    )
                )
            }
        }
    }

    fun saveRecentSearch(result: SearchResult) {
        recentSearchRepository?.let { repo ->
            val updatedList = repo.addRecentSearch(result)
            _uiState.update { currentState ->
                currentState.copy(
                    searchState = currentState.searchState.copy(
                        recentSearches = updatedList
                    )
                )
            }
        }
    }

    fun removeRecentSearch(searchResultId: Long) {
        recentSearchRepository?.let { repo ->
            val updatedList = repo.removeRecentSearch(searchResultId)
            _uiState.update { currentState ->
                currentState.copy(
                    searchState = currentState.searchState.copy(
                        recentSearches = updatedList
                    )
                )
            }
        }
    }

    fun clearRecentSearches() {
        recentSearchRepository?.let { repo ->
            val updatedList = repo.clearRecentSearches()
            _uiState.update { currentState ->
                currentState.copy(
                    searchState = currentState.searchState.copy(
                        recentSearches = updatedList
                    )
                )
            }
        }
    }

    fun expandSearchPanel() {
        _uiState.update { currentState ->
            currentState.copy(
                searchState = currentState.searchState.copy(
                    isExpanded = true
                )
            )
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        saveRecentSearch(result)
        val currentRouteStep = _uiState.value.routeState.step
        when (currentRouteStep) {
            RouteSelectionStep.SELECT_ORIGIN -> {
                selectOrigin(result)
            }
            RouteSelectionStep.SELECT_DEST -> {
                selectDestination(result)
            }
            RouteSelectionStep.NONE -> {
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
        }
    }

    private fun resetSearchQueryState(currentState: MapState): SearchState {
        return currentState.searchState.copy(
            query = "",
            isSearching = false,
            results = emptyList(),
            errorMessage = null,
            hasSearched = false,
            isExpanded = false
        )
    }

    fun startRoutePlanning() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedPinLocation = null,
                searchState = resetSearchQueryState(currentState).copy(isExpanded = true),
                routeState = RouteState(step = RouteSelectionStep.SELECT_ORIGIN)
            )
        }
    }

    fun selectOrigin(origin: SearchResult) {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    origin = origin,
                    step = RouteSelectionStep.SELECT_DEST
                ),
                searchState = resetSearchQueryState(currentState).copy(isExpanded = true)
            )
        }
    }

    fun selectOriginFromCurrentLocation() {
        val loc = _uiState.value.userLocation
        if (loc != null) {
            val currentLocResult = SearchResult(
                id = -100L,
                name = "My Location",
                address = "${String.format(Locale.US, "%.4f", loc.latitude)}, ${String.format(Locale.US, "%.4f", loc.longitude)}",
                category = "Current Location",
                latitude = loc.latitude,
                longitude = loc.longitude
            )
            selectOrigin(currentLocResult)
        } else {
            _uiState.update { it.copy(userMessage = "Current GPS location not available") }
        }
    }

    fun clearOrigin() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    origin = null,
                    step = RouteSelectionStep.SELECT_ORIGIN,
                    activeRoute = null
                ),
                searchState = resetSearchQueryState(currentState).copy(isExpanded = true)
            )
        }
    }

    fun clearDestination() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    destination = null,
                    step = RouteSelectionStep.SELECT_DEST,
                    activeRoute = null
                ),
                searchState = resetSearchQueryState(currentState).copy(isExpanded = true)
            )
        }
    }

    fun swapOriginAndDestination() {
        val currentOrigin = _uiState.value.routeState.origin
        val currentDest = _uiState.value.routeState.destination

        if (currentOrigin != null || currentDest != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    routeState = currentState.routeState.copy(
                        origin = currentDest,
                        destination = currentOrigin
                    )
                )
            }

            if (currentOrigin != null && currentDest != null) {
                calculateRoute(currentDest, currentOrigin)
            }
        }
    }

    fun selectDestination(destination: SearchResult) {
        val origin = _uiState.value.routeState.origin
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    destination = destination,
                    step = RouteSelectionStep.NONE
                ),
                searchState = resetSearchQueryState(currentState)
            )
        }

        if (origin != null) {
            calculateRoute(origin, destination)
        }
    }

    fun setTransportMode(mode: TransportMode) {
        if (_uiState.value.routeState.transportMode == mode && _uiState.value.routeState.activeRoute != null) return

        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    transportMode = mode
                )
            )
        }

        val origin = _uiState.value.routeState.origin
        val destination = _uiState.value.routeState.destination
        if (origin != null && destination != null) {
            calculateRoute(origin, destination)
        }
    }

    private fun calculateRoute(origin: SearchResult, destination: SearchResult) {
        val mode = _uiState.value.routeState.transportMode
        val param = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f", origin.longitude, origin.latitude, destination.longitude, destination.latitude)
        addGeekLog(
            GeekLogType.NET,
            "HTTP GET router.project-osrm.org/route/v1/${mode.osrmProfile}",
            "https://router.project-osrm.org/route/v1/${mode.osrmProfile}/$param?overview=full&geometries=geojson"
        )

        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    isCalculatingRoute = true,
                    routeError = null,
                    activeRoute = null
                )
            )
        }

        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            val result = routeRepository.calculateRoute(
                originName = origin.name,
                originLat = origin.latitude,
                originLng = origin.longitude,
                destinationName = destination.name,
                destLat = destination.latitude,
                destLng = destination.longitude,
                transportMode = mode
            )

            result.onSuccess { routeResult ->
                addGeekLog(
                    GeekLogType.ROUTE,
                    "OSRM Route Calculated",
                    "Parsed ${routeResult.points.size} GeoJSON LineString coordinates • Distance: ${routeResult.formattedDistance()} • Duration: ${routeResult.formattedDuration()}"
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        routeState = currentState.routeState.copy(
                            isCalculatingRoute = false,
                            activeRoute = routeResult,
                            routeError = null
                        )
                    )
                }
            }.onFailure { exception ->
                addGeekLog(
                    GeekLogType.NET,
                    "OSRM Route HTTP Error",
                    exception.message ?: "Failed to calculate OSRM route"
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        routeState = currentState.routeState.copy(
                            isCalculatingRoute = false,
                            activeRoute = null,
                            routeError = exception.message ?: "Failed to calculate route."
                        ),
                        userMessage = exception.message ?: "Failed to calculate route."
                    )
                }
            }
        }
    }

    fun cancelRoutePlanning() {
        routeJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                routeState = RouteState(),
                searchState = resetSearchQueryState(currentState)
            )
        }
    }

    private var offRouteCount = 0

    private fun processNavigationProgress(location: Location, currentState: MapState): LiveNavigationState {
        val navState = currentState.routeState.navigationState
        if (!navState.isNavigating) return navState
        val activeRoute = currentState.routeState.activeRoute ?: return navState

        val userLatLng = LatLng(location.latitude, location.longitude)
        val snapResult = NavigationUtils.snapToPolyline(userLatLng, activeRoute.points) ?: return navState

        if (snapResult.distanceToPolylineMeters > 35.0f) {
            offRouteCount++
            if (offRouteCount >= 2) {
                offRouteCount = 0
                val currentDest = currentState.routeState.destination
                if (currentDest != null) {
                    val currentLocSearchResult = SearchResult(
                        id = -100L,
                        name = "Current Location",
                        address = "${location.latitude}, ${location.longitude}",
                        category = "GPS",
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    calculateRoute(currentLocSearchResult, currentDest)
                }
                return navState.copy(isOffRoute = true)
            }
        } else {
            offRouteCount = 0
        }

        val (traveled, remaining) = NavigationUtils.splitPolyline(snapResult, activeRoute.points)
        val remainingDist = NavigationUtils.calculatePathDistance(remaining)

        val totalDist = activeRoute.distanceMeters.coerceAtLeast(1.0)
        val distRatio = (remainingDist / totalDist).coerceIn(0.0, 1.0)
        val remainingDur = activeRoute.durationSeconds * distRatio

        return navState.copy(
            snappedLocation = snapResult.snappedPoint,
            traveledPoints = traveled,
            remainingPoints = remaining,
            remainingDistanceMeters = remainingDist,
            remainingDurationSeconds = remainingDur,
            isOffRoute = false
        )
    }

    fun startLiveNavigation() {
        val activeRoute = _uiState.value.routeState.activeRoute ?: return
        val userLoc = _uiState.value.userLocation

        val initialSnap = if (userLoc != null) {
            LatLng(userLoc.latitude, userLoc.longitude)
        } else {
            activeRoute.points.firstOrNull()
        }

        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    navigationState = LiveNavigationState(
                        isNavigating = true,
                        snappedLocation = initialSnap,
                        remainingPoints = activeRoute.points,
                        traveledPoints = emptyList(),
                        remainingDistanceMeters = activeRoute.distanceMeters,
                        remainingDurationSeconds = activeRoute.durationSeconds
                    )
                )
            )
        }
    }

    fun stopLiveNavigation() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    navigationState = LiveNavigationState()
                )
            )
        }
    }

    fun clearRoute() {
        routeJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                routeState = RouteState()
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                searchState = resetSearchQueryState(currentState)
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

    fun toggleSettings() {
        _uiState.update { currentState ->
            currentState.copy(isSettingsOpen = !currentState.isSettingsOpen)
        }
    }

    fun openSettings() {
        _uiState.update { currentState ->
            currentState.copy(isSettingsOpen = true)
        }
    }

    fun closeSettings() {
        _uiState.update { currentState ->
            currentState.copy(isSettingsOpen = false)
        }
    }

    fun setThemeMode(themeMode: AppThemeMode, isSystemDark: Boolean = false) {
        userPreferencesRepository?.saveThemeMode(themeMode)
        _uiState.update { currentState ->
            val correspondingStyle = when (themeMode) {
                AppThemeMode.DARK -> MapStyleType.DARK
                AppThemeMode.LIGHT -> MapStyleType.LIGHT
                AppThemeMode.SYSTEM -> if (isSystemDark) MapStyleType.DARK else MapStyleType.LIGHT
            }
            userPreferencesRepository?.saveMapStyleType(correspondingStyle)
            currentState.copy(
                themeMode = themeMode,
                styleType = correspondingStyle
            )
        }
    }

    fun setColorPalette(palette: AppColorPalette) {
        userPreferencesRepository?.saveColorPalette(palette)
        _uiState.update { currentState ->
            currentState.copy(colorPalette = palette)
        }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        userPreferencesRepository?.saveUseDynamicColor(enabled)
        _uiState.update { currentState ->
            currentState.copy(useDynamicColor = enabled)
        }
    }

    fun setLocationUpdateInterval(seconds: Int) {
        val clampedSeconds = seconds.coerceIn(1, 8)
        userPreferencesRepository?.saveLocationUpdateIntervalSeconds(clampedSeconds)
        _uiState.update { currentState ->
            currentState.copy(locationUpdateIntervalSeconds = clampedSeconds)
        }

        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val logEntry = GeekLogEntry(
            timestamp = timestamp,
            tag = GeekLogType.GPS,
            title = "GPS UPDATE INTERVAL SET TO ${clampedSeconds}s",
            details = "Location client update interval changed to ${clampedSeconds * 1000} ms"
        )
        _uiState.update { it.copy(geekLogs = (listOf(logEntry) + it.geekLogs).take(30)) }

        appContext?.let { context ->
            if (_uiState.value.isLocationPermissionGranted) {
                startLocationUpdates(context)
            }
        }
    }

    fun toggleBottomSheetCollapse() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    isCollapsed = !currentState.routeState.isCollapsed
                )
            )
        }
    }

    fun expandBottomSheet() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    isCollapsed = false
                )
            )
        }
    }

    fun collapseBottomSheet() {
        _uiState.update { currentState ->
            currentState.copy(
                routeState = currentState.routeState.copy(
                    isCollapsed = true
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        searchJob?.cancel()
        routeJob?.cancel()
    }
}

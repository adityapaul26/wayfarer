package com.adityapaul.wayfarer

import com.adityapaul.wayfarer.data.route.RouteRepository
import com.adityapaul.wayfarer.data.route.RouteResult
import com.adityapaul.wayfarer.data.search.GeocodingRepository
import com.adityapaul.wayfarer.data.search.SearchResult
import com.adityapaul.wayfarer.ui.map.MapStyleType
import com.adityapaul.wayfarer.ui.map.MapViewModel
import com.adityapaul.wayfarer.ui.map.RouteSelectionStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeGeocodingRepository: FakeGeocodingRepository
    private lateinit var fakeRouteRepository: FakeRouteRepository
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGeocodingRepository = FakeGeocodingRepository()
        fakeRouteRepository = FakeRouteRepository()
        viewModel = MapViewModel(
            geocodingRepository = fakeGeocodingRepository,
            routeRepository = fakeRouteRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialCoordinates_pointToKolkata() {
        val state = viewModel.uiState.value
        assertEquals(22.5726, state.latitude, 0.0001)
        assertEquals(88.3639, state.longitude, 0.0001)
        assertEquals(12.0, state.zoom, 0.0001)
        assertEquals(MapStyleType.LIGHT, state.styleType)
        assertNull(state.userLocation)
        assertNull(state.selectedPinLocation)
        assertEquals(RouteSelectionStep.NONE, state.routeState.step)
        assertNull(state.routeState.activeRoute)
    }

    @Test
    fun startRoutePlanning_transitionsToSelectOrigin() {
        viewModel.startRoutePlanning()
        val routeState = viewModel.uiState.value.routeState
        assertEquals(RouteSelectionStep.SELECT_ORIGIN, routeState.step)
        assertNull(routeState.origin)
        assertNull(routeState.destination)
    }

    @Test
    fun selectOriginAndDestination_calculatesOsrmRoute() = runTest {
        val origin = SearchResult(
            id = 1L,
            name = "Howrah Bridge",
            address = "Howrah, WB",
            category = "Bridge",
            latitude = 22.5851,
            longitude = 88.3468
        )
        val destination = SearchResult(
            id = 2L,
            name = "Victoria Memorial",
            address = "Queen's Way, Kolkata",
            category = "Museum",
            latitude = 22.5448,
            longitude = 88.3425
        )

        val routePoints = listOf(
            LatLng(22.5851, 88.3468),
            LatLng(22.5600, 88.3500),
            LatLng(22.5448, 88.3425)
        )
        fakeRouteRepository.routeResult = Result.success(
            RouteResult(
                originName = "Howrah Bridge",
                destinationName = "Victoria Memorial",
                points = routePoints,
                distanceMeters = 5400.0,
                durationSeconds = 720.0
            )
        )

        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)

        val stateAfterOrigin = viewModel.uiState.value
        assertEquals(RouteSelectionStep.SELECT_DEST, stateAfterOrigin.routeState.step)
        assertEquals("Howrah Bridge", stateAfterOrigin.routeState.origin?.name)

        viewModel.selectDestination(destination)
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(RouteSelectionStep.NONE, finalState.routeState.step)
        assertFalse(finalState.routeState.isCalculatingRoute)

        val activeRoute = finalState.routeState.activeRoute
        assertNotNull(activeRoute)
        assertEquals("Howrah Bridge", activeRoute?.originName)
        assertEquals("Victoria Memorial", activeRoute?.destinationName)
        assertEquals(3, activeRoute?.points?.size)
        assertEquals("5.4 km", activeRoute?.formattedDistance())
        assertEquals("12 mins", activeRoute?.formattedDuration())
    }

    @Test
    fun swapOriginAndDestination_swapsOriginAndDest() = runTest {
        val origin = SearchResult(1L, "Howrah Bridge", "Howrah", "Bridge", 22.5851, 88.3468)
        val destination = SearchResult(2L, "Victoria Memorial", "Kolkata", "Museum", 22.5448, 88.3425)

        fakeRouteRepository.routeResult = Result.success(
            RouteResult("Victoria Memorial", "Howrah Bridge", listOf(LatLng(22.5448, 88.3425), LatLng(22.5851, 88.3468)), 5400.0, 720.0)
        )

        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)
        viewModel.selectDestination(destination)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.swapOriginAndDestination()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Victoria Memorial", state.routeState.origin?.name)
        assertEquals("Howrah Bridge", state.routeState.destination?.name)
    }

    @Test
    fun clearOrigin_clearsOriginAndResetsStep() {
        val origin = SearchResult(1L, "Origin", "O", null, 22.5, 88.3)
        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)

        viewModel.clearOrigin()

        val routeState = viewModel.uiState.value.routeState
        assertNull(routeState.origin)
        assertEquals(RouteSelectionStep.SELECT_ORIGIN, routeState.step)
    }

    @Test
    fun clearDestination_clearsDestinationAndResetsStep() {
        val origin = SearchResult(1L, "Origin", "O", null, 22.5, 88.3)
        val dest = SearchResult(2L, "Dest", "D", null, 22.6, 88.4)
        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)
        viewModel.selectDestination(dest)

        viewModel.clearDestination()

        val routeState = viewModel.uiState.value.routeState
        assertNull(routeState.destination)
        assertEquals(RouteSelectionStep.SELECT_DEST, routeState.step)
    }

    @Test
    fun calculateRoute_failure_setsRouteError() = runTest {
        val origin = SearchResult(
            id = 1L,
            name = "Origin",
            address = "Origin Address",
            category = null,
            latitude = 22.5851,
            longitude = 88.3468
        )
        val destination = SearchResult(
            id = 2L,
            name = "Dest",
            address = "Dest Address",
            category = null,
            latitude = 22.5448,
            longitude = 88.3425
        )

        fakeRouteRepository.routeResult = Result.failure(Exception("OSRM Route Service Timeout"))

        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)
        viewModel.selectDestination(destination)
        testDispatcher.scheduler.advanceUntilIdle()

        val routeState = viewModel.uiState.value.routeState
        assertFalse(routeState.isCalculatingRoute)
        assertNull(routeState.activeRoute)
        assertEquals("OSRM Route Service Timeout", routeState.routeError)
    }

    @Test
    fun clearRoute_resetsRouteState() = runTest {
        val origin = SearchResult(1L, "O", "O", null, 22.5, 88.3)
        val destination = SearchResult(2L, "D", "D", null, 22.6, 88.4)
        fakeRouteRepository.routeResult = Result.success(
            RouteResult("O", "D", listOf(LatLng(22.5, 88.3), LatLng(22.6, 88.4)), 1000.0, 600.0)
        )

        viewModel.startRoutePlanning()
        viewModel.selectOrigin(origin)
        viewModel.selectDestination(destination)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearRoute()

        val routeState = viewModel.uiState.value.routeState
        assertNull(routeState.activeRoute)
        assertEquals(RouteSelectionStep.NONE, routeState.step)
    }

    @Test
    fun submitSearch_emptyQuery_showsErrorMessage() {
        viewModel.onSearchQueryChange("   ")
        viewModel.submitSearch()

        val searchState = viewModel.uiState.value.searchState
        assertEquals("Please enter a location or place to search.", searchState.errorMessage)
        assertTrue(searchState.hasSearched)
    }

    @Test
    fun submitSearch_validQuery_updatesResults() = runTest {
        val samplePlaces = listOf(
            SearchResult(
                id = 101L,
                name = "Howrah Bridge",
                address = "Howrah, West Bengal, India",
                category = "Bridge",
                latitude = 22.5851,
                longitude = 88.3468
            )
        )
        fakeGeocodingRepository.searchResults = Result.success(samplePlaces)

        viewModel.onSearchQueryChange("Howrah Bridge")
        viewModel.submitSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val searchState = viewModel.uiState.value.searchState
        assertFalse(searchState.isSearching)
        assertEquals(1, searchState.results.size)
        assertEquals("Howrah Bridge", searchState.results.first().name)
        assertNull(searchState.errorMessage)
    }

    @Test
    fun zoomIn_increasesZoomLevel() {
        viewModel.zoomIn()
        assertEquals(13.0, viewModel.uiState.value.zoom, 0.0001)
    }

    @Test
    fun zoomOut_decreasesZoomLevel() {
        viewModel.zoomOut()
        assertEquals(11.0, viewModel.uiState.value.zoom, 0.0001)
    }

    @Test
    fun recenterToKolkata_resetsCameraPosition() {
        viewModel.onCameraMoved(10.0, 10.0, 5.0)
        viewModel.recenterToKolkata()

        val state = viewModel.uiState.value
        assertEquals(22.5726, state.latitude, 0.0001)
        assertEquals(88.3639, state.longitude, 0.0001)
        assertEquals(12.0, state.zoom, 0.0001)
    }

    @Test
    fun toggleStyle_cyclesMapStyles() {
        assertEquals(MapStyleType.LIGHT, viewModel.uiState.value.styleType)
        viewModel.toggleStyle()
        assertEquals(MapStyleType.DARK, viewModel.uiState.value.styleType)
        viewModel.toggleStyle()
        assertEquals(MapStyleType.LIBRE, viewModel.uiState.value.styleType)
        viewModel.toggleStyle()
        assertEquals(MapStyleType.LIGHT, viewModel.uiState.value.styleType)
    }
}

private class FakeGeocodingRepository : GeocodingRepository {
    var searchResults: Result<List<SearchResult>> = Result.success(emptyList())

    override suspend fun searchPlaces(query: String): Result<List<SearchResult>> {
        return searchResults
    }
}

private class FakeRouteRepository : RouteRepository {
    var routeResult: Result<RouteResult> = Result.failure(Exception("Not set"))

    override suspend fun calculateRoute(
        originName: String,
        originLat: Double,
        originLng: Double,
        destinationName: String,
        destLat: Double,
        destLng: Double
    ): Result<RouteResult> {
        return routeResult
    }
}

package com.adityapaul.wayfarer

import com.adityapaul.wayfarer.data.search.GeocodingRepository
import com.adityapaul.wayfarer.data.search.SearchResult
import com.adityapaul.wayfarer.ui.map.MapStyleType
import com.adityapaul.wayfarer.ui.map.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeGeocodingRepository: FakeGeocodingRepository
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGeocodingRepository = FakeGeocodingRepository()
        viewModel = MapViewModel(geocodingRepository = fakeGeocodingRepository)
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
    fun submitSearch_noMatchingPlaces_showsNoResultsMessage() = runTest {
        fakeGeocodingRepository.searchResults = Result.success(emptyList())

        viewModel.onSearchQueryChange("NonExistentPlaceXYZ")
        viewModel.submitSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val searchState = viewModel.uiState.value.searchState
        assertFalse(searchState.isSearching)
        assertTrue(searchState.results.isEmpty())
        assertEquals("No matching places found for \"NonExistentPlaceXYZ\".", searchState.errorMessage)
    }

    @Test
    fun submitSearch_networkFailure_showsNetworkErrorMessage() = runTest {
        fakeGeocodingRepository.searchResults = Result.failure(Exception("Network unreachable"))

        viewModel.onSearchQueryChange("Park Street")
        viewModel.submitSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val searchState = viewModel.uiState.value.searchState
        assertFalse(searchState.isSearching)
        assertTrue(searchState.results.isEmpty())
        assertEquals("Network unreachable", searchState.errorMessage)
    }

    @Test
    fun onSearchResultSelected_updatesCameraPositionAndPin() {
        val place = SearchResult(
            id = 202L,
            name = "Victoria Memorial",
            address = "Queen's Way, Kolkata",
            category = "Museum",
            latitude = 22.5448,
            longitude = 88.3425
        )

        viewModel.onSearchResultSelected(place)

        val state = viewModel.uiState.value
        assertEquals(22.5448, state.latitude, 0.0001)
        assertEquals(88.3425, state.longitude, 0.0001)
        assertEquals(15.0, state.zoom, 0.0001)
        assertEquals(place, state.selectedPinLocation)
        assertFalse(state.searchState.isExpanded)
    }

    @Test
    fun clearSelectedPin_removesPin() {
        val place = SearchResult(
            id = 202L,
            name = "Victoria Memorial",
            address = "Queen's Way, Kolkata",
            category = "Museum",
            latitude = 22.5448,
            longitude = 88.3425
        )
        viewModel.onSearchResultSelected(place)
        viewModel.clearSelectedPin()

        assertNull(viewModel.uiState.value.selectedPinLocation)
    }

    @Test
    fun recenterToUserLocation_withoutLocation_setsUserMessage() {
        val success = viewModel.recenterToUserLocation()
        assertFalse(success)
        assertEquals("Location permission required", viewModel.uiState.value.userMessage)
    }

    @Test
    fun dismissUserMessage_clearsMessage() {
        viewModel.recenterToUserLocation()
        viewModel.dismissUserMessage()
        assertNull(viewModel.uiState.value.userMessage)
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

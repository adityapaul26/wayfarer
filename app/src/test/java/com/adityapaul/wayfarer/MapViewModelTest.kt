package com.adityapaul.wayfarer

import com.adityapaul.wayfarer.ui.map.MapStyleType
import com.adityapaul.wayfarer.ui.map.MapViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapViewModelTest {

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        viewModel = MapViewModel()
    }

    @Test
    fun initialCoordinates_pointToKolkata() {
        val state = viewModel.uiState.value
        assertEquals(22.5726, state.latitude, 0.0001)
        assertEquals(88.3639, state.longitude, 0.0001)
        assertEquals(12.0, state.zoom, 0.0001)
        assertEquals(MapStyleType.LIGHT, state.styleType)
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

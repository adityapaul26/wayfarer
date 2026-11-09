package com.adityapaul.wayfarer.ui.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.math.abs

@Composable
fun MapViewContainer(
    mapState: MapState,
    onCameraMoved: (latitude: Double, longitude: Double, zoom: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentStyleUrl by remember { mutableStateOf<String?>(null) }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = {
            mapView.getMapAsync { map ->
                mapLibreMap = map

                // Configure MapLibre UI settings
                map.uiSettings.isAttributionEnabled = true
                map.uiSettings.isLogoEnabled = true

                // Initial camera position for Kolkata
                val initialCamera = CameraPosition.Builder()
                    .target(LatLng(mapState.latitude, mapState.longitude))
                    .zoom(mapState.zoom)
                    .build()
                map.cameraPosition = initialCamera

                // Set map style
                map.setStyle(Style.Builder().fromUri(mapState.styleType.url)) {
                    currentStyleUrl = mapState.styleType.url
                }

                map.addOnCameraIdleListener {
                    val target = map.cameraPosition.target
                    if (target != null) {
                        onCameraMoved(target.latitude, target.longitude, map.cameraPosition.zoom)
                    }
                }
            }
            mapView
        },
        modifier = modifier
    )

    // Handle map style changes
    LaunchedEffect(mapState.styleType.url) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (currentStyleUrl != mapState.styleType.url) {
            map.setStyle(Style.Builder().fromUri(mapState.styleType.url)) {
                currentStyleUrl = mapState.styleType.url
            }
        }
    }

    // Handle programmatic camera moves (zoom in, zoom out, recenter)
    LaunchedEffect(mapState.latitude, mapState.longitude, mapState.zoom) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val currentTarget = map.cameraPosition.target
        val currentZoom = map.cameraPosition.zoom

        val isPositionDiverged = currentTarget == null ||
                abs(currentTarget.latitude - mapState.latitude) > 0.0001 ||
                abs(currentTarget.longitude - mapState.longitude) > 0.0001 ||
                abs(currentZoom - mapState.zoom) > 0.1

        if (isPositionDiverged) {
            val updatedCamera = CameraPosition.Builder()
                .target(LatLng(mapState.latitude, mapState.longitude))
                .zoom(mapState.zoom)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(updatedCamera), 300)
        }
    }
}

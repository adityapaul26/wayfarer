package com.adityapaul.wayfarer.ui.map

import android.annotation.SuppressLint
import android.content.Context
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
import com.adityapaul.wayfarer.data.location.hasLocationPermission
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
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
    var searchMarker by remember { mutableStateOf<Marker?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

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

                // Initial camera position
                val initialCamera = CameraPosition.Builder()
                    .target(LatLng(mapState.latitude, mapState.longitude))
                    .zoom(mapState.zoom)
                    .build()
                map.cameraPosition = initialCamera

                // Set map style
                map.setStyle(Style.Builder().fromUri(mapState.styleType.url)) { style ->
                    currentStyleUrl = mapState.styleType.url
                    setupLocationComponent(map, style, context, mapState)
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
            map.setStyle(Style.Builder().fromUri(mapState.styleType.url)) { style ->
                currentStyleUrl = mapState.styleType.url
                setupLocationComponent(map, style, context, mapState)
            }
        }
    }

    // Handle user location updates on map
    LaunchedEffect(mapState.userLocation, mapState.isLocationPermissionGranted) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        val location = mapState.userLocation ?: return@LaunchedEffect

        if (hasLocationPermission(context)) {
            val locationComponent = map.locationComponent
            if (!locationComponent.isLocationComponentActivated) {
                setupLocationComponent(map, style, context, mapState)
            }
            if (locationComponent.isLocationComponentActivated) {
                locationComponent.forceLocationUpdate(location)
            }
        }
    }

    // Handle search result pin on map
    LaunchedEffect(mapState.selectedPinLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        searchMarker?.let { map.removeMarker(it) }
        searchMarker = null

        val pin = mapState.selectedPinLocation ?: return@LaunchedEffect
        val markerOptions = MarkerOptions()
            .position(LatLng(pin.latitude, pin.longitude))
            .title(pin.name)
            .snippet(pin.address)

        val newMarker = map.addMarker(markerOptions)
        searchMarker = newMarker
        newMarker.let { map.selectMarker(it) }
    }

    // Handle polyline route geometry rendering on map
    LaunchedEffect(mapState.routeState.activeRoute) {
        val map = mapLibreMap ?: return@LaunchedEffect
        currentPolyline?.let { map.removePolyline(it) }
        currentPolyline = null

        val activeRoute = mapState.routeState.activeRoute ?: return@LaunchedEffect
        if (activeRoute.points.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .addAll(activeRoute.points)
                .color(android.graphics.Color.parseColor("#2563EB"))
                .width(6f)

            currentPolyline = map.addPolyline(polylineOptions)

            if (activeRoute.points.size > 1) {
                try {
                    val boundsBuilder = LatLngBounds.Builder()
                    activeRoute.points.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120), 600)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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

@SuppressLint("MissingPermission")
private fun setupLocationComponent(
    map: MapLibreMap,
    style: Style,
    context: Context,
    mapState: MapState
) {
    if (!hasLocationPermission(context)) return
    try {
        val locationComponent = map.locationComponent
        val options = LocationComponentOptions.builder(context)
            .pulseEnabled(true)
            .pulseColor(android.graphics.Color.parseColor("#2563EB"))
            .foregroundTintColor(android.graphics.Color.parseColor("#2563EB"))
            .backgroundTintColor(android.graphics.Color.parseColor("#FFFFFF"))
            .bearingTintColor(android.graphics.Color.parseColor("#2563EB"))
            .accuracyAlpha(0.18f)
            .accuracyColor(android.graphics.Color.parseColor("#3B82F6"))
            .build()

        val activationOptions = LocationComponentActivationOptions.builder(context, style)
            .locationComponentOptions(options)
            .useDefaultLocationEngine(false)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.cameraMode = CameraMode.NONE

        mapState.userLocation?.let {
            locationComponent.forceLocationUpdate(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

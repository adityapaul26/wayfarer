package com.adityapaul.wayfarer.data.route

import com.google.gson.annotations.SerializedName
import org.maplibre.android.geometry.LatLng
import java.util.Locale

data class OsrmRouteResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("routes") val routes: List<OsrmRoute>?,
    @SerializedName("message") val message: String?
)

data class OsrmRoute(
    @SerializedName("geometry") val geometry: OsrmGeometry?,
    @SerializedName("distance") val distanceMeters: Double?,
    @SerializedName("duration") val durationSeconds: Double?
)

data class OsrmGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>?,
    @SerializedName("type") val type: String?
)

data class RouteResult(
    val originName: String,
    val destinationName: String,
    val points: List<LatLng>,
    val distanceMeters: Double,
    val durationSeconds: Double
) {
    fun formattedDistance(): String {
        return if (distanceMeters >= 1000) {
            String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }
    }

    fun formattedDuration(): String {
        val totalMinutes = (durationSeconds / 60.0).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes} mins"
            else -> "Less than 1 min"
        }
    }
}

package com.adityapaul.wayfarer.data.route

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import java.util.Locale

interface RouteRepository {
    suspend fun calculateRoute(
        originName: String,
        originLat: Double,
        originLng: Double,
        destinationName: String,
        destLat: Double,
        destLng: Double
    ): Result<RouteResult>
}

class OsrmRouteRepository(
    private val apiService: OsrmApiService = OsrmApiService.create()
) : RouteRepository {

    override suspend fun calculateRoute(
        originName: String,
        originLat: Double,
        originLng: Double,
        destinationName: String,
        destLat: Double,
        destLng: Double
    ): Result<RouteResult> = withContext(Dispatchers.IO) {
        try {
            // OSRM expects coordinates in "longitude,latitude;longitude,latitude" format!
            val coordinatesParam = String.format(
                Locale.US,
                "%.6f,%.6f;%.6f,%.6f",
                originLng,
                originLat,
                destLng,
                destLat
            )

            val response = apiService.getRoute(coordinatesParam)

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Route service error (HTTP ${response.code()})")
                )
            }

            val body = response.body()
            if (body == null || body.code != "Ok" || body.routes.isNullOrEmpty()) {
                val errorMsg = body?.message ?: "Unable to find a valid route between locations"
                return@withContext Result.failure(Exception(errorMsg))
            }

            val firstRoute = body.routes.first()
            val coordArray = firstRoute.geometry?.coordinates

            if (coordArray.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("No route geometry returned"))
            }

            val points = coordArray.mapNotNull { coordPair ->
                if (coordPair.size >= 2) {
                    val lng = coordPair[0]
                    val lat = coordPair[1]
                    LatLng(lat, lng)
                } else null
            }

            if (points.isEmpty()) {
                return@withContext Result.failure(Exception("Parsed route contains no valid coordinates"))
            }

            val result = RouteResult(
                originName = originName,
                destinationName = destinationName,
                points = points,
                distanceMeters = firstRoute.distanceMeters ?: 0.0,
                durationSeconds = firstRoute.durationSeconds ?: 0.0
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

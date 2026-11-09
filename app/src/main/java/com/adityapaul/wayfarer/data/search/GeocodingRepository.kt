package com.adityapaul.wayfarer.data.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

interface GeocodingRepository {
    suspend fun searchPlaces(query: String): Result<List<SearchResult>>
}

class NominatimGeocodingRepository : GeocodingRepository {

    override suspend fun searchPlaces(query: String): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        val encodedQuery = URLEncoder.encode(trimmedQuery, "UTF-8")
        val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=10"

        try {
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "WayfarerAndroidApp/1.0 (com.adityapaul.wayfarer)")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonText)
                val results = mutableListOf<SearchResult>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val placeId = item.optLong("place_id", i.toLong())
                    val displayName = item.optString("display_name", "")
                    val lat = item.optDouble("lat", 0.0)
                    val lon = item.optDouble("lon", 0.0)
                    val type = item.optString("type", "").replace("_", " ").trim()

                    val nameParts = displayName.split(",")
                    val primaryName = nameParts.firstOrNull()?.trim() ?: displayName
                    val secondaryAddress = if (nameParts.size > 1) {
                        nameParts.drop(1).joinToString(",").trim()
                    } else {
                        displayName
                    }

                    results.add(
                        SearchResult(
                            id = placeId,
                            name = primaryName,
                            address = secondaryAddress,
                            category = if (type.isNotEmpty()) type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } else null,
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }

                Result.success(results)
            } else {
                Result.failure(Exception("Geocoding service error (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.adityapaul.wayfarer.data.route

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApiService {

    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): Response<OsrmRouteResponse>

    companion object {
        private const val BASE_URL = "https://router.project-osrm.org/"

        fun create(): OsrmApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OsrmApiService::class.java)
        }
    }
}

package com.adityapaul.wayfarer.utils

import android.content.Context
import android.content.Intent
import java.util.Locale

object LocationShareUtils {

    fun shareCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double,
        locationName: String? = null
    ) {
        val formattedCoords = String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
        val title = locationName ?: "My Location"
        val shareMessage = "$title: $formattedCoords\nhttps://maps.google.com/?q=$formattedCoords"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$title Coordinates")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        val chooser = Intent.createChooser(sendIntent, "Share Location via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

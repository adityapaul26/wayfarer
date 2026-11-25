package com.adityapaul.wayfarer.data.route

import android.location.Location
import org.maplibre.android.geometry.LatLng

data class SnapResult(
    val snappedPoint: LatLng,
    val segmentIndex: Int,
    val distanceToPolylineMeters: Float
)

object NavigationUtils {

    fun distanceMeters(p1: LatLng, p2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0]
    }

    fun snapToPolyline(userPos: LatLng, polyline: List<LatLng>): SnapResult? {
        if (polyline.isEmpty()) return null
        if (polyline.size == 1) {
            val dist = distanceMeters(userPos, polyline.first())
            return SnapResult(polyline.first(), 0, dist)
        }

        var minDistance = Float.MAX_VALUE
        var bestSnapPoint = polyline.first()
        var bestSegmentIndex = 0

        for (i in 0 until polyline.size - 1) {
            val a = polyline[i]
            val b = polyline[i + 1]
            val snap = getClosestPointOnSegment(userPos, a, b)
            val dist = distanceMeters(userPos, snap)

            if (dist < minDistance) {
                minDistance = dist
                bestSnapPoint = snap
                bestSegmentIndex = i
            }
        }

        return SnapResult(bestSnapPoint, bestSegmentIndex, minDistance)
    }

    private fun getClosestPointOnSegment(p: LatLng, a: LatLng, b: LatLng): LatLng {
        val latP = Math.toRadians(p.latitude)
        val lonP = Math.toRadians(p.longitude)
        val latA = Math.toRadians(a.latitude)
        val lonA = Math.toRadians(a.longitude)
        val latB = Math.toRadians(b.latitude)
        val lonB = Math.toRadians(b.longitude)

        val dLonAB = lonB - lonA
        val dLatAB = latB - latA

        val lengthSq = dLatAB * dLatAB + dLonAB * dLonAB
        if (lengthSq == 0.0) return a

        val t = ((latP - latA) * dLatAB + (lonP - lonA) * dLonAB) / lengthSq
        val clampedT = t.coerceIn(0.0, 1.0)

        val snapLat = Math.toDegrees(latA + clampedT * dLatAB)
        val snapLon = Math.toDegrees(lonA + clampedT * dLonAB)

        return LatLng(snapLat, snapLon)
    }

    fun splitPolyline(
        snapResult: SnapResult,
        polyline: List<LatLng>
    ): Pair<List<LatLng>, List<LatLng>> {
        if (polyline.isEmpty()) return Pair(emptyList(), emptyList())

        val index = snapResult.segmentIndex
        val snapPoint = snapResult.snappedPoint

        val traveled = polyline.take(index + 1).toMutableList()
        traveled.add(snapPoint)

        val remaining = mutableListOf(snapPoint)
        if (index + 1 < polyline.size) {
            remaining.addAll(polyline.drop(index + 1))
        }

        return Pair(traveled, remaining)
    }

    fun calculatePathDistance(points: List<LatLng>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += distanceMeters(points[i], points[i + 1])
        }
        return total
    }

    fun calculateSpeedMps(prevLoc: Location?, currLoc: Location): Float {
        if (currLoc.hasSpeed() && currLoc.speed > 0f) {
            return currLoc.speed
        }
        if (prevLoc == null) return 0f

        val timeDeltaSeconds = (currLoc.elapsedRealtimeNanos - prevLoc.elapsedRealtimeNanos) / 1_000_000_000.0
        if (timeDeltaSeconds <= 0.1) return 0f

        val dist = distanceMeters(
            LatLng(prevLoc.latitude, prevLoc.longitude),
            LatLng(currLoc.latitude, currLoc.longitude)
        )
        return (dist / timeDeltaSeconds).toFloat()
    }
}

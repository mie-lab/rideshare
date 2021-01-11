package ch.ethz.ikg.rideshare.util

import java.lang.Math.*

object Geography {
    const val EarthRadius = 6372.8 // in kilometers

    fun haversineDistance(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
        val λ1 = toRadians(lat1)
        val λ2 = toRadians(lat2)
        val Δλ = toRadians(lat2 - lat1)
        val Δφ = toRadians(lon2 - lon1)
        return 2 * EarthRadius * asin(sqrt(pow(sin(Δλ / 2), 2.0) + pow(sin(Δφ / 2), 2.0) * cos(λ1) * cos(λ2)))
    }
}
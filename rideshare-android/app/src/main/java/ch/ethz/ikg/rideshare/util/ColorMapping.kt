package ch.ethz.ikg.rideshare.util

import android.content.Context
import androidx.core.content.ContextCompat
import ch.ethz.ikg.rideshare.R

object ColorMapping {
    fun getColor(context: Context, transportMode: String): Int {
        val colorCode = when (transportMode) {
            "walk" -> R.color.transportModeWalk
            "run" -> R.color.transportModeRun
            "bike" -> R.color.transportModeBike
            "ebike" -> R.color.transportModeEBike
            "tram" -> R.color.transportModeTram
            "bus" -> R.color.transportModeBus
            "subway" -> R.color.transportModeSubway
            "boat" -> R.color.transportModeBoat
            "train" -> R.color.transportModeTrain
            "ecar" -> R.color.transportModeECar
            "car" -> R.color.transportModeCar
            "motorcycle" -> R.color.transportModeMotorcycle
            "airplane" -> R.color.transportModeAirplane
            "skate" -> R.color.transportModeSkate
            "ski" -> R.color.transportModeSki
            "scooter" -> R.color.transportModeScooter
            "skilift" -> R.color.transportModeSkilift
            "kayak" -> R.color.transportModeKayak
            "horse" -> R.color.transportModeHorse
            "helicopter" -> R.color.transportModeHelicopter
            else -> R.color.transportModeUnknown
        }

        return ContextCompat.getColor(context, colorCode)
    }
}
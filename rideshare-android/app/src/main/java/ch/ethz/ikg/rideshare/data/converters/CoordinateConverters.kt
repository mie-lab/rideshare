package ch.ethz.ikg.rideshare.data.converters

import androidx.room.TypeConverter
import com.beust.klaxon.Klaxon

class CoordinateConverters {
    @TypeConverter
    fun stringToCoordinates(data: String): List<Pair<Double, Double>> {
        return Klaxon().parseArray<Pair<Double, Double>>(data).orEmpty()
    }

    @TypeConverter
    fun coordinatesToString(data: List<Pair<Double, Double>>): String {
        return Klaxon().toJsonString(data)
    }
}

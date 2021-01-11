package ch.ethz.ikg.rideshare.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONTokener

/**
 * Data class that contains trackpoints (before and to a certain degree after they have
 * been synchronized with the server).
 */
@Entity(tableName = "trackpoints")
data class Trackpoint(
    @PrimaryKey(autoGenerate = true) var uid: Long = 0,
    @ColumnInfo(name = "sync") var sync: Boolean = false,

    @ColumnInfo(name = "timestamp") var timestamp: Long = 0,
    @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0,

    @ColumnInfo(name = "accuracy") var accuracy: Float = 0.0f,
    @ColumnInfo(name = "elevation") var elevation: Double = 0.0,
    @ColumnInfo(name = "vertical_accuracy") var verticalAccuracy: Float? = null,
    @ColumnInfo(name = "bearing") var bearing: Float = 0.0f,
    @ColumnInfo(name = "bearing_accuracy") var bearingAccuracy: Float? = null,
    @ColumnInfo(name = "provider") var provider: String = "",
    @ColumnInfo(name = "speed") var speed: Float = 0.0f,
    @ColumnInfo(name = "speed_accuracy") var speedAccuracy: Float? = null
)
package ch.ethz.ikg.rideshare.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "staypoints")
data class Staypoint(
    @PrimaryKey(autoGenerate = true) var uid: Long = 0,

    @ColumnInfo(name = "started_at") var startedAt: Long = 0,
    @ColumnInfo(name = "finished_at") var finishedAt: Long = 0,
    @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0
)
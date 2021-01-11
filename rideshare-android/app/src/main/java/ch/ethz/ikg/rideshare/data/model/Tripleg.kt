package ch.ethz.ikg.rideshare.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ch.ethz.ikg.rideshare.data.converters.CoordinateConverters
import ch.ethz.ikg.rideshare.util.Geography

@Entity(tableName = "triplegs")
@TypeConverters(CoordinateConverters::class)
data class Tripleg(
    @PrimaryKey(autoGenerate = true) var uid: Long = 0,

    @ColumnInfo(name = "started_at") val startedAt: Long = 0,
    @ColumnInfo(name = "finished_at") val finishedAt: Long = 0,

    @ColumnInfo(name = "mode_validated") val modeValidated: String = "",

    @ColumnInfo(name = "geom") val geom: List<Pair<Double, Double>> = listOf()
) {
    val length: Double
        get() = this.geom.zipWithNext().foldRight(0.0,
            { pr, acc ->
                acc + Geography.haversineDistance(
                    pr.first.first,
                    pr.first.second,
                    pr.second.first,
                    pr.second.second
                )
            })
}
package ch.ethz.ikg.rideshare.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.ethz.ikg.rideshare.data.model.Staypoint


@Dao
interface StaypointDao {
    @Query("SELECT * FROM staypoints")
    fun getAll(): LiveData<List<Staypoint>>

    @Query("SELECT * FROM staypoints WHERE (started_at BETWEEN :dayStart AND :dayEnd) OR (finished_at BETWEEN :dayStart AND :dayEnd)")
    fun getAllOnDate(dayStart: Long, dayEnd: Long): List<Staypoint>

    @Insert
    suspend fun insertAll(vararg staypoints: Staypoint)

    @Delete
    suspend fun delete(vararg staypoints: Staypoint)

    @Query("DELETE FROM staypoints")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(vararg staypoints: Staypoint) {
        deleteAll()
        insertAll(*staypoints)
    }
}

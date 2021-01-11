package ch.ethz.ikg.rideshare.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.ethz.ikg.rideshare.data.model.Trackpoint
import java.util.*

@Dao
interface TrackpointDao {
    @Query("SELECT * FROM trackpoints")
    fun getAll(): LiveData<List<Trackpoint>>

    @Query("SELECT * FROM trackpoints WHERE sync=0")
    fun getNonSynchronized(): LiveData<List<Trackpoint>>

    @Query("SELECT * FROM trackpoints WHERE timestamp BETWEEN :dayStart AND :dayEnd")
    fun getAllOnDate(dayStart: Long, dayEnd: Long): List<Trackpoint>

    @Insert
    suspend fun insertAll(vararg trackpoints: Trackpoint)

    @Delete
    suspend fun delete(vararg trackpoints: Trackpoint)

    @Query("DELETE FROM trackpoints")
    suspend fun deleteAll()

    @Query("UPDATE trackpoints SET sync=1 WHERE uid IN (:ptIds)")
    suspend fun setAllSynchronized(ptIds: List<Long>)
}
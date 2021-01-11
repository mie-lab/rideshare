package ch.ethz.ikg.rideshare.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.ethz.ikg.rideshare.data.model.Tripleg


@Dao
interface TriplegDao {
    @Query("SELECT * FROM triplegs")
    fun getAll(): LiveData<List<Tripleg>>

    @Query("SELECT * FROM triplegs WHERE (started_at BETWEEN :dayStart AND :dayEnd) OR (finished_at BETWEEN :dayStart AND :dayEnd)")
    fun getAllOnDate(dayStart: Long, dayEnd: Long): List<Tripleg>

    @Insert
    suspend fun insertAll(vararg triplegs: Tripleg)

    @Delete
    suspend fun delete(vararg triplegs: Tripleg)

    @Query("DELETE FROM triplegs")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(vararg triplegs: Tripleg) {
        deleteAll()
        insertAll(*triplegs)
    }
}
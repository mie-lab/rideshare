package ch.ethz.ikg.rideshare.data.daos

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.room.*
import ch.ethz.ikg.rideshare.data.model.User


@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Transaction
    suspend fun upsert(user: User) {
        try {
            insert(user)
        } catch (exception: SQLiteConstraintException) {
            update(user)
        }
    }
}
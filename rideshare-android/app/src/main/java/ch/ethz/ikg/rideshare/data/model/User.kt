package ch.ethz.ikg.rideshare.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.StringBufferInputStream

/**
 * Data class that captures user information for logged in users retrieved from UserRepository.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey var username: String,
    @ColumnInfo(name = "token") var token: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "first_name") var firstName: String,
    @ColumnInfo(name = "last_name") var lastName: String
)

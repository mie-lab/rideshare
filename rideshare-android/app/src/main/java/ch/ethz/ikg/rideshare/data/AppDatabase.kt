package ch.ethz.ikg.rideshare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.data.daos.StaypointDao
import ch.ethz.ikg.rideshare.data.daos.TrackpointDao
import ch.ethz.ikg.rideshare.data.daos.TriplegDao
import ch.ethz.ikg.rideshare.data.daos.UserDao
import ch.ethz.ikg.rideshare.data.model.Staypoint
import ch.ethz.ikg.rideshare.data.model.Trackpoint
import ch.ethz.ikg.rideshare.data.model.Tripleg
import ch.ethz.ikg.rideshare.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Database(
    entities = arrayOf(Trackpoint::class, Tripleg::class, Staypoint::class, User::class),
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackpointDao(): TrackpointDao
    abstract fun triplegDao(): TriplegDao
    abstract fun staypointDao(): StaypointDao

    abstract fun userDao(): UserDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, Constants.DatabaseName
                    )
                        .fallbackToDestructiveMigration()
                        //.addCallback(roomCallback)
                        .build()
                }
            }

            return instance
        }

        fun destroyInstance() {
            instance = null
        }

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        instance?.trackpointDao()?.deleteAll()
//                        instance?.trackpointDao()?.insertAll(
//                            Trackpoint(0, false, 123, 8.5, 57.4),
//                            Trackpoint(0, false, 123, 8.53, 57.3),
//                            Trackpoint(0, false, 123, 8.56, 57.2)
//                        )
                    }
                }
            }
        }
    }
}

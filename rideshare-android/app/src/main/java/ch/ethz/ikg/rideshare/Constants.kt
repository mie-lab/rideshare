package ch.ethz.ikg.rideshare

object Constants {
    const val ServerURL = "http://10.0.2.2:8000/api/v1/"
    //const val ServerURL = "http://rideshare.ethz.ch/api/v1/"

    const val LogTag = "RSLOG"
    const val SplashDisplayLength = 2000L

    const val NotificationChannelId = "RIDESHARE"
    const val NotificationId = 958
    const val ServiceNotificationId = 959

    const val DatabaseName = "rideshare-database"

    // For testing.
    const val minLocationUpdateTimeNetwork = 1000L
    const val minLocationUpdateTimeGPS = 1000L
    const val minLocationDistanceNetwork = 10.0f
    const val minLocationDistanceGPS = 10.0f
    const val ServerUpdateTime = 2500

    // For deployment.
//    const val minLocationUpdateTimeNetwork = 15000L
//    const val minLocationUpdateTimeGPS = 15000L
//    const val minLocationDistanceNetwork = 50.0f
//    const val minLocationDistanceGPS = 20.0f
//    const val ServerUpdateTime = 1000 * 60 * 5  // 5 minutes
}
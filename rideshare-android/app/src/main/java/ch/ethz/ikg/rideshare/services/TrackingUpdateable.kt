package ch.ethz.ikg.rideshare.services

import android.content.Intent

interface TrackingUpdateable {
    fun onUpdate(intent: Intent)
}
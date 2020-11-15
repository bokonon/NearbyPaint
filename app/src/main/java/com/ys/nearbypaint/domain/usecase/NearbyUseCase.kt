package com.ys.nearbypaint.domain.usecase

import android.app.Activity
import android.app.PendingIntent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.ys.nearbypaint.utiles.NearbyPaintLog

class NearbyUseCase(private val activity: FragmentActivity, listener: NearbySubscribeListener) {

    interface NearbySubscribeListener {
        fun subscribe(message: Message)
    }

    private val TAG: String = javaClass.simpleName

    /**
     * Listener on message received
     */
    private val messageListener = object : MessageListener() {
        override fun onFound(message: Message?) {
            NearbyPaintLog.d(TAG, "onFound message: ${message?.toString()}")
            if (message != null) {
                listener.subscribe(message)
            }
        }
        override fun onLost(message: Message) {
            NearbyPaintLog.d(TAG, "onLost message: $message")
        }

        override fun onDistanceChanged(message: Message, distance: Distance) {
            NearbyPaintLog.d(TAG, "onDistanceChanged message: $message")
            NearbyPaintLog.d(TAG, "distance accuracy: ${distance.accuracy}")
            NearbyPaintLog.d(TAG, "distance meters: ${distance.meters}")
        }

        override fun onBleSignalChanged(message: Message, bleSignal: BleSignal) {
            NearbyPaintLog.d(TAG, "onBleSignalChanged message: $message")
            NearbyPaintLog.d(TAG, "bleSignal rssi: ${bleSignal.rssi}")
            NearbyPaintLog.d(TAG, "bleSignal txPower: ${bleSignal.txPower}")
        }
    }

    fun subscribe() {
        Nearby.getMessagesClient(activity).subscribe(messageListener)
    }

    fun unsubscribe() {
        Nearby.getMessagesClient(activity).unsubscribe(messageListener)
    }

    /**
     * post message
     */
    fun publish(message: Message) {
        NearbyPaintLog.d(TAG, "message.namespace : $message.namespace")
        NearbyPaintLog.d(TAG, "message.content : $message.content")
        Nearby.getMessagesClient(activity).publish(message)
    }

}
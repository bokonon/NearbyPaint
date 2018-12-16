package com.ys.nearbypaint.domain.usecase

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.ys.nearbypaint.utiles.NearbyPaintLog

class NearbyUseCase(fragmentActivity: FragmentActivity, listener: NearbySubscribeListener) {

    interface NearbySubscribeListener {
        fun subscribe(message: Message)
    }

    companion object {
        private val TAG: String = if (javaClass.simpleName != null) javaClass.simpleName else "NearbyUseCase"
    }

    /**
     * Nearby connection callback
     */
    private val callbacks = NearbyConnectionCallbacks()

    /**
     * Nearby connection failed listener
     */
    private val failedListener = NearbyConnectionFailedListener()

    private val googleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(fragmentActivity)
            .addApi(Nearby.MESSAGES_API)
            .addConnectionCallbacks(callbacks)
            .enableAutoManage(fragmentActivity, failedListener)
            .build()
    }

    /**
     * Nearby Message strategy
     */
    private val strategy = Strategy.Builder()
        .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
        .setDistanceType(Strategy.DISCOVERY_MODE_DEFAULT)
        .setTtlSeconds(Strategy.TTL_SECONDS_DEFAULT)
        .build()

    /**
     * Listener on message received
     */
    val messageListener = object : MessageListener() {
        override fun onFound(message: Message?) {
            NearbyPaintLog.d(TAG, "onFound: ${message?.toString()}")
            if (message != null) {
                listener.subscribe(message)
            }
        }
    }

    fun connect() {
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
        }
    }

    fun disconnect() {
        if (googleApiClient.isConnected) {
            Nearby.Messages.unsubscribe(googleApiClient, messageListener)
        }
        googleApiClient.disconnect()
    }


    /**
     * post message
     */
    fun publish(message: Message) {
        if (!googleApiClient.isConnected) {
            NearbyPaintLog.d(TAG, "client is not connected")
            return
        }
        val options = PublishOptions.Builder()
            .setStrategy(strategy)
            .setCallback(object : PublishCallback() {
                override fun onExpired() {
                    super.onExpired()
                    NearbyPaintLog.d(TAG, "publish onExpired")
                }
            })
            .build()

        NearbyPaintLog.d(TAG, "message.namespace() : $message.namespace")
        NearbyPaintLog.d(TAG, "message.content() : $message.content")

        Nearby.Messages.publish(googleApiClient, message, options)
            .setResultCallback {
                if (it.isSuccess) {
                    NearbyPaintLog.d(TAG, "publish succeeded")
                } else {
                    NearbyPaintLog.d(TAG, "publish not succeeded")
                }
            }
    }

    /**
     * Callbacks for Nearby Connection
     */
    private inner class NearbyConnectionCallbacks : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(connectionHint: Bundle?) {
            NearbyPaintLog.d(TAG, "onConnected: $connectionHint")
            NearbyPaintLog.d(TAG, "Start subscribe")

            val options = SubscribeOptions.Builder()
                .setStrategy(strategy)
                .setCallback(object : SubscribeCallback() {
                    override fun onExpired() {
                        super.onExpired()
                        NearbyPaintLog.d(TAG, "No longer subscribing")
                    }
                }).build()
            Nearby.Messages.subscribe(googleApiClient, messageListener, options)
        }

        override fun onConnectionSuspended(p0: Int) {
            NearbyPaintLog.d(TAG, "onConnectionSuspended")
        }
    }

    /**
     * Listener when Nearby Connection failed
     */
    private class NearbyConnectionFailedListener : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(p0: ConnectionResult) {
            NearbyPaintLog.d(TAG, "onConnectionFailed")
            NearbyPaintLog.d(TAG, p0.toString())
            if (p0.resolution != null) {
                val pendingIntent = p0.resolution as PendingIntent
                NearbyPaintLog.d(TAG, "describeContents : $pendingIntent.describeContents().toString()")
                NearbyPaintLog.d(TAG, "creatorPackage : $pendingIntent.creatorPackage")
                NearbyPaintLog.d(TAG, "targetPackage : $pendingIntent.targetPackage")
            }
        }
    }
}
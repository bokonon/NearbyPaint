package com.ys.nearbypaint.utiles

import android.util.Log
import com.ys.nearbypaint.BuildConfig

class NearbyPaintLog {
    companion object {
        private const val PRE_FIX = "NP "
        fun d(tag: String, message: String) {
            if (BuildConfig.DEBUG) {
                Log.d(PRE_FIX + tag, message)
            }
        }
        fun e(tag: String, message: String?, throwable: Throwable?) {
            if (BuildConfig.DEBUG) {
                if (throwable == null) {
                    Log.e(PRE_FIX + tag, message)
                } else {
                    Log.e(PRE_FIX + tag, message, throwable)
                }
            }
        }
    }
}
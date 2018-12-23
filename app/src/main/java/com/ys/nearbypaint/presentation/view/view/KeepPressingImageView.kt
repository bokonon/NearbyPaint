package com.ys.nearbypaint.presentation.view.view

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.ys.nearbypaint.utiles.NearbyPaintLog
import java.util.*

class KeepPressingImageView(context: Context?, attrs: AttributeSet?) :
    ImageView(context, attrs) {

    interface OnKeepPressingListener {
        fun onKeepPressing()
        fun onKeepPressingEnd()
    }

    private val TAG : String = if(javaClass.simpleName != null) javaClass.simpleName else "KeepPressingImageView"

    companion object {
        const val DEFAULT_PERIOD = 100L
    }

    private var timer: Timer? = null

    private var listener: OnKeepPressingListener? = null

    @get:JvmName("getHandler_")
    val handler = Handler()

    private val runnable = Runnable {
        this.listener?.onKeepPressing()
    }

    private var period = DEFAULT_PERIOD

    private var rect = Rect(left, top, right, bottom)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rect = Rect(left, top, right, bottom)
                prepareTimer()
            }
            MotionEvent.ACTION_MOVE -> {
                // swipe to outside
                if (!rect.contains((left + event.x).toInt(), (top + event.y).toInt())) {
                    NearbyPaintLog.d(TAG, "swipe to outside")
                    stopTimer()
                    this.listener?.onKeepPressingEnd()
                }
            }
            MotionEvent.ACTION_UP -> {
                NearbyPaintLog.d(TAG, "ACTION_UP")
                stopTimer()
                this.listener?.onKeepPressingEnd()
                performClick()
            }
            else -> {}
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        stopTimer()
        super.onDetachedFromWindow()
    }

    fun setKeepPressingListener(duration: Long, listener: OnKeepPressingListener) {
        this.period = duration
        this.listener = listener
    }

    fun forceStop() {
        NearbyPaintLog.d(TAG, "forceStop")
        stopTimer()
        this.listener?.onKeepPressingEnd()
    }

    private fun prepareTimer() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post(runnable)
            }
        },0, this.period)
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

}
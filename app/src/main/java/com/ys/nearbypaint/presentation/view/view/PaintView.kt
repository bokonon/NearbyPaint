package com.ys.nearbypaint.presentation.view.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class PaintView : View {

    companion object {
        val TAG : String = if(javaClass.simpleName != null) javaClass.simpleName else "PaintView"
        const val TOLERANCE = 3
    }

    interface OnDrawEnd {
        fun onDrawEnd(paintData: PaintData) {}
    }

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private val currentPoint = PointF()
    private val prePoint = PointF()
    private val downPoint = PointF()
    private var pathPointList: MutableList<PointF> = ArrayList()
    private var drawElementList: MutableList<DrawElement> = ArrayList()
    private var drawElement: DrawElement? = null
    private var listener: OnDrawEnd? = null

    private val elementMode = ElementMode.MODE_LINE
    private val bgColor = Color.WHITE
    private val brushColor = Color.BLACK
    private val thickness = 2
    //	private boolean antiAlias = true
    private val strokeType = true

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    fun setListener(listener: OnDrawEnd) {
        this.listener = listener
    }

    override fun onDraw(canvas : Canvas) {
        canvas.drawColor(bgColor)
        for (i in drawElementList.indices) {
            val element = drawElementList[i]
            canvas.drawPath(element.path, element.paint)
        }

        if (drawElement != null) {
            canvas.drawPath(drawElement?.path, drawElement?.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        currentPoint.x = event.x
        currentPoint.y = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
//                NearbyPaintLog.d(TAG, "ACTION_DOWN")
                downPoint.x = currentPoint.x
                downPoint.y = currentPoint.y

                drawElement = null
                drawElement = DrawElement(brushColor, thickness, strokeType)
                drawElement?.path?.moveTo(currentPoint.x, currentPoint.y)
                pathPointList.clear()
                when (elementMode) {
                    ElementMode.MODE_STAMP_RECTANGLE_DURATION, ElementMode.MODE_STAMP_TRIANGLE_DURATION, ElementMode.MODE_STAMP_STAR_DURATION -> {
                    }
                    else -> pathPointList.add(PointF(currentPoint.x, currentPoint.y))
                }
            }
            MotionEvent.ACTION_MOVE -> {
//                NearbyPaintLog.d(TAG, "ACTION_MOVE")
                when (elementMode) {
                    ElementMode.MODE_LINE ->
                        // Do not draw if moving distance is less than allowable value
                        if (Math.abs(currentPoint.x - prePoint.x) >= TOLERANCE || Math.abs(currentPoint.y - prePoint.y) >= TOLERANCE) {
                            // draw smooth line
                            drawElement?.path?.quadTo(
                                prePoint.x,
                                prePoint.y,
                                (prePoint.x + currentPoint.x) / 2,
                                (prePoint.y + currentPoint.y) / 2
                            )

                            // point list
                            pathPointList.add(PointF(currentPoint.x, currentPoint.y))
                        }
                    else -> {
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
//                NearbyPaintLog.d(TAG, "ACTION_UP")
                when (elementMode) {
                    ElementMode.MODE_LINE -> {
                        drawElement?.path?.lineTo(currentPoint.x, currentPoint.y)
                        // point list
                        pathPointList.add(PointF(currentPoint.x, currentPoint.y))
                    }
                    else -> {
                    }
                }
                // notify draw end
                onDrawEnd(pathPointList, strokeType)
            }
            else -> {

            }
        }
        addDrawElement(drawElement)
        prePoint.x = currentPoint.x
        prePoint.y = currentPoint.y
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()

        return true
    }

    @Synchronized
    fun addDrawElement(drawElement: DrawElement?) {
        if (drawElement != null) {
            drawElementList.add(drawElement)
            invalidate()
        }
    }

    fun clearList() {
        drawElementList.clear()
        drawElement = null
        invalidate()
    }

    fun captureCanvas(canvas: Canvas) {
        canvas.drawColor(bgColor)
        for (i in drawElementList.indices) {
            val element = drawElementList[i]
            canvas.drawPath(element.path, element.paint)
        }

        if (drawElement != null) {
            canvas.drawPath(drawElement?.path, drawElement?.paint)
        }
    }

    private fun onDrawEnd(pathPointList: List<PointF>, fillType: Boolean) {
        canvasWidth = width
        canvasHeight = height
        val paintData = PaintData(canvasWidth, canvasHeight, pathPointList)
        listener?.onDrawEnd(paintData)
    }

}
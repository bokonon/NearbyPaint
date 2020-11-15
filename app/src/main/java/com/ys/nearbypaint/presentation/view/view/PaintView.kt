package com.ys.nearbypaint.presentation.view.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ys.nearbypaint.utiles.NearbyPaintLog
import java.util.*

class PaintView : View {

    private val TAG : String = if(javaClass.simpleName != null) javaClass.simpleName else "PaintView"

    companion object {
        const val TOLERANCE = 3
    }

    interface OnDrawEnd {
        fun onDrawEnd(paintData: PaintData) {}
    }

    private val currentPoint = PointF()
    private val prePoint = PointF()
    private val downPoint = PointF()
    private var pathPointList: MutableList<PointF> = ArrayList()
    private var drawElementList: MutableList<DrawElement> = ArrayList()
    private var drawElement: DrawElement? = null
    private var listener: OnDrawEnd? = null

    private var elementMode = ElementMode.MODE_LINE
    private val bgColor = Color.WHITE
    private var brushColor = Color.BLACK
    private var thickness = 0
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
        super.onDraw(canvas)

        canvas.drawColor(bgColor)
        for (element in drawElementList) {
            canvas.drawPath(element.path, element.paint)
        }

        if (drawElement != null) {
            canvas.drawPath(drawElement!!.path, drawElement!!.paint)
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
                pathPointList.clear()
                when (elementMode) {
                    ElementMode.MODE_ERASER,
                    ElementMode.MODE_LINE -> {
                        drawElement?.path?.moveTo(currentPoint.x, currentPoint.y)
                        pathPointList.add(PointF(currentPoint.x, currentPoint.y))
                    }
                    ElementMode.MODE_STAMP_SQUARE, ElementMode.MODE_STAMP_TRIANGLE, ElementMode.MODE_STAMP_STAR -> {

                    }
                    else -> {}
                }
            }
            MotionEvent.ACTION_MOVE -> {
//                NearbyPaintLog.d(TAG, "ACTION_MOVE")
                when (elementMode) {
                    ElementMode.MODE_ERASER,
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
                    ElementMode.MODE_STAMP_SQUARE -> {
                        makeSquareStamp(downPoint, currentPoint)
                    }
                    ElementMode.MODE_STAMP_RECTANGLE -> {
                        makeRectangleStamp(downPoint, currentPoint)
                    }
                    else -> {
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
//                NearbyPaintLog.d(TAG, "ACTION_UP")
                performClick()
                when (elementMode) {
                    ElementMode.MODE_ERASER,
                    ElementMode.MODE_LINE -> {
                        drawElement?.path?.lineTo(currentPoint.x, currentPoint.y)
                        // point list
                        pathPointList.add(PointF(currentPoint.x, currentPoint.y))
                    }
                    ElementMode.MODE_STAMP_SQUARE -> {

                    }
                    else -> {
                    }
                }
                // notify draw end
                onDrawEnd(pathPointList)
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

    fun setElementMode(mode: ElementMode) {
        this.elementMode = mode
        if (mode == ElementMode.MODE_ERASER) {
            this.brushColor = Color.WHITE
        } else {
            this.brushColor = Color.BLACK
        }
    }

    fun setThickness(value: Int) {
        thickness = value
    }

    fun addThickness(value: Int): Int {
        thickness += value
        if (thickness < 1) {
            thickness = 1
        }
        return thickness
    }

    fun captureCanvas(canvas: Canvas) {
        canvas.drawColor(bgColor)
        for (i in drawElementList.indices) {
            val element = drawElementList[i]
            canvas.drawPath(element.path, element.paint)
        }

        if (drawElement != null) {
            canvas.drawPath(drawElement!!.path, drawElement!!.paint)
        }
    }

    private fun onDrawEnd(pathPointList: List<PointF>) {
        val paintData = PaintData(width, height, elementMode.rawValue, pathPointList, thickness, brushColor)
        listener?.onDrawEnd(paintData)
    }

    private fun makeSquareStamp(downPoint: PointF, currentPoint: PointF) {
        val diffX = currentPoint.x - downPoint.x
        val diffY = currentPoint.y - downPoint.y
        val dx = Math.pow(diffX.toDouble(), 2.0)
        val dy = Math.pow(diffY.toDouble(), 2.0)
        val radius = Math.sqrt(dx + dy).toFloat()
        NearbyPaintLog.d(TAG, "radius : $radius")

        val topPoint = PointF(downPoint.x, downPoint.y - radius)

        val currentToTopDiffX = currentPoint.x - topPoint.x
        val currentToTopDiffY = currentPoint.y - topPoint.y
        val currentToTopDx = Math.pow(currentToTopDiffX.toDouble(), 2.0)
        val currentToTopDy = Math.pow(currentToTopDiffY.toDouble(), 2.0)
        val currentToTopDistance = Math.sqrt(currentToTopDx + currentToTopDy).toFloat()

        val ratio = (radius * radius + radius * radius - currentToTopDistance * currentToTopDistance) /( 2 * radius * radius)
        var degree = Math.acos(ratio.toDouble()) * (180 / Math.PI)

        NearbyPaintLog.d(TAG, "degree : $degree")

        if (downPoint.x < currentPoint.x) {
            degree = -degree
        }

        val leftTopRadian = (degree + 315) * (Math.PI / 180)
        val newLeftTopX = downPoint.x + radius * Math.cos(leftTopRadian)
        val newLeftTopY = downPoint.y - radius * Math.sin(leftTopRadian)
        val newLeftTop = PointF(newLeftTopX.toFloat(), newLeftTopY.toFloat())

        val leftBottomRadian = (degree + 225) * (Math.PI / 180)
        val newLeftBottomX = downPoint.x + radius * Math.cos(leftBottomRadian)
        val newLeftBottomY = downPoint.y - radius * Math.sin(leftBottomRadian)
        val newLeftBottom = PointF(newLeftBottomX.toFloat(), newLeftBottomY.toFloat())

        val rightBottomRadian = (degree + 135) * (Math.PI / 180)
        val newRightBottomX = downPoint.x + radius * Math.cos(rightBottomRadian)
        val newRightBottomY = downPoint.y - radius * Math.sin(rightBottomRadian)
        val newRightBottom = PointF(newRightBottomX.toFloat(), newRightBottomY.toFloat())

        val rightTopRadian = (degree + 45) * (Math.PI / 180)
        val newRightTopX = downPoint.x + radius * Math.cos(rightTopRadian)
        val newRightTopY = downPoint.y - radius * Math.sin(rightTopRadian)
        val newRightTop = PointF(newRightTopX.toFloat(), newRightTopY.toFloat())

        pathPointList.clear()
        pathPointList.add(newLeftTop)
        pathPointList.add(newLeftBottom)
        pathPointList.add(newRightBottom)
        pathPointList.add(newRightTop)
        pathPointList.add(PointF(newLeftTop.x, newLeftTop.y))

        drawElement?.path?.reset()
        drawElement?.path?.moveTo(newLeftTop.x, newLeftTop.y)
        drawElement?.path?.lineTo(newLeftBottom.x, newLeftBottom.y)
        drawElement?.path?.lineTo(newRightBottom.x, newRightBottom.y)
        drawElement?.path?.lineTo(newRightTop.x, newRightTop.y)
        drawElement?.path?.lineTo(newLeftTop.x, newLeftTop.y)
    }

    private fun makeRectangleStamp(downPoint: PointF, currentPoint: PointF) {
        val diffX = currentPoint.x - downPoint.x
        val diffY = currentPoint.y - downPoint.y

        // for rectangle
        pathPointList.clear()
        pathPointList.add(PointF(downPoint.x - diffX, downPoint.y - diffY))
        pathPointList.add(PointF(downPoint.x - diffX, downPoint.y + diffY))
        pathPointList.add(PointF(downPoint.x + diffX, downPoint.y + diffY))
        pathPointList.add(PointF(downPoint.x + diffX, downPoint.y - diffY))
        pathPointList.add(PointF(downPoint.x - diffX, downPoint.y - diffY))

        drawElement?.path?.reset()
        drawElement?.path?.moveTo(downPoint.x - diffX, downPoint.y - diffY)
        drawElement?.path?.lineTo(downPoint.x - diffX, downPoint.y + diffY)
        drawElement?.path?.lineTo(downPoint.x + diffX, downPoint.y + diffY)
        drawElement?.path?.lineTo(downPoint.x + diffX, downPoint.y - diffY)
        drawElement?.path?.lineTo(downPoint.x - diffX, downPoint.y - diffY)
    }

}
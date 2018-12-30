package com.ys.nearbypaint.presentation.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.nearby.messages.Message
import com.ys.nearbypaint.R
import com.ys.nearbypaint.domain.usecase.CaptureUseCase
import com.ys.nearbypaint.domain.usecase.NearbyUseCase
import com.ys.nearbypaint.presentation.view.activity.MainActivity
import com.ys.nearbypaint.presentation.view.view.*
import com.ys.nearbypaint.utiles.GsonUtil
import com.ys.nearbypaint.utiles.NearbyPaintLog
import com.ys.nearbypaint.utiles.SharedPreferencesManager


class MainFragment : Fragment(), NearbyUseCase.NearbySubscribeListener, PaintView.OnDrawEnd,
    MainActivity.OnActivityWindowFocusChangedListener {

    private val TAG : String = if(javaClass.simpleName != null) javaClass.simpleName else "MainFragment"
    companion object {
//        fun newInstance(): MainFragment {
//            return MainFragment()
//        }
    }

    /**
     * Nearby UseCase
     */
    private lateinit var nearbyUseCase: NearbyUseCase

    /**
     * Painting Area View
     */
    private lateinit var paintView: PaintView

    private lateinit var thicknessNumberText: DisappearTextView

    // status bar height
    private var statusBarHeight = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        val view =inflater.inflate(R.layout.fragment_main, container, false)

        nearbyUseCase = NearbyUseCase(activity as MainActivity, this)

        paintView = view.findViewById(R.id.paint_view) as PaintView
        paintView.setListener(this)

        val clearButton =  view.findViewById<ImageView>(R.id.clear_button)
        clearButton.setOnClickListener { clear() }

        val increaseThicknessButton =  view.findViewById<ImageView>(R.id.increase_thickness_button)
        increaseThicknessButton.setOnClickListener {
            paintView.setElementMode(ElementMode.MODE_LINE)
            increaseThickness(1)
        }

        val reduceThicknessButton =  view.findViewById<ImageView>(R.id.reduce_thickness_button)
        reduceThicknessButton.setOnClickListener {
            paintView.setElementMode(ElementMode.MODE_LINE)
            increaseThickness(-1)
        }

        val thickness = SharedPreferencesManager().readThickness(activity as Context)
        thicknessNumberText = view.findViewById(R.id.thickness_number_text)
        paintView.setThickness(thickness)
        thicknessNumberText.setDisappearText(thickness.toString())

        val squareButton = view.findViewById<ImageView>(R.id.square_button)
        squareButton.setOnClickListener{ paintView.setElementMode(ElementMode.MODE_STAMP_SQUARE) }

        val rectangleButton = view.findViewById<ImageView>(R.id.rectangle_button)
        rectangleButton.setOnClickListener{ paintView.setElementMode(ElementMode.MODE_STAMP_RECTANGLE) }

        val eraserButton = view.findViewById<ImageView>(R.id.eraser_button)
        eraserButton.setOnClickListener{ paintView.setElementMode(ElementMode.MODE_ERASER) }

        val saveButton =  view.findViewById(R.id.save_button) as ImageView
        saveButton.setOnClickListener { save() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)

    }

    override fun onStart() {
        super.onStart()
        nearbyUseCase.connect()
    }

    override fun onStop() {
        nearbyUseCase.disconnect()
        super.onStop()
    }

    /**
     * subscribe message
     */
    override fun subscribe(message: Message) {
        activity?.runOnUiThread {
//            NearbyPaintLog.d(TAG, "message : $message")
            val paintData = GsonUtil.fromMessage(message)
            parseData(paintData)
        }
    }

    override fun onDrawEnd(paintData: PaintData) {
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        NearbyPaintLog.d(TAG, "paintData.canvasWidth : " + paintData.canvasWidth)
        NearbyPaintLog.d(TAG, "paintData.canvasHeight : " + paintData.canvasHeight)
        NearbyPaintLog.d(TAG, "paintData.clearFlg : " + paintData.clearFlg)
        NearbyPaintLog.d(TAG, "paintData.thickness : " + paintData.thickness)
        NearbyPaintLog.d(TAG, "paintData.color() : " + paintData.color())

        decreaseStatusBarHeight(paintData.points)
        nearbyUseCase.publish(GsonUtil.newMessage(paintData))
    }

    override fun onActivityWindowFocusChanged(hasFocus: Boolean) {
        statusBarHeight = getStatusBarHeight()
    }

    private fun getStatusBarHeight(): Int {
        val rect = Rect()
        val window = activity?.window
        window?.decorView?.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }

    private fun parseData(paintData: PaintData) {
        // clear
        if (paintData.clearFlg == 1) {
            paintView.clearList()
        } else {
            val elementMode = ElementMode.from(paintData.elementMode)
            val color = paintData.color()
            NearbyPaintLog.d(TAG, "subscribe color : $color")
            val thickness = paintData.thickness
            val drawElement = DrawElement(color, thickness, true)
            val width = paintData.canvasWidth
            val height = paintData.canvasHeight
            val canvasWidth = paintView.width
            val canvasHeight = paintView.height

            var prePoint: PointF? = null
            for ((num, point) in paintData.points.withIndex()) {
                point.x = point.x / width * canvasWidth
                point.y = point.y / height * canvasHeight
                increaseStatusBarHeight(point)

                if (num == 0) {
                    drawElement.path.moveTo(point.x, point.y)
                } else {
                    when (elementMode) {
                        ElementMode.MODE_ERASER,
                        ElementMode.MODE_LINE -> {
                            if (prePoint != null) {
                                drawElement.path.quadTo(
                                    prePoint.x,
                                    prePoint.y,
                                    (prePoint.x + point.x) / 2,
                                    (prePoint.y + point.y) / 2)
                            }
                        }
                        else -> {
                            drawElement.path.lineTo(point.x, point.y)
                        }
                    }
                }
                prePoint = point
            }
            paintView.addDrawElement(drawElement)
        }
    }

    private fun clear() {
        paintView.clearList()
        val message = GsonUtil.newMessage(PaintData(1))
        nearbyUseCase.publish(message)
    }

    private fun increaseThickness(value: Int) {
        val thickness = paintView.addThickness(value)
        thicknessNumberText.setDisappearText(thickness.toString())
        SharedPreferencesManager().putThickness(activity as Context, thickness)
    }

    private fun increaseStatusBarHeight(point: PointF) {
        // add status bar height
        point.y = point.y + statusBarHeight
    }

    private fun decreaseStatusBarHeight(points: List<PointF>) {
        for (point in points) {
            point.y = point.y - statusBarHeight
        }
    }

    private fun save() {
        val bitmap = drawBitmap(paintView)
        val filePath = CaptureUseCase().captureCanvas(activity as Context, bitmap)
        if (filePath != null) {
            Toast.makeText(
                activity?.applicationContext, getString(R.string.message_capture_success)
                        + "\nFilePath : " + filePath,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(activity?.applicationContext, R.string.message_capture_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * make bitmap
     * @return Bitmap
     */
    private fun drawBitmap(paintView: PaintView): Bitmap {
        val bitmap = Bitmap.createBitmap(paintView.width, paintView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paintView.captureCanvas(canvas)
        return bitmap
    }

}
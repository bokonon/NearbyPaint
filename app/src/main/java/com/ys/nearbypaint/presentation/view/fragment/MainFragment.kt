package com.ys.nearbypaint.presentation.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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

class MainFragment : Fragment(), NearbyUseCase.NearbySubscribeListener, PaintView.OnDrawEnd {

    private val TAG : String = if(javaClass.simpleName != null) javaClass.simpleName else "MainFragment"
    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
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

    private lateinit var reduceThicknessButton: KeepPressingImageView
    private lateinit var increaseThicknessButton: KeepPressingImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        val view =inflater.inflate(R.layout.fragment_main, container, false)

        nearbyUseCase = NearbyUseCase(activity as MainActivity, this)

        paintView = view.findViewById(R.id.paint_view) as PaintView
        paintView.setListener(this)

        val scrollView = view.findViewById(R.id.paint_scrollview) as ListenableHorizontalScrollView
        scrollView.setOnScrollChangeListener(object : ListenableHorizontalScrollView.OnScrollChangeListener{
            override fun onScrollChange() {
                NearbyPaintLog.d(TAG, "onScrollChange")
                onScrollChanged()
            }
        })

        val clearButton =  view.findViewById(R.id.clear_button) as ImageView
        clearButton.setOnClickListener { clear() }

        reduceThicknessButton =  view.findViewById(R.id.reduce_thickness_button) as KeepPressingImageView
        reduceThicknessButton.setOnClickListener { paintView.setElementMode(ElementMode.MODE_LINE) }
        reduceThicknessButton.setKeepPressingListener(KeepPressingImageView.DEFAULT_PERIOD, object :
            KeepPressingImageView.OnKeepPressingListener{
            override fun onKeepPressing() {
                increaseThickness(-1)
            }
            override fun onKeepPressingEnd() {
                commitThickness()
            }
        })
        increaseThicknessButton =  view.findViewById(R.id.increase_thickness_button) as KeepPressingImageView
        increaseThicknessButton.setOnClickListener { paintView.setElementMode(ElementMode.MODE_LINE) }
        increaseThicknessButton.setKeepPressingListener(KeepPressingImageView.DEFAULT_PERIOD, object :
            KeepPressingImageView.OnKeepPressingListener{
            override fun onKeepPressing() {
                increaseThickness(1)
            }
            override fun onKeepPressingEnd() {
                commitThickness()
            }
        })

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
            NearbyPaintLog.d(TAG, "message : $message")
            val paintData = GsonUtil.fromMessage(message)
            // eraser
            if (paintData.eraserFlg == 1) {
                paintView.clearList()
            } else {
                val drawElement = DrawElement(Color.BLACK, 2, true)
                val width = paintData.canvasWidth
                val height = paintData.canvasHeight
                var num = 0
                val canvasWidth = paintView.width
                val canvasHeight = paintView.height
                for (point in paintData.points) {
                    if (num == 0) {
                        drawElement.path.moveTo(point.x / width * canvasWidth, point.y / height * canvasHeight)
                        num++
                    }
                    drawElement.path.lineTo(point.x / width * canvasWidth, point.y / height * canvasHeight)
                }
                paintView.addDrawElement(drawElement)
            }
        }
    }

    override fun onDrawEnd(paintData: PaintData) {
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        NearbyPaintLog.d(TAG, "paintData.canvasWidth : " + paintData.canvasWidth)
        NearbyPaintLog.d(TAG, "paintData.canvasHeight : " + paintData.canvasHeight)
        NearbyPaintLog.d(TAG, "paintData.eraserFlg : " + paintData.eraserFlg)

        nearbyUseCase.publish(GsonUtil.newMessage(paintData))
    }

    private fun onScrollChanged() {
//        NearbyPaintLog.d(TAG, "onScrollChanged")
        reduceThicknessButton.forceStop()
        increaseThicknessButton.forceStop()
    }

    private fun clear() {
        paintView.clearList()
        val message = GsonUtil.newMessage(PaintData(1))
        nearbyUseCase.publish(message)
    }

    private fun increaseThickness(value: Int) {
        val thickness = paintView.addThickness(value)
        thicknessNumberText.setDisappearText(thickness.toString())
    }

    private fun commitThickness() {
        val thickness = Integer.parseInt(thicknessNumberText.text.toString())
        SharedPreferencesManager().putThickness(activity as Context, thickness)
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
package com.ys.nearbypaint.presentation.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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


class MainFragment : Fragment(), NearbyUseCase.NearbySubscribeListener, PaintView.OnDrawEnd, MainActivity.OnRequestPermissionsResultListener {

    private val TAG : String = javaClass.simpleName
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
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

    private val buttons = mutableListOf<ImageView>()

    private val clickListener = View.OnClickListener {
        when(it.id) {
            R.id.clear_button -> clear()
            R.id.increase_thickness_button -> {
                paintView.setElementMode(ElementMode.MODE_LINE)
                increaseThickness(1)
            }
            R.id.reduce_thickness_button -> {
                paintView.setElementMode(ElementMode.MODE_LINE)
                increaseThickness(-1)
            }
            R.id.square_button -> paintView.setElementMode(ElementMode.MODE_STAMP_SQUARE)
            R.id.rectangle_button -> paintView.setElementMode(ElementMode.MODE_STAMP_RECTANGLE)
            R.id.eraser_button -> paintView.setElementMode(ElementMode.MODE_ERASER)
            R.id.save_button -> {
                if (isWriteExternalStoragePermission()) {
                    save()
                } else {
                    requestPermission()
                }
            }
        }
        when(it.id) {
            R.id.clear_button,
            R.id.save_button -> {} //do nothing
            else -> {
                for (b in buttons) {
                    b.background = ResourcesCompat.getDrawable(resources, android.R.color.transparent, null)
                }
                it.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_back_round_shape, null)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        val view =inflater.inflate(R.layout.fragment_main, container, false)

        nearbyUseCase = NearbyUseCase(activity as MainActivity, this)

        paintView = view.findViewById(R.id.paint_view) as PaintView
        paintView.setListener(this)

        val clearButton =  view.findViewById<ImageView>(R.id.clear_button)
        setClickListener(clearButton)

        val increaseThicknessButton =  view.findViewById<ImageView>(R.id.increase_thickness_button)
        setClickListener(increaseThicknessButton)
        increaseThicknessButton.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_back_round_shape, null)

        val reduceThicknessButton =  view.findViewById<ImageView>(R.id.reduce_thickness_button)
        setClickListener(reduceThicknessButton)

        val thickness = SharedPreferencesManager().readThickness(activity as Context)
        thicknessNumberText = view.findViewById(R.id.thickness_number_text)
        paintView.setThickness(thickness)
        thicknessNumberText.setDisappearText(thickness.toString())

        val squareButton = view.findViewById<ImageView>(R.id.square_button)
        setClickListener(squareButton)

        val rectangleButton = view.findViewById<ImageView>(R.id.rectangle_button)
        setClickListener(rectangleButton)

        val eraserButton = view.findViewById<ImageView>(R.id.eraser_button)
        setClickListener(eraserButton)

        val saveButton =  view.findViewById(R.id.save_button) as ImageView
        setClickListener(saveButton)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
    }

    override fun onStart() {
//        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
        super.onStart()
        nearbyUseCase.connect()
    }

    override fun onStop() {
//        NearbyPaintLog.d(TAG, object : Any() {}.javaClass.enclosingMethod.name)
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

        nearbyUseCase.publish(GsonUtil.newMessage(paintData))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    save()
                } else {
                    Toast.makeText(
                        activity?.applicationContext,
                        getString(R.string.message_capture_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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

    private fun setClickListener(button: ImageView) {
        button.setOnClickListener(clickListener)
        buttons.add(button)
    }

    private fun isWriteExternalStoragePermission(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    activity as Context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
            return false
        }
        return true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

}
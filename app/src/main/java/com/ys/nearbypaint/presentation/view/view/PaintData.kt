package com.ys.nearbypaint.presentation.view.view

import android.graphics.Color
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * PaintData Dto.
 */
class PaintData : Parcelable {

    @SerializedName("canvasWidth")
    val canvasWidth: Int

    @SerializedName("canvasHeight")
    val canvasHeight: Int

    @SerializedName("clearFlg")
    val clearFlg: Int

    @SerializedName("elementMode")
    val elementMode: Int

    @SerializedName("points")
    var points: List<PointF> = ArrayList()

    @SerializedName("thickness")
    val thickness: Int

//    @SerializedName("color")
//    val color: Int

    @SerializedName("red")
    val red: Int

    @SerializedName("green")
    val green: Int

    @SerializedName("blue")
    val blue: Int

    @SerializedName("alpha")
    val alpha: Int

    constructor(width: Int, height: Int, elementMode: Int, points: List<PointF>, thickness: Int, color: Int) {
        this.canvasWidth = width
        this.canvasHeight = height
        this.clearFlg = 0
        this.elementMode = elementMode
        this.points = points
        this.thickness = thickness

//        this.color = color
        this.red = Color.red(color)
        this.green = Color.green(color)
        this.blue = Color.blue(color)
        this.alpha = Color.alpha(color)
    }

    constructor(eraser: Int) {
        this.canvasWidth = 0
        this.canvasHeight = 0
        this.clearFlg = eraser
        this.elementMode = 0
        this.points = ArrayList()
        this.thickness = 0
//        this.color = 0
        this.red = 0
        this.green = 0
        this.blue = 0
        this.alpha = 0
    }

    override fun describeContents(): Int {
        return 0
    }

    private constructor(`in`: Parcel) {
        canvasWidth = `in`.readInt()
        canvasHeight = `in`.readInt()
        clearFlg = `in`.readInt()
        elementMode = `in`.readInt()
        arrayListOf<PointF>().apply {
            `in`.readList(this, PointF::class.java.classLoader)
        }
        thickness = `in`.readInt()
//        color = `in`.readInt()
        red = `in`.readInt()
        green = `in`.readInt()
        blue = `in`.readInt()
        alpha = `in`.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(canvasWidth)
        out.writeInt(canvasHeight)
        out.writeInt(clearFlg)
        out.writeInt(elementMode)
        out.writeList(points)
        out.writeInt(thickness)
//        out.writeInt(color)
        out.writeInt(red)
        out.writeInt(green)
        out.writeInt(blue)
        out.writeInt(alpha)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PaintData> = object : Parcelable.Creator<PaintData> {
            override fun createFromParcel(`in`: Parcel): PaintData {
                return PaintData(`in`)
            }

            override fun newArray(size: Int): Array<PaintData?> {
                return arrayOfNulls(size)
            }
        }
    }

    fun color(): Int {
        return Color.argb(alpha, red, green, blue)
    }

}

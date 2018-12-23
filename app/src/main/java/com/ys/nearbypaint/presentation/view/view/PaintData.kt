package com.ys.nearbypaint.presentation.view.view

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

    @SerializedName("points")
    var points: List<PointF> = ArrayList()

    @SerializedName("eraserFlg")
    val eraserFlg: Int

    @SerializedName("thickness")
    val thickness: Int

    @SerializedName("color")
    val color: Int

    constructor(width: Int, height: Int, points: List<PointF>, thickness: Int, color: Int) {
        this.canvasWidth = width
        this.canvasHeight = height
        this.points = points
        this.eraserFlg = 0
        this.thickness = thickness
        this.color = color
    }

    constructor(eraser: Int) {
        this.canvasWidth = 0
        this.canvasHeight = 0
        this.points = ArrayList()
        this.eraserFlg = eraser
        this.thickness = 0
        this.color = 0
    }

    override fun describeContents(): Int {
        return 0
    }

    private constructor(`in`: Parcel) {
        canvasWidth = `in`.readInt()
        canvasHeight = `in`.readInt()
        arrayListOf<PointF>().apply {
            `in`.readList(this, PointF::class.java.classLoader)
        }

        eraserFlg = `in`.readInt()
        thickness = `in`.readInt()
        color = `in`.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(canvasWidth)
        out.writeInt(canvasHeight)
        out.writeList(points)
        out.writeInt(eraserFlg)
        out.writeInt(thickness)
        out.writeInt(color)
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

}

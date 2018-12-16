package com.ys.nearbypaint.presentation.view.view

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

/**
 * PaintData Dto.
 */
class PaintData : Parcelable {

    @SerializedName("canvasWidth")
    var canvasWidth: Int = 0

    @SerializedName("canvasHeight")
    var canvasHeight: Int = 0

    @SerializedName("points")
    lateinit var points: List<PointF>

    @SerializedName("eraserFlg")
    var eraserFlg = 0

    constructor() {

    }

    constructor(width: Int, height: Int, points: List<PointF>) {
        this.canvasWidth = width
        this.canvasHeight = height
        this.points = points
    }

    constructor(eraser: Int) {
        this.canvasWidth = 0
        this.canvasHeight = 0
        this.points = ArrayList()
        this.eraserFlg = eraser
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
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(canvasWidth)
        out.writeInt(canvasHeight)
        out.writeList(points)
        out.writeInt(eraserFlg)
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

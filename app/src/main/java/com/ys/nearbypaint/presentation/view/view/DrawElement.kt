package com.ys.nearbypaint.presentation.view.view

import android.graphics.Paint
import android.graphics.Path

/**
 * DrawElement.
 */
class DrawElement(brushColor: Int, thickness: Int, stroke: Boolean) {

    val path = Path()
    val paint = Paint()

    init {
        paint.color = brushColor
        paint.isAntiAlias = true
        paint.style = if (stroke) Paint.Style.STROKE else Paint.Style.FILL
        paint.strokeWidth = thickness.toFloat()
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
    }

}
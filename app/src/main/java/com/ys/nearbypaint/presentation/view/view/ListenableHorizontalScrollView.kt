package com.ys.nearbypaint.presentation.view.view

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class ListenableHorizontalScrollView(context: Context?, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {

    interface OnScrollChangeListener {
        fun onScrollChange()
    }

    private var listener: OnScrollChangeListener? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        this.listener?.onScrollChange()
    }

    fun setOnScrollChangeListener(listener: OnScrollChangeListener) {
        this.listener = listener
    }

}
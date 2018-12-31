package com.ys.nearbypaint.presentation.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.ys.nearbypaint.R


class DisappearTextView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
    }

    fun setDisappearText(text: String) {
        this.text = text
        fadeout()
    }

    private fun fadeout() {
        alpha = 1.0f
        val animation = AnimationUtils.loadAnimation(
            context,
            R.anim.alpha_fadeout
        )
        startAnimation(animation)
    }
}
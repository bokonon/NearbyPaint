package com.ys.nearbypaint.presentation.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ys.nearbypaint.R

class MainActivity : AppCompatActivity() {

    interface OnActivityWindowFocusChangedListener {
        fun onActivityWindowFocusChanged(hasFocus: Boolean) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val fragments = supportFragmentManager.fragments
        for (f in fragments) {
            if (f is OnActivityWindowFocusChangedListener) {
                f.onActivityWindowFocusChanged(hasFocus)
            }
        }
    }
}

package com.ys.nearbypaint.presentation.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ys.nearbypaint.R

class MainActivity : AppCompatActivity() {

    interface OnRequestPermissionsResultListener {
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is OnRequestPermissionsResultListener) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

}

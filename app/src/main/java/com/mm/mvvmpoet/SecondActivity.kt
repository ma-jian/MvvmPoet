package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

/**
 * Created by : majian
 * Date : 4/16/21
 * Describe :
 */
class SecondActivity : Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        Log.e("majian", "SecondActivity density :${resources?.displayMetrics?.density} ; densityDpi :${resources?.displayMetrics?.densityDpi} ; scaledDensity :${resources?.displayMetrics?.scaledDensity} ")
        findViewById<TextView>(R.id.textView).text = "density :${resources?.displayMetrics?.density} ; densityDpi :${resources?.displayMetrics?.densityDpi} ; scaledDensity :${resources?.displayMetrics?.scaledDensity}"
    }
}
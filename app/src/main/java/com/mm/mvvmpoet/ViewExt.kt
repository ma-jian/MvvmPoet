package com.mm.mvvmpoet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay

/**
 * View 扩展
 */

/**
 * measureView
 */
fun View.measureSpec() = apply {
    measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
    layout(0, 0, measuredWidth, measuredHeight)
}

/**
 * View to Bitmap
 */
fun View.covertBitmap(): Bitmap = measureSpec().run {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    val c = Canvas(bitmap)
    c.drawColor(Color.WHITE)
    draw(c)
    bitmap
}

/**
 * 点击防重
 * @param time 防重时间
 */
fun View.setOneClickListener(time: Long = 500, block: (View) -> Unit) {
    val actor = GlobalScope.actor<View>(Dispatchers.Main) {
        for (ch in channel) {
            block(ch)
            delay(time)
        }
    }
    setOnClickListener {
        actor.offer(it)
    }
}
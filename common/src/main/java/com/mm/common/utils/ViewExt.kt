@file:JvmName("ViewExt")
package com.mm.common.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import java.io.File
import java.io.FileOutputStream


/**
 * View 扩展
 */

/**
 * measureView
 */
fun View.measureSpec() = apply {
    measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
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
@OptIn(DelicateCoroutinesApi::class, ObsoleteCoroutinesApi::class)
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
fun View.setOneClickListener(time: Long = 500, block: (View) -> Unit) {
    val actor = GlobalScope.actor<View>(Dispatchers.Main) {
        for (ch in channel) {
            block(ch)
            delay(time)
        }
    }
    setOnClickListener {
        actor.trySend(it).isSuccess
    }
}

/**
 * Bitmap Save
 * @param fileName 文件名 .jpg .jpeg
 * @param filePath 文件地址
 * @param cacheFile 返回保存后文件
 */
@Suppress("DEPRECATION")
inline fun Bitmap.saveToFile(fileName: String, filePath: File, cacheFile: (File) -> Unit) {
    this.also {
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val cache = File(filePath.absolutePath, fileName)
        if (cache.exists()) {
            cacheFile.invoke(cache)
            return@also
        }
        val outputStream = FileOutputStream(cache)
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        } else if (fileName.endsWith(".png")) {
            compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, outputStream)
            } else {
                compress(Bitmap.CompressFormat.WEBP, 90, outputStream)
            }
        }
        outputStream.flush()
        outputStream.close()
        cacheFile.invoke(cache)
    }
}

/**
 * 判定当前View是否被点击
 */
fun View.viewIsTouch(ev: MotionEvent): Boolean {
    val l = intArrayOf(0, 0)
    this.getLocationInWindow(l)
    val left = l[0]
    val top = l[1]
    val bottom = top + height
    val right = left + width
    return (ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
}

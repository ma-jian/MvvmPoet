@file:JvmName("StringKt")

package com.mm.common.utils

import java.text.DecimalFormat
import java.util.*

/**
 * 转换文件大小
 *
 * @param fileS
 * @return
 */
fun Long?.formatFileSize(): String {
    return this?.let {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (it == 0L) {
            return wrongSize
        }
        fileSizeString = if (it < 1024) {
            df.format(it.toDouble()) + "B"
        } else if (it < 1048576) {
            df.format(it.toDouble() / 1024) + "KB"
        } else if (it < 1073741824) {
            df.format(it.toDouble() / 1048576) + "MB"
        } else {
            df.format(it.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    } ?: "0B"
}

/**
 * 转换文件大小
 *
 * @param fileS
 * @return
 */
fun Double?.formatFileSize(): String {
    return this?.let {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (it == 0.0) {
            return wrongSize
        }
        fileSizeString = if (it < 1024) {
            df.format(it) + "B"
        } else if (it < 1048576) {
            df.format(it / 1024) + "KB"
        } else if (it < 1073741824) {
            df.format(it / 1048576) + "MB"
        } else {
            df.format(it / 1073741824) + "GB"
        }
        return fileSizeString
    } ?: "0B"

}

/**
 * 转换 mb 文件大小
 *
 * @param size
 * @return
 */
fun String?.reverseSize(): Long {
    return this?.let {
        if (it.lowercase(Locale.getDefault()).contains("mb")) {
            val mb = it.lowercase(Locale.getDefault()).replace("mb", "")
            val si = mb.toDouble()
            return (si * 1048576).toLong()
        }
        return 200 * 1048576L
    } ?: 0L
}
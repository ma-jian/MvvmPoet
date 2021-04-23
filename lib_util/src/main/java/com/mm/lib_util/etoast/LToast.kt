package com.mm.lib_util.etoast

import android.text.TextUtils
import android.util.Log

/**
 * Created by : majian
 * Date : 4/13/21
 * Describe :
 */
object LToast {
    internal var isDebug = true
    private val TAG = "LToast"
    fun i(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.i(TAG, "$msg")
    }

    fun d(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.d(TAG, "$msg")
    }

    fun e(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.e(TAG, "$msg")
    }

    fun d(tag: String, msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.i(tag, "$msg")
    }
}
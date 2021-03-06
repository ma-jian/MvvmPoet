package com.mm.lib_http

import android.text.TextUtils
import android.util.Log

/**
 * Created by : majian
 * Date : 4/13/21
 * Describe :
 */

internal object LHttp {
    internal var isDebug = false
    fun i(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.i("LHttp: ", "$msg")
    }

    fun d(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.d("LHttp: ", "$msg")
    }

    fun e(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.e("LHttp: ", "$msg")
    }

    fun d(tag: String, msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) Log.i(tag, "$msg")
    }
}
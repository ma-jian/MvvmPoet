package com.mm.common.crash

import android.app.Instrumentation

/**
 * Date : 2023/3/23
 * @since 1.0
 */
class InstrumentationProxy : Instrumentation() {

    override fun onException(obj: Any, e: Throwable): Boolean {
        try {
            return true
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return super.onException(obj, e)
    }
}
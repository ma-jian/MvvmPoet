package com.mm.common.utils

import android.text.TextUtils
import android.util.Log
import com.mm.common.BuildConfig

/**
 * @since 1.0
 * 日志工具
 */
object LogUtil {
    var isDebug = BuildConfig.BUILD_TYPE == "debug"
    var mTag = "LogUtil"

    //for info log
    @JvmStatic
    fun i(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.i(mTag, msg!!)
        }
    }

    //for debug log
    @JvmStatic
    fun d(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.d(mTag, msg!!)
        }
    }

    //for warming log
    @JvmStatic
    fun w(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.w(mTag, msg!!)
        }
    }

    //for error log
    @JvmStatic
    fun e(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.e(mTag, msg!!)
        }
    }


    //for verbose log
    @JvmStatic
    fun v(msg: String?) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.v(mTag, msg!!)
        }
    }

    //for info log
    @JvmStatic
    fun i(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) {
                t = mTag
            }
            Log.i(t, msg!!)
        }
    }

    //for debug log
    @JvmStatic
    fun d(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) {
                t = mTag
            }
            Log.d(t, msg!!)
        }
    }

    //for verbose log
    @JvmStatic
    fun v(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) {
                t = mTag
            }
            Log.v(t, msg!!)
        }
    }

    //for warming log
    @JvmStatic
    fun w(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) {
                t = mTag
            }
            Log.w(t, msg!!)
        }
    }

    //for verbose log
    @JvmStatic
    fun e(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) {
                t = mTag
            }
            Log.e(t, msg!!)
        }
    }

    /**
     * 点击Log跳转到指定源码位置
     *
     * @param tag
     * @param msg
     */
    @JvmStatic
    fun showLog(tag: String?, msg: String?) {
        var t = tag
        if (isDebug && !TextUtils.isEmpty(msg)) {
            if (TextUtils.isEmpty(t)) t = mTag
            val stackTraceElement = Thread.currentThread().stackTrace
            var currentIndex = -1
            for (i in stackTraceElement.indices) {
                if (stackTraceElement[i].methodName.compareTo("showLog") == 0) {
                    currentIndex = i + 1
                    break
                }
            }
            if (currentIndex >= 0) {
                val fullClassName = stackTraceElement[currentIndex].className
                val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
                val methodName = stackTraceElement[currentIndex].methodName
                val lineNumber = stackTraceElement[currentIndex].lineNumber.toString()
                Log.i(t, "$msg---->at $className.$methodName($className.java:$lineNumber)")
            } else {
                Log.i(t, msg!!)
            }
        }
    }
}
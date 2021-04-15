package com.mm.lib_http

import android.text.TextUtils
import okhttp3.HttpUrl


/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

object HostGlobal {
    var dynamicHostMap = hashMapOf<String, HashMap<String, String>>()
    val dynamicOriginalHostMap = hashMapOf<String, HostInfo>()
}

class HostInfo private constructor(private val builder: Builder) {
    internal val dynamicHost: Boolean
        get() = builder.dynamicHost

    @Volatile
    internal var hostKey: String = ""
        get() {
            val stringBuilder = StringBuilder()
            if (dynamicHost) {
                stringBuilder.append("dynamicHost")
                stringBuilder.append("_")
            }
            val str = if (needSystemParam) "1" else "0"
            stringBuilder.append(str)
            stringBuilder.append("_")
            stringBuilder.append(signMethod)
            return stringBuilder.toString()
        }
    private val needSystemParam
        get() = builder.needSystemParam

    private val signMethod
        get() = builder.signMethod

    internal val srcHost: String
        get() = builder.srcHost

    companion object {
        fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        internal var dynamicHost = false
        internal var needSystemParam = false
        internal var signMethod = 0
        internal var srcHost: String = ""

        fun srcHost(srcHost: String?) = apply {
            this.srcHost = srcHost ?: ""
        }

        fun dynamicHost(dynamicHost: Boolean) = apply {
            this.dynamicHost = dynamicHost
        }

        fun needSystemParam(needSystemParam: Boolean) = apply {
            this.needSystemParam = needSystemParam
        }

        fun signMethod(signMethod: Int) = apply {
            this.signMethod = signMethod
        }

        fun build() = HostInfo(this)
    }
}

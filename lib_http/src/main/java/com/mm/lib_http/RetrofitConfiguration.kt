package com.mm.lib_http

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe : retrofit 配置
 */

open class RetrofitConfiguration private constructor(private val builder: Builder) {
    val mInterceptors: List<Interceptor>
        get() = builder.interceptors
    val mNetworkInterceptors: List<Interceptor>
        get() = builder.networkInterceptors
    val mFilterHost: List<String>
        get() = builder.filterHost
    val mCookieName: String
        get() = builder.cookieName
    val mDynamicHostUrl: String
        get() = builder.dynamicHostUrl
    val mLevel: HttpLoggingInterceptor.Level
        get() = builder.level

    companion object {
        fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
        val DEFAULT = build {}
    }

    class Builder constructor() {
        internal var interceptors: List<Interceptor> = emptyList()
        internal var networkInterceptors: List<Interceptor> = emptyList()
        internal var filterHost: MutableList<String> = ArrayList()
        internal var cookieName: String = "cookie"
        internal var dynamicHostUrl: String = ""
        internal var level = HttpLoggingInterceptor.Level.NONE

        internal constructor(retrofitConfiguration: RetrofitConfiguration) : this() {
            this.interceptors = retrofitConfiguration.mInterceptors
            this.networkInterceptors = retrofitConfiguration.mNetworkInterceptors
            this.filterHost = retrofitConfiguration.mFilterHost.toMutableList()
            this.cookieName = retrofitConfiguration.mCookieName
            this.dynamicHostUrl = retrofitConfiguration.mDynamicHostUrl
            this.level = retrofitConfiguration.mLevel
        }

        fun build(): RetrofitConfiguration = RetrofitConfiguration(this)

        fun interceptors(interceptors: () -> List<Interceptor>) = apply {
            this.interceptors = interceptors.invoke()
        }

        fun networkInterceptors(interceptors: () -> List<Interceptor>) = apply {
            this.networkInterceptors = interceptors.invoke()
        }

        fun filterHost(hosts: () -> List<String>) = apply {
            this.filterHost = hosts.invoke().toMutableList()
        }

        fun cookieName(name: () -> String) = apply {
            this.cookieName = name.invoke()
        }

        fun dynamicHost(url: () -> String) = apply {
            this.dynamicHostUrl = url.invoke()
        }

        fun httpLogLevel(level: () -> HttpLoggingInterceptor.Level) = apply {
            this.level = level.invoke()
        }
    }
}
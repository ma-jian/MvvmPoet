package com.mm.lib_http

import okhttp3.Interceptor


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe : retrofit 配置
 */

open class RetrofitConfiguration private constructor(private val builder: Builder) {
    val mEnv: Int
        get() = builder.env
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

    companion object {
        fun build(block :Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder constructor() {
        internal var env = Env.RELEASE
        internal var interceptors: List<Interceptor> = emptyList()
        internal var networkInterceptors: List<Interceptor> = emptyList()
        internal var filterHost: MutableList<String> = ArrayList()
        internal var cookieName: String = "cookie"
        internal var dynamicHostUrl: String = ""

       internal constructor(retrofitConfiguration: RetrofitConfiguration) : this() {
           this.env = retrofitConfiguration.mEnv
           this.interceptors = retrofitConfiguration.mInterceptors
           this.networkInterceptors = retrofitConfiguration.mNetworkInterceptors
           this.filterHost = retrofitConfiguration.mFilterHost.toMutableList()
           this.cookieName = retrofitConfiguration.mCookieName
           this.dynamicHostUrl = retrofitConfiguration.mDynamicHostUrl
       }

        fun build(): RetrofitConfiguration = RetrofitConfiguration(this)

        fun env(env: Int): Builder  = apply {
            if (env == Env.RELEASE || env == Env.PRE_RELEASE || env == Env.DEBUG) {
                this.env = env
            }
        }

        fun interceptors(interceptors: List<Interceptor>) = apply {
            this.interceptors = interceptors
        }

        fun networkInterceptors(interceptors: List<Interceptor>) =apply {
            this.networkInterceptors = interceptors
        }

        fun filterHost(hosts: List<String>) = apply {
            this.filterHost = hosts.toMutableList()
        }

        fun cookieName(name: String) = apply {
            this.cookieName = name
        }

        fun dynamicHost(url: String) = apply {
            this.dynamicHostUrl = url
        }
    }
}
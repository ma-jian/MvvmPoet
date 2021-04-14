package com.mm.lib_http

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.Response


/**
 * Created by : majian
 * Date : 4/14/21
 * Describe : 动态域名
 */

class DynamicHostInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val scheme = request.url.scheme
        val host = request.url.host
        HostGlobal.dynamicOriginalHostMap[host]?.let {
            if (!TextUtils.isEmpty(it.dynamicHostKey)) {
                RetrofitGlobal.rwLock.readLock().lock()
                try {
                    val str = it.dynamicHostKey
                    HostGlobal.dynamicHostMap[str]?.let { url ->
                        val bool = url.host == host && url.scheme == scheme
                        if (!bool) {
                            val httpUrl = request.url.newBuilder().scheme(url.scheme).host(url.host)
                                .port(url.port).build()
                            return chain.proceed(request.newBuilder().url(httpUrl).build())
                        }
                    }
                } finally {
                    RetrofitGlobal.rwLock.readLock().unlock()
                }
            }

            if (!TextUtils.isEmpty(it.srcHost)) {
                val httpUrl1 = request.url.newBuilder().host(it.srcHost).build()
                return chain.proceed(request.newBuilder().url(httpUrl1).build())
            }
        }
        return chain.proceed(request)
    }
}
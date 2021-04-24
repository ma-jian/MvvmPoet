package com.mm.lib_http

import android.net.Uri
import android.text.TextUtils
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response


/**
 * Created by : majian
 * Date : 4/14/21
 * Describe : 动态域名下发；未指定域名支持下配置 host Type 0: api + h5 1: 仅api 2 :仅h5
 * @see WorkerRunnable 预加载已下发域名
 */

internal class DynamicHostInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val scheme = request.url.scheme
        val host = request.url.host
        HostGlobal.dynamicOriginalHostMap[host]?.let {
            if (it.dynamicHost) {
                RetrofitGlobal.rwLock.readLock().lock()
                try {
                    val auth = request.url.toUri().authority
                    val newHost = findHost(auth).toHttpUrl()
                    val bool = newHost.host == host && newHost.scheme == scheme
                    if (!bool) {
                        val httpUrl = request.url.newBuilder().scheme(newHost.scheme).host(newHost.host)
                            .port(newHost.port).build()
                        return chain.proceed(request.newBuilder().url(httpUrl).build())
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

    /**
     * 域名替换支持类型
     * @type 0: api + h5 1: 仅api 2 :仅h5
     */
    private fun findHost(host: String) = run {
        val key = HostGlobal.dynamicHostMap.keys.find { key -> Uri.parse(key).authority == host }
        HostGlobal.dynamicHostMap[key]?.let {
            val type = it.keys.first()
            if (type == "1" || type == "0") {
                it[type]
            } else host
        } ?: host
    }
}
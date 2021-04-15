package com.mm.lib_http

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.TimeUnit


/**
 * Created by : majian
 * Date : 4/14/21
 * Describe : 超时拦截
 */

class TimeOutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        request.tag(Invocation::class.java)?.run {
            method().getAnnotation(TimeOut::class.java)?.let {
                val newChain = chain.run {
                    if (it.CONNECT_TIMEOUT > 0) withConnectTimeout(it.CONNECT_TIMEOUT, it.unit) else this
                }.run {
                    if (it.READ_TIMEOUT > 0) withReadTimeout(it.READ_TIMEOUT, it.unit) else this
                }.run {
                    if (it.WRITE_TIMEOUT > 0) withWriteTimeout(it.WRITE_TIMEOUT, it.unit) else this
                }
                return newChain.proceed(request.newBuilder().build())
            }
        }
        return chain.proceed(request.newBuilder().build())
    }
}
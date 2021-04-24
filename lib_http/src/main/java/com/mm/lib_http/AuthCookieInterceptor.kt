package com.mm.lib_http

import android.text.TextUtils
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response


/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

internal class AuthCookieInterceptor constructor(private val authCookieJar: AuthCookieJar) :Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        authCookieJar.loadForRequest(request.url)?.let {
            val header = cookieHeader(it)
            val cookie = request.header("Cookie")
            var str1 = header
            if (!TextUtils.isEmpty(cookie)) {
                val stringBuilder = StringBuilder()
                stringBuilder.append(header)
                stringBuilder.append("; ")
                stringBuilder.append(cookie)
                str1 = stringBuilder.toString()
            }
            builder.header("Cookie", str1)
        }
        val response: Response = chain.proceed(builder.build())
        authCookieJar.saveFromResponse(response)
        return response
    }

    private fun cookieHeader(paramCookie: Cookie): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(paramCookie.name)
        stringBuilder.append('=')
        stringBuilder.append(paramCookie.value)
        return stringBuilder.toString()
    }
}
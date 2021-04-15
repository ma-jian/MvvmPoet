package com.mm.lib_http

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.Response


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe :
 */

class AuthCookieJar internal constructor(private val context: Context, private val configuration: RetrofitConfiguration) {
    private val KEY_NAME_AUTH = "auth_value"
    private val KEY_NAME_AUTH_EXPIRED = "auth_expired"
    private val PREF_NAME = "auth_cookie_pref"
    private val cookiePreferences: SharedPreferences by lazy<SharedPreferences> {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    @Volatile
    private var cookie: Cookie? = null
        get() {
            val str = cookiePreferences.getString(KEY_NAME_AUTH, null)
            val l = cookiePreferences.getLong(KEY_NAME_AUTH_EXPIRED, 0L)
            return if (str == null) null else Cookie.Builder().name(configuration.mCookieName)
                .path("/").secure().httpOnly().value(str).expiresAt(l).build()
        }

    private fun isCookieExpired(cookie: Cookie?): Boolean =
        (cookie?.expiresAt ?: System.currentTimeMillis()) < System.currentTimeMillis()

    fun getAuthCookieString(): String {
        return if (hasValidityCookie()) cookie!!.value else ""
    }

    fun hasValidityCookie(): Boolean =
        cookie != null && !isCookieExpired(cookie) && !TextUtils.isEmpty(cookie!!.value)

    fun loadForRequest(httpUrl: HttpUrl): Cookie? {
        return if (isUserHost(httpUrl.host) && hasValidityCookie()) cookie else null
    }

    private fun isUserHost(host: String): Boolean =
        configuration.mFilterHost.isNotEmpty() && !configuration.mFilterHost.find { it.toLowerCase().endsWith(host, true) }.isNullOrEmpty()

    fun saveFromResponse(response: Response) {
        val httpUrl = response.request.url
        val list = Cookie.parseAll(httpUrl, response.headers)
        for (cookie in list) {
            if (isUserHost(httpUrl.host) && configuration.mCookieName == cookie.name) {
                if (!TextUtils.isEmpty(cookie.value) && "deleted" != cookie.value) {
                    this.cookie = cookie
                    save(cookie)
                } else {
                    this.cookie = null
                    clear()
                }
            }
        }
        if (LHttp.isDebug) {
            var stringBuilder = StringBuilder()
            stringBuilder.append("存入的 cookie : ")
            stringBuilder.append(list.size)
            LHttp.i(stringBuilder.toString())
            for (cookie in list) {
                stringBuilder = StringBuilder()
                stringBuilder.append(cookie.name)
                stringBuilder.append(" : ")
                stringBuilder.append(cookie.value)
                LHttp.i(stringBuilder.toString())
            }
        }
    }

    private fun save(cookie: Cookie) {
        val edit = cookiePreferences.edit()
        edit.putString(KEY_NAME_AUTH, cookie.value)
        edit.putLong(KEY_NAME_AUTH_EXPIRED, cookie.expiresAt)
        edit.apply()
    }

    private fun clear() = cookiePreferences.edit().clear()

}
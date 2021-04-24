package com.mm.lib_http

import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.*
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe : 获取动态host
 * @sample DynamicHostInterceptor host type 0: api + h5 1: 仅api 2 :仅h5
 * {
 *   code: 1,
 *   data: {
 *     "http://api.github.com/": {
 *         "0": "http://api.github.com/",
 *         "1": "http://api.github.com/",
 *         "2": "http://api.github.com/"
 *      }
 *    }
 * }
 */
internal class WorkerRunnable internal constructor(private val authCookieJar: AuthCookieJar, private val configuration: RetrofitConfiguration) : Runnable {
    private val baseHost
        get() = configuration.mDynamicHostUrl

    private fun loadCache(file: File): Boolean {
        if (file.exists()) {
            val bufferedSource: BufferedSource = file.source().buffer()
            bufferedSource.use {
                val type = object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type
                val json = RetrofitGlobal.gson.fromJson<HashMap<String, HashMap<String, String>>>(it.readUtf8(), type)
                HostGlobal.dynamicHostMap.putAll(json)
            }
        }
        return false
    }

    override fun run() {
        if (TextUtils.isEmpty(baseHost)) return
        if (!RetrofitGlobal.rwLock.isWriteLocked && RetrofitGlobal.rwLock.writeLock().tryLock()) {
            try {
                val builder = OkHttpClient.Builder()
                val authCookieInterceptor = AuthCookieInterceptor(authCookieJar)
                builder.addInterceptor(authCookieInterceptor)
                configuration.mInterceptors.forEach {
                    builder.addInterceptor(it)
                }
                val okHttpClient = builder.connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(10L, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
                val bool = authCookieJar.hasValidityCookie()
                if (!bool) {
                    val httpUrl = baseHost.toHttpUrl().newBuilder()
                        .addQueryParameter("appVersion", "1")
                        .addQueryParameter("openUDID", UUID.randomUUID().toString())
                        .addQueryParameter("os", "Android")
                        .addQueryParameter("systemVersion", Build.VERSION.RELEASE)
                        .addQueryParameter("model", Build.MODEL).build()
                    okHttpClient.newCall(Request.Builder().url(httpUrl).build()).execute().close()
                }
                val cache = RetrofitGlobal.appContext.cacheDir
                val file = File(cache, "hosts")
                val file2 = File("${file.absolutePath}${File.separator}cache.txt")
                val parentFile = file2.parentFile
                parentFile?.let {
                    if (!it.exists()) it.mkdirs()
                }
                if (!file2.exists()) {
                    file2.createNewFile()
                }
                val httpUrl = baseHost.toHttpUrl().newBuilder().addQueryParameter("appVersion", "1")
                    .addQueryParameter("openUDID", UUID.randomUUID().toString())
                    .addQueryParameter("os", "Android")
                    .addQueryParameter("systemVersion", Build.VERSION.RELEASE)
                    .addQueryParameter("model", Build.MODEL).build()
                val response = okHttpClient.newCall(Request.Builder().url(httpUrl).build())
                    .execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        val jSONObject = JSONObject(it)
                        if (jSONObject.has("data")) {
                            jSONObject.optJSONObject("data")?.apply {
                                val json = RetrofitGlobal.gson.fromJson(this.toString(), JsonObject::class.java)
                                val type = object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type
                                val host = RetrofitGlobal.gson.fromJson<HashMap<String, HashMap<String, String>>>(json, type)
                                HostGlobal.dynamicHostMap.putAll(host)
                                val bufferedSink: BufferedSink = file2.sink().buffer()
                                bufferedSink.writeUtf8(RetrofitGlobal.gson.toJson(host))
                                bufferedSink.flush()
                                bufferedSink.close()
                            }
                        }
                    }
                } else {
                    loadCache(file2)
                }
            } catch (e: Exception) {
                LHttp.e(Log.getStackTraceString(e))
            } finally {
                RetrofitGlobal.rwLock.writeLock().unlock()
            }
        }
    }
}
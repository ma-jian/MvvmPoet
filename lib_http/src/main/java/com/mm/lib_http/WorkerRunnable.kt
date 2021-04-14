package com.mm.lib_http

import android.os.Build
import android.text.TextUtils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe : 获取动态host
 */

class WorkerRunnable internal constructor(
    val authCookieJar: AuthCookieJar,
    val configuration: RetrofitConfiguration
) : Runnable {
    private val baseHost
        get() = configuration.mDynamicHostUrl

    private fun loadCache(file: File): Boolean {
        if (file.exists()) {
            val bufferedSource: BufferedSource = file.source().buffer()
            try {
                val jSONObject = JSONObject(bufferedSource.readUtf8())
                val iterator = jSONObject.keys()
                var bool = false
                while (true) {
                    if (iterator.hasNext()) {
                        val str = iterator.next()
                        val httpUrl = jSONObject.optString(str).toHttpUrlOrNull()
                        httpUrl?.let {
                            HostGlobal.dynamicHostMap[str] = it
                            bool = true
                        }
                        continue
                    }
                    return bool
                }
            } catch (exception: Exception) {
                LHttp.e(exception.message)
            } finally {
                bufferedSource.close()
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
                    builder.addNetworkInterceptor(it)
                }
                val okHttpClient =
                    builder.connectTimeout(30L, TimeUnit.SECONDS).readTimeout(10L, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true).build()
                var bool = authCookieJar.hasValidityCookie()
                if (!bool) {
                    val httpUrl = baseHost.toHttpUrl().newBuilder()
                        .addQueryParameter("appVersion", "1")
                        .addQueryParameter("openUDID", UUID.randomUUID().toString())
                        .addQueryParameter("os", "Android")
                        .addQueryParameter("systemVersion", Build.VERSION.RELEASE)
                        .addQueryParameter("model", Build.MODEL).build()
                    okHttpClient.newCall(Request.Builder().url(httpUrl).build()).execute().close()
                }
                val i = configuration.mEnv
                if (i != 0) return
                val file1 = RetrofitGlobal.appContext.cacheDir
                val stringBuilder = StringBuilder()
                stringBuilder.append("hosts")
                stringBuilder.append(File.separator)
                stringBuilder.append("cache")
                val file2 = File(file1, stringBuilder.toString())
                if (!file2.exists()) file2.mkdirs()
                bool = loadCache(file2)
                if (bool && RetrofitGlobal.rwLock.isWriteLocked && RetrofitGlobal.rwLock.writeLock().isHeldByCurrentThread) {
                    RetrofitGlobal.rwLock.writeLock().unlock()
                } else {
                    val httpUrl = baseHost.toHttpUrl().newBuilder()
                        .addQueryParameter("appVersion", "1")
                        .addQueryParameter("openUDID", UUID.randomUUID().toString())
                        .addQueryParameter("os", "Android")
                        .addQueryParameter("systemVersion", Build.VERSION.RELEASE)
                        .addQueryParameter("model", Build.MODEL).build()
                    okHttpClient.newCall(Request.Builder().url(httpUrl).build())
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {}
                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    val jSONObject = JSONObject(response.body.toString())
                                    val code = jSONObject.optInt("code")
                                    if (jSONObject.has("data") && code == 1) {
                                        jSONObject.optJSONObject("data")?.also {
                                            val hosts = it.optString("hosts")
                                            if (!bool) {
                                                val hostJson = JSONObject(hosts)
                                                val iterator = hostJson.keys()
                                                while (iterator.hasNext()) {
                                                    val key = iterator.next()
                                                    val nHost = hostJson.optString(key)
                                                    if (!TextUtils.isEmpty(nHost)) {
                                                        nHost.toHttpUrlOrNull()?.let { url ->
                                                            HostGlobal.dynamicHostMap.put(key, url)
                                                        }
                                                        val bufferedSink: BufferedSink =
                                                            file2.sink().buffer()
                                                        bufferedSink.writeUtf8(hosts)
                                                        bufferedSink.flush()
                                                        bufferedSink.close()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }
            } catch (e: Exception) {
                LHttp.e("WorkerRunnable: ${e.message}")
            }
        }
    }
}
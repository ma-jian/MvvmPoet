package com.mm.lib_http

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock


/**
 * Created by : majian
 * Date : 4/13/21
 * Describe : retrofit
 */
object Env {
    const val RELEASE = 0
    const val DEBUG = 1
    const val PRE_RELEASE = 2
}

open class RetrofitGlobal {
    companion object {
        internal val rwLock = ReentrantReadWriteLock()
        private var init = false
        var retrofitBuilder: Retrofit.Builder? = null
        var mEnv = Env.RELEASE
        internal lateinit var appContext: Context

        fun init(context: Context) {
            init(context, RetrofitConfiguration.Builder().build())
        }

        fun init(context: Context, interceptors: List<Interceptor>) {
            val configuration = RetrofitConfiguration.build {
                interceptors(interceptors)
            }
            init(context, configuration)
        }

        fun init(context: Context, configuration: RetrofitConfiguration) {
            this.appContext = context
            this.mEnv = configuration.mEnv
            val authCookieJar = AuthCookieJar(context, configuration)
            val dispatcher = Dispatcher()
            retrofitBuilder ?: createRetrofit(dispatcher, authCookieJar, configuration).apply {
                retrofitBuilder = this
            }
            val workerRunnable = WorkerRunnable(authCookieJar, configuration)
            if (!init) {
                init = true
                dispatcher.executorService.execute(workerRunnable)
            }
        }

        private fun createRetrofit(
            dispatcher: Dispatcher,
            authCookieJar: AuthCookieJar,
            configuration: RetrofitConfiguration
        ): Retrofit.Builder {
            val okHttpBuilder = OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS)
                .connectTimeout(20L, TimeUnit.SECONDS)
            configuration.mInterceptors.forEach {
                okHttpBuilder.addInterceptor(it)
            }
            okHttpBuilder.addInterceptor(DynamicHostInterceptor())
                .addInterceptor(AuthCookieInterceptor(authCookieJar))
                .addInterceptor(TimeOutInterceptor())
            configuration.mNetworkInterceptors.forEach {
                okHttpBuilder.addNetworkInterceptor(it)
            }
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpBuilder.build())
        }

        inline fun <reified T> create(): T? {
            T::class.java.isAnnotation
            val annotation = T::class.java.getAnnotation(HOST::class.java)
            require(annotation != null) {
                "需要指定对应的Host"
            }
            var srcUrl = when (mEnv) {
                Env.DEBUG -> annotation.debugUrl
                Env.PRE_RELEASE -> annotation.preUrl
                else -> annotation.releaseUrl
            }
            if (TextUtils.isEmpty(srcUrl)) {
                srcUrl = annotation.releaseUrl
            }
            val httpUrl = srcUrl.toHttpUrl()
            val hostInfo = HostInfo.build {
                srcHost(httpUrl.host)
                    .dynamicHostKey(annotation.dynamicHostKey)
                    .needSystemParam(annotation.needSystemParam)
                    .signMethod(annotation.signMethod)
            }
            HostGlobal.dynamicOriginalHostMap[httpUrl.host] = hostInfo
            return retrofitBuilder?.baseUrl(httpUrl)?.build()?.create(T::class.java)
        }
    }
}
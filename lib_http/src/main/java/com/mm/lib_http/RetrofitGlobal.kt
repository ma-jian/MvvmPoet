package com.mm.lib_http

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

open class RetrofitGlobal private constructor(val build: Builder) {
    private var init = false
    private var mRetrofitBuilder: Retrofit.Builder
    internal val mEnv
        get() = build.env
    internal val mAppContext: Context
        get() = build.appContext

    init {
        val authCookieJar = AuthCookieJar(build.appContext, build.config.invoke())
        val dispatcher = Dispatcher()
        mRetrofitBuilder = createRetrofit(dispatcher, authCookieJar, build.config.invoke())
        val workerRunnable = WorkerRunnable(authCookieJar, build.config.invoke())
        if (!init) {
            init = true
            dispatcher.executorService.execute(workerRunnable)
        }
    }

    companion object {
        internal val rwLock = ReentrantReadWriteLock()
        internal val gson = Gson()
        private var env = Env.RELEASE
        private lateinit var retrofitBuilder: Retrofit.Builder
        internal lateinit var appContext: Context
        fun build(context: Context, block: Builder.() -> Unit) =
            Builder(context).apply(block).build().apply {
                appContext = context
                env = mEnv
                retrofitBuilder = mRetrofitBuilder
            }

        inline fun <reified T> create(): T = create(T::class.java)

        fun <T> create(clazz: Class<T>): T {
            val annotation = clazz.getAnnotation(HOST::class.java)
            require(annotation != null) { "需要指定对应的Host" }
            var srcUrl = when (env) {
                Env.DEBUG -> annotation.debugUrl
                Env.PRE_RELEASE -> annotation.preUrl
                else -> annotation.releaseUrl
            }
            if (TextUtils.isEmpty(srcUrl)) {
                srcUrl = annotation.releaseUrl
            }
            val httpUrl = srcUrl.toHttpUrl()
            val hostInfo = HostInfo.build {
                srcHost(httpUrl.host).dynamicHost(annotation.dynamicHost)
                    .needSystemParam(annotation.needSystemParam).signMethod(annotation.signMethod)
            }
            HostGlobal.dynamicOriginalHostMap[httpUrl.host] = hostInfo
            require(this::retrofitBuilder.isInitialized) { "在使用create前,请先进行RetrofitGlobal初始化build()" }
            return retrofitBuilder.baseUrl(httpUrl).build().create(clazz)
        }
    }

    private fun createRetrofit(dispatcher: Dispatcher, authCookieJar: AuthCookieJar, configuration: RetrofitConfiguration): Retrofit.Builder {
        val okHttpBuilder = OkHttpClient.Builder().dispatcher(dispatcher)
            .readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS)
            .connectTimeout(20L, TimeUnit.SECONDS)
        configuration.mInterceptors.forEach {
            okHttpBuilder.addInterceptor(it)
        }
        okHttpBuilder.addInterceptor(DynamicHostInterceptor())
            .addInterceptor(AuthCookieInterceptor(authCookieJar))
            .addInterceptor(TimeOutInterceptor()).addInterceptor(HttpLoggingInterceptor().apply {
                level = configuration.mLevel
            })
        configuration.mNetworkInterceptors.forEach {
            okHttpBuilder.addNetworkInterceptor(it)
        }
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .client(okHttpBuilder.build())
    }

    class Builder internal constructor(internal val appContext: Context) {
        var config: () -> RetrofitConfiguration = { RetrofitConfiguration.DEFAULT }
        var isDebug: Boolean = false
            set(value) {
                LHttp.isDebug = value
                field = value
            }
        var env: Int = Env.RELEASE
            set(value) {
                if (value == Env.RELEASE || value == Env.PRE_RELEASE || value == Env.DEBUG) {
                    field = value
                }
            }

        fun build() = RetrofitGlobal(this)
    }
}
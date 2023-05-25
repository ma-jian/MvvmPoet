package com.mm.common.base

import com.mm.common.ActivityDelegate
import com.mm.common.UrlManager
import com.mm.common.http.LogInterceptor
import com.mm.http.DynamicHostInterceptor
import com.mm.http.HOST
import com.mm.http.RetrofitCache
import com.mm.http.cache.CacheHelper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import java.io.File

/**
 * Created by : m
 * @since 1.0
 *
 */

open class BaseRepository {

    val MediaType = "application/json;charset=UTF-8".toMediaType()

    companion object {
        @Volatile
        private var retrofit: RetrofitCache? = null

        /**
         * 默认retrofit 配置
         */
        @JvmStatic
        fun defaultRetrofitProviderFactory(): RetrofitCache {
            return retrofit ?: synchronized(this) {
                retrofit ?: createRetrofit().also {
                    retrofit = it
                }
            }
        }

        @JvmStatic
        fun <T> create(clazz: Class<T>): T {
            return defaultRetrofitProviderFactory().create(clazz)
        }

        /**
         * 用于创建原始retrofit网络配置，不经过缓存逻辑
         */
        @JvmStatic
        fun <T> createService(clazz: Class<T>): T {
            return defaultRetrofitProviderFactory().createService(clazz)
        }

        private fun createRetrofit(): RetrofitCache {
            val cacheDir = ActivityDelegate.delegate.get()?.activity?.cacheDir
            val cacheHelper = CacheHelper(File("$cacheDir/okhttp"), Long.MAX_VALUE)
            return RetrofitCache.Builder().addInterceptor(LogInterceptor()).cache(cacheHelper)
                .addHostInterceptor(object : DynamicHostInterceptor {
                    override fun hostUrl(host: HOST): HttpUrl {
                        return when (host.hostType) {
                            1 -> UrlManager.DOMAIN_URL.toHttpUrl()
                            else -> super.hostUrl(host)
                        }
                    }
                }).build()
        }
    }
}
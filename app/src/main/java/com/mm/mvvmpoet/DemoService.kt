package com.mm.mvvmpoet

import com.mm.http.HOST
import com.mm.http.cache.CacheStrategy
import com.mm.http.cache.StrategyType
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by : m
 * Date : 4/14/21
 * Describe : https://api.github.com/
 */
@HOST(value = "https://api.github.com/", hostType = HOST.NORMAL)
interface DemoService {

    @CacheStrategy(StrategyType.FORCE_NETWORK)
    @GET("users/{user}")
    fun getUser(@Path("user") user: String): Call<Any>

    @CacheStrategy(StrategyType.FORCE_CACHE)
    @GET("users/{user}")
    fun getUserCache(@Path("user") user: String): Call<Any>

    @CacheStrategy(StrategyType.IF_NETWORK_ELSE_CACHE)
    @GET("users/{user}")
    fun getUserNetOrCache(@Path("user") user: String): Call<Any>

    @CacheStrategy(StrategyType.IF_CACHE_ELSE_NETWORK)
    @GET("users/{user}")
    fun getUserCacheOrNet(@Path("user") user: String): Call<Any>

    @CacheStrategy(StrategyType.CACHE_AND_NETWORK)
    @GET("users/{user}")
    fun getUserCacheAndNet(@Path("user") user: String): Call<Any>

}
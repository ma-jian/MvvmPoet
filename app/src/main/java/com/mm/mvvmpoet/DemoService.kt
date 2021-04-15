package com.mm.mvvmpoet

import com.mm.lib_http.HOST
import com.mm.lib_http.TimeOut
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe : https://api.github.com/
 */
@HOST(releaseUrl = "https://api.github.com/", dynamicHost = true)
interface DemoService {
    @TimeOut(CONNECT_TIMEOUT = 2)
    @GET("users/{user}")
    fun getUser(@Path("user") user: String): Call<Any>
}
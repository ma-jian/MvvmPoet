package com.mm.mvvmpoet

import android.app.Application
import com.mm.lib_http.RetrofitConfiguration
import com.mm.lib_http.RetrofitGlobal
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = RetrofitConfiguration.build {
            interceptors(listOf(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }))
        }
        RetrofitGlobal.init(this,configuration)
    }
}
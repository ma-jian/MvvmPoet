package com.mm.mvvmpoet

import android.app.Application
import com.mm.lib_http.Env
import com.mm.lib_http.RetrofitConfiguration
import com.mm.lib_http.RetrofitGlobal
import com.mm.lib_util.FitDisplayMetrics
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitGlobal.build(this) {
            env = Env.DEBUG
            isDebug = true
            config = {
                RetrofitConfiguration.build {
                    httpLogLevel { HttpLoggingInterceptor.Level.HEADERS }
                    dynamicHost { "https://www.fastmock.site/mock/98167980d234084dd1fcbd22e1ba1cfe/poet/api/get_auth_cookie" }
                }
            }
        }
        FitDisplayMetrics.build(applicationContext) {
            openFontScale(true)
            designDp(360f)
            fitDisplayOrientation(FitDisplayMetrics.ORIENTATION_WIDTH)
        }
    }
}
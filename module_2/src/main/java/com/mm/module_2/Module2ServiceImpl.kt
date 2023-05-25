package com.mm.module_2

import android.util.Log
import com.mm.annotation.ServiceProvider


/**
 * Date : 2023/5/25
 */
@ServiceProvider(returnType = Module2Service::class, des = "module2 对外提供的接口")
class Module2ServiceImpl : Module2Service {

    override fun moduleName(): String {
        return BuildConfig.LIBRARY_PACKAGE_NAME
    }

    override fun module2Log(message: String) {
        Log.e("module2", message)
    }
}
package com.mm.module_1

import android.app.Application
import com.mm.common.okbus.IModule


/**
 * Date : 2023/5/25
 */
class Module1 : IModule {

    override fun init() {

    }

    override fun getModuleId(): String {
        return "module_1"
    }

    override fun afterConnected(application: Application?) {

    }
}
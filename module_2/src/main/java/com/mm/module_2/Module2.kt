package com.mm.module_2

import android.app.Application
import com.mm.common.okbus.IModule


/**
 * Date : 2023/5/25
 */
class Module2 : IModule {

    override fun init() {

    }

    override fun getModuleId(): String {
        return "module_2"
    }

    override fun afterConnected(application: Application?) {

    }
}
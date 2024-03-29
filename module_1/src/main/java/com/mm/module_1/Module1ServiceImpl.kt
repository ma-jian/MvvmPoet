package com.mm.module_1

import com.mm.router.annotation.ServiceProvider


/**
 * Date : 2023/5/25
 */
@ServiceProvider("module1/service", des = "module1 对外提供的接口")
class Module1ServiceImpl : Module1Service {

    override fun moduleName(): String {
        return BuildConfig.LIBRARY_PACKAGE_NAME
    }

    override fun version(): Int {
        return 1
    }
}
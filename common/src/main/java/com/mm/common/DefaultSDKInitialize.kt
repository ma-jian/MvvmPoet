package com.mm.common

import android.app.Application
import com.mm.common.FitDisplayMetrics.Companion.restDisplayMetrics
import com.mm.common.crash.AppCrashExceptionHandler
import com.mm.common.okbus.IModule
import com.mm.common.utils.isMainProcess
import com.tencent.mmkv.MMKV
import java.util.ServiceLoader


/**
 * Date : 2023/5/12
 * @since 1.0
 * 默认工具类注册器
 */
class DefaultSDKInitialize : ActivityDelegate.InstallableWatcher {

    companion object {
        lateinit var mApplication: Application
    }

    override fun install(application: Application) {
        mApplication = application
        //mmkv
        MMKV.initialize(application)
        if (application.isMainProcess()) {
            /**
             * 屏幕适配 默认开启以屏幕宽度适配，375dp为基点，
             * 若出现适配异常可调用 [FitDisplayMetrics.restDisplayMetrics] 恢复系统适配
             */
            FitDisplayMetrics.build(application) {
                openFontScale(false)
                designDp(375f)
                fitDisplayOrientation(FitDisplayMetrics.ORIENTATION_WIDTH)
            }
            AppCrashExceptionHandler.instance.init()
            registerSpi(application)
        }
    }

    override fun uninstall(application: Application) {

    }

    private fun registerSpi(application: Application) {
        //module自动注册服务
        val modules = ServiceLoader.load(IModule::class.java)
        for (module in modules) module.afterConnected(application)
    }
}
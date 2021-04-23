package com.mm.lib_util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.mm.lib_util.etoast.ToastGlobal
import java.lang.ref.WeakReference

/**
 * Created by : majian
 * Date : 4/22/21
 * Describe : activity生命周期监听代理，为其他工具提供支持。
 */

class ActivityDelegate private constructor(application: Application) : Application.ActivityLifecycleCallbacks {
    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    private var activity: Activity? = null
    internal val mActivity: Activity?
        get() = activity

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        this.activity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        this.activity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        this.activity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        this.activity = null
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        internal var delegate: WeakReference<ActivityDelegate> = WeakReference(null)
        internal val appDefaultWatchers = listOf(ToastGlobal.newInstance(), DialogQueue.newInstance())

        /**
         * @param application
         * @param watcher 为观察者共享activity生命周期
         */
        fun register(application: Application, watcher: List<InstallableWatcher> = emptyList()) =
            ActivityDelegate(application).apply {
                delegate = WeakReference(this)
                (if (watcher.isEmpty()) appDefaultWatchers else watcher).forEach {
                    it.install(application)
                }
            }

        fun unRegister(application: Application, watcher: List<InstallableWatcher> = appDefaultWatchers) =
            ActivityDelegate(application).apply {
                delegate = WeakReference(null)
                watcher.forEach {
                    it.uninstall(application)
                }
            }
    }


    interface InstallableWatcher {
        fun install(application: Application)
        fun uninstall(application: Application)
    }
}

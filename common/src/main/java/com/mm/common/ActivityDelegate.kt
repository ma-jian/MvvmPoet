package com.mm.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Created by : m
 * @since 1.0
 * Describe : activity生命周期监听代理，为其他工具提供支持。
 */

class ActivityDelegate private constructor(application: Application) : Application.ActivityLifecycleCallbacks {
    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    private val activityStack = ArrayDeque<Activity>()

    fun getActivityStack() = activityStack

    val activity: Activity?
        get() = activityStack.firstOrNull()

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activityStack.contains(activity)) {
            activityStack.remove(activity)
        }
        if (activity is FragmentActivity) {
            activityStack.addFirst(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
    }

    companion object {
        @JvmField
        var delegate: WeakReference<ActivityDelegate> = WeakReference(null)

        internal val appDefaultWatchers = arrayListOf<InstallableWatcher>(DefaultSDKInitialize())

        /**
         * @param application
         * @param watcher 为观察者共享activity生命周期
         */
        fun register(application: Application, watcher: List<InstallableWatcher> = appDefaultWatchers) =
            ActivityDelegate(application).apply {
                delegate = WeakReference(this)
                watcher.forEach {
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

package com.mm.mvvmpoet.etoast

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Pair
import android.view.Gravity
import androidx.annotation.LayoutRes
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by : majian
 * Date : 4/20/21
 * Describe : Toast全局管理类
 */
class ToastGlobal private constructor() : ActivityLifecycleCallbacks {
    private var mActivity: Activity? = null
    internal val activity: Activity?
        get() = mActivity

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        mActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        mActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        mActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {
        mActivity = null
    }

    companion object {
        private var isShown = false
        private lateinit var queue: Queue<Pair<Activity?, ToastCompat>>
        internal lateinit var global: WeakReference<ToastGlobal>
        fun build(application: Application) = ToastGlobal().apply {
            queue = LinkedList()
            application.registerActivityLifecycleCallbacks(this)
            global = WeakReference<ToastGlobal>(this)
        }

        @Synchronized
        private fun showSyn() {
            if (!isShown && !queue.isEmpty()) {
                queue.poll()?.apply {
                    if (first?.isFinishing == true || first?.isDestroyed == true) {
                        //主窗口载体消亡了,就可以清空了
                        queue.clear()
                        isShown = false
                    } else {
                        isShown = true
                        second.show()
                    }
                }
            }
        }

        private fun addQueue(toastCompat: Pair<Activity?, ToastCompat>) {
            queue.offer(toastCompat)
        }

        /**
         * 按队列显示,先进先出
         * @param text
         * @param style
         */
        fun showByQueue(text: CharSequence?, style: Int = 0) {
            global.get()?.mActivity?.let {
                if (!text.isNullOrEmpty()) {
                    val toast = ToastCompat.makeText(context = it, text) {
                        message(text)
                        duration(EToast.LENGTH_SHORT)
                        styleType(style)
                        setGravity(Gravity.CENTER)
                    }.onToastListener {
                        isShown = false
                        showSyn()
                    }
                    addQueue(Pair(it, toast))
                    showSyn()
                }
            }
        }

        /**
         * 按队列显示,先进先出
         * @param text
         * @param block
         */
        fun showByQueue(text: CharSequence?, block: ToastCompat.Builder.() -> Unit) {
            global.get()?.mActivity?.let {
                if (!text.isNullOrEmpty()) {
                    val toast = ToastCompat.makeText(context = it, text, block).onToastListener {
                        isShown = false
                        showSyn()
                    }
                    addQueue(Pair(it, toast))
                    showSyn()
                }
            }
        }

        /**
         * @param text
         * @param duration 可以是任意时间，但不保证生效。
         * @param style 自定义布局
         */
        fun show(text: CharSequence?, @EToast.Duration duration: Int = EToast.LENGTH_SHORT, @LayoutRes style: Int = 0) {
            global.get()?.mActivity?.let {
                if (!text.isNullOrEmpty()) {
                    ToastCompat.makeText(context = it, text) {
                        message(text)
                        duration(duration)
                        styleType(style)
                    }.show()
                }
            }
        }

        /**
         * @param text
         * @param block 自定义Toast参数
         */
        fun show(text: CharSequence?, block: ToastCompat.Builder.() -> Unit) {
            global.get()?.mActivity?.let {
                if (!text.isNullOrEmpty()) {
                    ToastCompat.makeText(context = it, text, block).show()
                }
            }
        }
    }
}
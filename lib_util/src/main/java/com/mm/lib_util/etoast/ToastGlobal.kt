package com.mm.lib_util.etoast

import android.app.Activity
import android.app.Application
import android.util.Pair
import android.view.Gravity
import androidx.annotation.LayoutRes
import com.mm.lib_util.ActivityDelegate
import java.util.*

/**
 * Created by : majian
 * Date : 4/20/21
 * Describe : Toast全局管理类，关闭通知栏弹出Toast
 * [ActivityDelegate.register] 默认在代理类自动完成本工具的注册
 */
class ToastGlobal private constructor() : ActivityDelegate.InstallableWatcher {
    companion object {
        private var isShown = false
        private lateinit var queue: Queue<Pair<Activity?, ToastCompat>>

        /**
         * @return 返回对象实例提供注册使用
         */
        fun newInstance() = ToastGlobal()

        /**
         * @param application
         * @param watcher 全局代理类是否自动注册，否则在本类进行注册
         */
        fun register(application: Application, watcher: Boolean = false) = newInstance().run {
            queue = LinkedList()
            if (!watcher) {
                val list = ActivityDelegate.appDefaultWatchers.dropWhile { it::class.java == this::class.java }
                ActivityDelegate.register(application, list)
            }
        }

        @Synchronized
        private fun showSyn() {
            require(this::queue.isInitialized) { "请先进行注册ToastGlobal初始化，register()" }
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
         * 为java提供
         */
        fun showByQueue(text: CharSequence?) {
            showByQueue(text, 0)
        }

        /**
         * 按队列显示,先进先出
         * @param text
         * @param style
         */
        fun showByQueue(text: CharSequence?, style: Int = 0) {
            require(this::queue.isInitialized) { "请先进行注册ToastGlobal初始化，register()" }
            ActivityDelegate.delegate.get()?.mActivity?.let {
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
            require(this::queue.isInitialized) { "请先进行注册ToastGlobal初始化，register()" }
            ActivityDelegate.delegate.get()?.mActivity?.let {
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
         * 为java提供
         */
        fun show(text: CharSequence?) {
            show(text, EToast.LENGTH_SHORT, 0)
        }

        fun show(text: CharSequence?, duration: Int) {
            show(text, duration, 0)
        }

        /**
         * @param text
         * @param duration 可以是任意时间，但不保证生效。
         * @param style 自定义布局
         */
        fun show(text: CharSequence?, @EToast.Duration duration: Int = EToast.LENGTH_SHORT, @LayoutRes style: Int = 0) {
            require(this::queue.isInitialized) { "请先进行注册ToastGlobal初始化，register()" }
            ActivityDelegate.delegate.get()?.mActivity?.let {
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
            require(this::queue.isInitialized) { "请先进行注册ToastGlobal初始化，register()" }
            ActivityDelegate.delegate.get()?.mActivity?.let {
                if (!text.isNullOrEmpty()) {
                    ToastCompat.makeText(context = it, text, block).show()
                }
            }
        }
    }

    override fun install(application: Application) {
        register(application, true)
    }

    override fun uninstall(application: Application) {
        queue.clear()
    }
}
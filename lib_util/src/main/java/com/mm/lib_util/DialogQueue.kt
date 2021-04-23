package com.mm.lib_util

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ComponentCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.text.TextUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by : majian
 * Date : 4/22/21
 * Describe : dialog队列弹窗，支持在指定activity，fragment队列弹出，支持弹窗插队，延迟，自定义拦截
 * [ActivityDelegate.register] 默认在代理类自动完成本工具的注册
 */
class DialogQueue private constructor() : ActivityDelegate.InstallableWatcher {

    /**
     * 实现该接口，并在Fragment显示时调用[notifyVisible],可在指定Fragment页面弹出队列
     * @sample DialogQueue class Fragment2 : Fragment(), DialogQueue.FragmentObserver {}
     */
    interface FragmentObserver {
        fun notifyVisible(fragment: Fragment, visible: Boolean) {
            if (visible) {
                findPopDialog(fragment::class.java)
            }
        }

        fun notifyVisible(fragment: android.app.Fragment, visible: Boolean) {
            if (visible) {
                findPopDialog(fragment::class.java)
            }
        }
    }

    companion object {
        private lateinit var queue: ArrayDeque<Node>
        private var mShowingNode: Node? = null
        //找的已符合条件的弹窗队列
        private val readyAsyncNode = PriorityBlockingQueue<Node>()
        //已经弹出的弹窗队列
        private val runningAsyncNode = ArrayDeque<Node>()
        private val lock = ReentrantLock()

        /**
         * @return 返回对象实例提供注册使用，其他情况无意义
         */
        fun newInstance() = DialogQueue()

        /**
         * @param application
         * @param watcher 全局代理类是否自动注册，否则在本类进行注册
         * @see ActivityDelegate.register 单独注册超过两个以上建议使用
         */
        fun register(application: Application, watcher: Boolean = false) = newInstance().run {
            queue = ArrayDeque<Node>()
            if (!watcher) {
                val list = ActivityDelegate.appDefaultWatchers.dropWhile { it::class.java == this::class.java }
                ActivityDelegate.register(application, list)
            }
        }

        /**
         * @see register 使用前先完成注册
         * @param dialog 弹出框
         * @param code 队列顺序，数字越小优先级越高 0 -> [Int.MAX_VALUE]
         * @param block 队列其他配置
         */
        fun addDialog(dialog: Dialog, code: Int, block: (Node.() -> Unit)? = null) {
            require(this::queue.isInitialized) { "请先进行DialogStack初始化register" }
            val dialogNode = DialogNode()
            dialogNode.dialog = dialog
            dialogNode.code = code
            block?.let {
                dialogNode.apply(it)
            }
            queue.add(dialogNode)
            findPopDialog()
        }

        fun addDialog(dialog: DialogFragment, code: Int, block: (Node.() -> Unit)? = null) {
            require(this::queue.isInitialized) { "请先进行DialogStack初始化register" }
            val dialogNode = AndroidxFragmentNode()
            dialogNode.dialog = dialog
            dialogNode.code = code
            block?.let {
                dialogNode.apply(it)
            }
            queue.add(dialogNode)
            findPopDialog()
        }

        fun addDialog(dialog: android.app.DialogFragment, code: Int, block: (Node.() -> Unit)? = null) {
            require(this::queue.isInitialized) { "请先进行DialogStack初始化register" }
            val dialogNode = FragmentNode()
            dialogNode.dialog = dialog
            dialogNode.code = code
            block?.let {
                dialogNode.apply(it)
            }
            queue.add(dialogNode)
            findPopDialog()
        }

        private val observer = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                findPopDialog()
            }
        }

        /**
         * @param observerFragment 指定的Fragment
         */
        private fun findPopDialog(observerFragment: Class<out Any>? = null) {
            ActivityDelegate.delegate.get()?.mActivity?.let { activity ->
                if (queue.isNotEmpty()) {
                    val list = queue.filter {
                        if (it.targetActivity.invoke().isNotEmpty()) {
                            if (it.targetActivity.invoke().contains(activity::class.java)) {
                                if (it.targetFragment.invoke().isNotEmpty()) {
                                    if (activity is FragmentActivity) {
                                        if (observerFragment != null) {
                                            it.targetFragment.invoke().contains(observerFragment)
                                        } else {
                                            val visible = activity.supportFragmentManager.fragments.filter { f -> f.userVisibleHint && f.isVisible }
                                            !Collections.disjoint(it.targetFragment.invoke(), visible)
                                        }
                                    } else {
                                        true
                                    }
                                } else {
                                    true
                                }
                            } else {
                                false
                            }
                        } else {
                            true
                        }
                    }
                    readyAsyncNode.clear()
                    readyAsyncNode.addAll(list)
                    if (readyAsyncNode.isNotEmpty()) {
                        nextDialog()
                    }
                }
            }
        }

        /**
         * intercept ->
         * activity -> fragment -> tag
         */
        private fun nextDialog() {
            ActivityDelegate.delegate.get()?.mActivity?.let { activity ->
                if (readyAsyncNode.isNotEmpty()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        if (lock.isHeldByCurrentThread || mShowingNode != null) {
                            return@launch
                        }
                        if (activity is LifecycleOwner) {
                            activity.lifecycle.addObserver(observer)
                        }
                        lock.lock()
                        readyAsyncNode.poll()?.apply {
                            //是否拦截该弹窗，拦截直接跳过弹出下一个
                            if (!intercept.invoke()) {
                                delay(delay)
                                when (this) {
                                    is DialogNode -> {
                                        mShowingNode = this
                                        runningAsyncNode.add(this)
                                        dialog.apply {
                                            //dialog 只会在当前activity弹出，非指定activity直接跳过
                                            val javaClass = findContextBase(context)
                                            if (javaClass == activity.localClassName) {
                                                show()
                                                setOnDismissListener {
                                                    lock.unlock()
                                                    mShowingNode = null
                                                    nextDialog()
                                                }
                                            } else {
                                                lock.unlock()
                                                mShowingNode = null
                                                nextDialog()
                                            }
                                        }
                                    }
                                    is AndroidxFragmentNode -> {
                                        mShowingNode = this
                                        runningAsyncNode.add(this)
                                        val tag = if (TextUtils.isEmpty(tag)) dialog.javaClass.name else tag
                                        dialog.apply {
                                            lifecycle.addObserver(object : LifecycleObserver {
                                                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                                                fun onDestroy() {
                                                    lock.unlock()
                                                    mShowingNode = null
                                                    nextDialog()
                                                }
                                            })
                                            if (activity is FragmentActivity) {
                                                show(activity.supportFragmentManager, tag)
                                            } else {
                                                lock.unlock()
                                                mShowingNode = null
                                                nextDialog()
                                            }
                                        }
                                    }
                                    is FragmentNode -> {
                                        mShowingNode = this
                                        runningAsyncNode.add(this)
                                        val tag = if (TextUtils.isEmpty(tag)) dialog.javaClass.name else tag
                                        dialog.apply {
                                            show(activity.fragmentManager, tag)
                                            this.dialog.setOnDismissListener {
                                                lock.unlock()
                                                mShowingNode = null
                                                nextDialog()
                                            }
                                        }
                                    }
                                    else -> {
                                        lock.unlock()
                                        throw IllegalStateException("弹窗队列不符合条件,type in dialog,dialogFragment")
                                    }
                                }
                            } else {
                                lock.unlock()
                                mShowingNode = null
                                nextDialog()
                            }
                        }
                    }
                } else {
                    //移除已经弹出的队列。未弹出的队列可在下次满足条件后弹出
                    queue.removeAll(runningAsyncNode)
                    runningAsyncNode.clear()
                }
            }
        }

        private fun findContextBase(context: Context): String {
            if (context is Activity) {
                return context.localClassName
            } else if (context is ContextWrapper) {
                return findContextBase(context.baseContext)
            }
            return context.javaClass.name
        }
    }

    internal class AndroidxFragmentNode : Node() {
        lateinit var dialog: DialogFragment
    }

    internal class FragmentNode : Node() {
        lateinit var dialog: android.app.DialogFragment
    }


    /**
     * 对dialog 设置 targetActivity无意义
     */
    internal class DialogNode : Node() {
        lateinit var dialog: Dialog
    }

    open class Node : Comparable<Node> {
        var code: Int = 0
        var targetFragment: () -> List<Class<out ComponentCallbacks>> = { emptyList() }
        var targetActivity: () -> List<Class<out Activity>> = { emptyList() }
        var delay: Long = 0L
        var override: Boolean = false
        var tag: String? = ""
        var intercept: () -> Boolean = { false }
        override fun compareTo(other: Node): Int {
            return if (override) -1 else code.compareTo(other.code)
        }
    }

    override fun install(application: Application) {
        register(application, true)
    }

    override fun uninstall(application: Application) {
        mShowingNode = null
        readyAsyncNode.clear()
        runningAsyncNode.clear()
        queue.clear()
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }
}
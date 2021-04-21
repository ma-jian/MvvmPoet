package com.mm.mvvmpoet

import android.app.Dialog
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.lang.reflect.Field
import java.util.*

/**
 * @待处理
 */
class DialogManager {

    private var queue: DialogQueue? = null
    private var currentDialog: Any? = null

    init {
        queue = DialogQueue()
    }

    companion object {

        //暂无此需求
        val LEVEL_WEIGHT_DEFAULT :Int = 0    // 默认权重
        val LEVEL_WEIGHT_COMMON  :Int = 1
        val LEVEL_WEIGHT_MIDDLE  :Int = 2
        val LEVEL_WEIGHT_HIGHEST :Int = 3

        fun getInstance(): DialogManager {
            return DialogManagerHolder.dm
        }
    }

    private object DialogManagerHolder {
        internal val dm = DialogManager()
    }

    @Synchronized
    fun addDialog(dialog: Any?): DialogManager {
        if (dialog is Dialog || dialog is DialogFragment) {
            queue?.offer(dialog)
        } else {
            throw Exception("must be dialog or dialogFragment !")
        }
        return getInstance()
    }

    @Synchronized
    fun addDialog(vararg dialogs: Any?): DialogManager {
        for(dialog in dialogs){
            addDialog(dialog)
        }
        return getInstance()
    }

    @Synchronized
    fun show(fm: FragmentManager?) {
        if (currentDialog == null) {
            currentDialog = queue?.poll()
            currentDialog?.run{
                when (currentDialog) {
                    is Dialog -> {
                        (currentDialog as? Dialog)?.show()
                        (currentDialog as? Dialog)?.setOnDismissListener {
                            currentDialog = null
                            show(fm)
                        }
                    }
                    is DialogFragment -> {
                        val dia: DialogFragment? = (currentDialog as? DialogFragment)
                        val lifecycle = dia?.lifecycle
                        lifecycle?.addObserver(object : LifecycleObserver {

                            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                            fun onDestroy() {
                                currentDialog = null
                                show(fm)
                            }
                        })
                        fm?.run {
                            dia?.run {
                                showAllowingLoss(this,fm,dia.javaClass.simpleName)
                            }
                        }?: kotlin.run {
                            Log.e("a","${this@DialogManager.javaClass.simpleName}: Is an instance of DialogFragment, FragmentManager cannot be null")
                        }
                    }
                    else -> {
                        Log.e("a","${this@DialogManager.javaClass.simpleName}: Illegal parameter type")
                    }
                }
            }
        }else{
            Log.e("a","当前已存在弹框！进入队列等待!")
        }
    }

    /**
     * 解决 Can not perform this action after onSaveInstanceState问题
     * @param manager FragmentManager
     * @param tag     tag
     */
    fun showAllowingLoss(dia: DialogFragment,manager: FragmentManager, tag: String?) {
        try {
            val cls: Class<*> = DialogFragment::class.java
            val mDismissed: Field = cls.getDeclaredField("mDismissed")
            mDismissed.setAccessible(true)
            mDismissed.set(this, false)
            val mShownByMe: Field = cls.getDeclaredField("mShownByMe")
            mShownByMe.setAccessible(true)
            mShownByMe.set(this, true)
        } catch (e: Exception) {
            //调系统的show()方法
//            dia.show(manager!!, tag)
            return
        }
        val ft: FragmentTransaction? = manager?.beginTransaction()
        ft?.add(dia, tag)
        ft?.commitAllowingStateLoss()
    }
}


internal class DialogQueue {


    internal var list: Queue<Any> = LinkedList()

//    internal var highest_list: Queue<Any> = LinkedList()
//    internal var middle_list: Queue<Any> = LinkedList()
//    internal var common_list: Queue<Any> = LinkedList()
//    internal var default_list: Queue<Any> = LinkedList()

    internal fun offer(obj: Any?) {
        list.offer(obj)
    }

    internal fun poll(): Any? {
        return if (list.size > 0) {
            list.poll()
        } else null
    }
}
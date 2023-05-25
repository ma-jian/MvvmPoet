package com.mm.common.etoast

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.mm.common.ActivityDelegate
import com.mm.common.DefaultSDKInitialize
import com.mm.common.R
import com.mm.common.etoast.IToast.Companion.dp2px
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by : m
 * @since 1.0
 */
class ToastCompat private constructor(private val mBuilder: Builder) {
    private val mToast: IToast

    init {
        mToast = createToast()
    }

    private fun createToast(): IToast {
        return IToast.Builder(mBuilder.mContext).type(mBuilder.mType).message(mBuilder.mText).duration(mBuilder.mDuration)
            .view(mBuilder.mView).gravity(mBuilder.mGravity, mBuilder.mX, mBuilder.mY).viewParams(mBuilder.mParams)
            .windowAnimations(mBuilder.mAnimation).build()
    }

    fun show() {
        if (!TextUtils.isEmpty(mToast.text)) {
            mToast.show()
        }
    }

    /**
     * 按队列显示,先进先出
     */
    fun showByQueue() {
        ActivityDelegate.delegate.get()?.let {
            addToastDismissListener(object : ToastDismissListener {
                override fun onDismiss() {
                    lock.unlock()
                    showSyn()
                }
            })
            addQueue(Pair(it.activity, this))
            showSyn()
        }
    }

    private fun addQueue(pair: Pair<Activity?, ToastCompat>) {
        queue.offer(pair)
    }

    private fun showSyn() {
        if (!lock.isLocked && !queue.isEmpty()) {
            val pair = queue.poll()
            if (pair != null) {
                if (pair.first!!.isFinishing || pair.first!!.isDestroyed) {
                    //主窗口载体消亡了,就可以清空了
                    queue.clear()
                } else {
                    lock.lock()
                    pair.second.show()
                }
            }
        }
    }

    fun cancel() {
        mToast.cancel()
    }

    fun setText(message: CharSequence?): ToastCompat {
        if (!TextUtils.isEmpty(message)) {
            mToast.text = message!!
        }
        return this
    }

    fun addToastDismissListener(dismissListener: ToastDismissListener?): ToastCompat {
        mToast.addToastDismissListener(dismissListener!!)
        return this
    }

    class Builder {
        var mText: CharSequence = ""
        var mType = 1 // type 1 toast 2 自定义
        var mDuration = IToast.LENGTH_SHORT
        var mView: View? = null
        var mParams: WindowManager.LayoutParams? = null
        var mGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        var mX = 0
        var mY = 0
        val mContext: Context
        var mAnimation = android.R.style.Animation_Toast

        constructor(context: Context?) {
            mContext = context ?: DefaultSDKInitialize.mApplication
            mY = dp2px(context ?: DefaultSDKInitialize.mApplication, 72)
        }

        constructor() {
            val delegate = ActivityDelegate.delegate.get()
            mContext = delegate?.activity ?: DefaultSDKInitialize.mApplication
            mY = dp2px(mContext, 72)
        }

        fun message(message: CharSequence): Builder {
            mText = message
            return this
        }

        fun message(@StringRes resId: Int): Builder {
            mText = mContext.getString(resId)
            return this
        }

        fun duration(duration: Int): Builder {
            mDuration = duration
            return this
        }

        fun type(type: Int): Builder {
            mType = type
            return this
        }

        fun style(@LayoutRes style: Int): Builder {
            if (style != 0) {
                mView = LayoutInflater.from(mContext).inflate(style, null)
            }
            return this
        }

        fun view(view: View?): Builder {
            mView = view
            return this
        }

        fun viewParams(params: WindowManager.LayoutParams?): Builder {
            mParams = params
            return this
        }

        fun windowAnimations(animation: Int): Builder {
            mAnimation = animation
            if (animation > 0) {
                mType = 2
            }
            return this
        }

        fun gravity(gravity: Int, xOffset: Int, yOffset: Int): Builder {
            mGravity = gravity
            mX = xOffset
            mY = yOffset
            return this
        }

        fun gravity(gravity: Int): Builder {
            mGravity = gravity
            return this
        }

        fun build(): ToastCompat {
            return ToastCompat(this)
        }
    }

    companion object {
        private val queue: Queue<Pair<Activity?, ToastCompat>> = LinkedList()
        private val lock = ReentrantLock()
        private var compat: ToastCompat? = null

        @JvmStatic
        fun makeText(@StringRes text: Int): ToastCompat {
            val delegate = ActivityDelegate.delegate.get()
            val context: Context = delegate?.activity ?: DefaultSDKInitialize.mApplication
            return makeText(context.getString(text))
        }

        @JvmStatic
        fun makeText(text: CharSequence): ToastCompat {
            val delegate = ActivityDelegate.delegate.get()
            val context: Context = delegate?.activity ?: DefaultSDKInitialize.mApplication
            return makeText(context, text, IToast.LENGTH_SHORT)
        }

        @JvmStatic
        fun makeCenterText(text: CharSequence): ToastCompat {
            val delegate = ActivityDelegate.delegate.get()
            val context: Context = delegate?.activity ?: DefaultSDKInitialize.mApplication
            return Builder(context).gravity(Gravity.CENTER, 0, 0).message(text).build()
        }

        @JvmStatic
        fun makeText(context: Context, text: CharSequence, duration: Int = IToast.LENGTH_SHORT): ToastCompat {
            return makeText(context, R.layout.layout_toast, text, duration)
        }

        @JvmStatic
        fun makeText(
            context: Context, @LayoutRes layout: Int, text: CharSequence, duration: Int = IToast.LENGTH_SHORT
        ): ToastCompat {
            if (compat != null) {
                compat!!.cancel()
            }
            compat = Builder(context).message(text).style(layout).gravity(Gravity.CENTER, 0, 0).duration(duration).build()
            return compat!!
        }

        /**
         * @param context
         * @param text
         * @param icon    弹窗icon
         */
        @JvmStatic
        fun makeIconText(context: Context, text: CharSequence, @DrawableRes icon: Int): ToastCompat {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_toast_icon, null)
            val textView = view.findViewById<TextView>(R.id.message)
            val imageView = view.findViewById<ImageView>(R.id.icon)
            textView.text = text
            imageView.setImageResource(icon)
            return Builder(context).message(text).view(view).gravity(Gravity.CENTER, 0, 0).duration(IToast.LENGTH_SHORT).build()
        }

        /**
         * 失败弹窗
         *
         * @param text
         * @return
         */
        @JvmStatic
        fun makeErrorToast(text: CharSequence): ToastCompat {
            return makeViewToast(R.layout.toast_fail_top, text, R.drawable.icon_error_fill, Toast.LENGTH_SHORT)
        }

        /**
         * 失败弹窗
         *
         * @param text
         * @param icon
         * @return
         */
        @JvmStatic
        fun makeErrorToast(text: CharSequence, @DrawableRes icon: Int): ToastCompat {
            return makeViewToast(R.layout.toast_fail_top, text, icon, Toast.LENGTH_SHORT)
        }

        /**
         * 成功顶部弹窗
         */
        @JvmStatic
        fun makeSuccessToast(text: CharSequence): ToastCompat {
            return makeViewToast(R.layout.toast_success_top, text, R.drawable.icon_success_fill, Toast.LENGTH_SHORT)
        }

        /**
         * 成功顶部弹窗
         */
        @JvmStatic
        fun makeSuccessToast(text: CharSequence, @DrawableRes icon: Int): ToastCompat {
            return makeViewToast(R.layout.toast_success_top, text, icon, Toast.LENGTH_SHORT)
        }

        @SuppressLint("ResourceAsColor")
        private fun makeViewToast(
            @LayoutRes layout: Int, text: CharSequence, @DrawableRes icon: Int, duration: Int
        ): ToastCompat {
            val delegate = ActivityDelegate.delegate.get()
            val context: Context = delegate?.activity ?: DefaultSDKInitialize.mApplication
            val view = LayoutInflater.from(context).inflate(layout, null)
            val message = view.findViewById<TextView>(R.id.message)
            val clear = view.findViewById<View>(R.id.clear)
            val ic = view.findViewById<ImageView>(R.id.icon)
            view.setPadding(0, getStatusBarHeight(context), 0, 0)
            if (!TextUtils.isEmpty(text)) {
                message.text = text.toString().trim { it <= ' ' }
            }
            ic?.setImageResource(icon)
            val compat = makeTopToast(view, duration)
            clear.setOnClickListener { compat.cancel() }
            return compat
        }

        private fun makeTopToast(view: View?, duration: Int): ToastCompat {
            return Builder().gravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, -1).view(view).duration(duration)
                .windowAnimations(R.style.toast_animator).build()
        }

        /**
         * 获取状态栏高度
         *
         * @param context 目标Context
         */
        private fun getStatusBarHeight(context: Context?): Int {
            // 获得状态栏高度
            return if (context != null) {
                val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                context.resources.getDimensionPixelSize(resourceId)
            } else {
                -1
            }
        }
    }
}
package com.mm.lib_util.etoast

import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.mm.lib_util.ActivityDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 管理Toast
 */
class ToastCompat private constructor(private val builder: Builder) {

    private var mToast: Any? = null
    private var onToastListener: ((Toast) -> Unit)? = null
    private val nanosPerMilli = 1000000

    init {
        if (isNotificationEnabled(builder.mContext)) {
            mToast = createToast().toast
        } else {
            if (builder.mContext is Activity) {
                mToast = createToast()
            } else {
                ActivityDelegate.delegate.get()?.mActivity?.let {
                    mToast = createToast()
                }
            }
        }
    }


    /**
     * 检查通知栏权限有没有开启
     */
    private fun isNotificationEnabled(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = context.applicationInfo
                val pkg = context.applicationContext?.packageName
                val uid = appInfo?.uid
                try {
                    val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                    val checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String::class.java)
                    val opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")
                    val value = opPostNotificationValue[Int::class.java] as Int
                    checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) as Int == 0
                } catch (e: Exception) {
                    true
                }
            }
            else -> true

        }
    }

    companion object {
        fun makeText(context: Context, text: CharSequence?, builder: Builder.() -> Unit) =
            Builder(context).message(text).apply(builder).build()
    }

    private fun createToast() = EToast.build(builder.mContext) {
        message(if (builder.mResId > 0) mContext.getText(builder.mResId) else builder.mText)
        duration(builder.mDuration)
        style(builder.mStyleType)
        builder.mParams?.let {
            styleParams(it)
        }
        setGravity(builder.mGravity, builder.mX, builder.mY)
    }


    fun show() {
        if (mToast is EToast) {
            (mToast as EToast).onToastDismissListener(onToastListener)
            (mToast as EToast).show()
        } else if (mToast is Toast) {
            GlobalScope.launch(Dispatchers.Main) {
                val start = System.nanoTime()
                val toast = mToast as Toast
                toast.show()
                val end = System.nanoTime()
                delay(builder.mDuration - (end - start).div(nanosPerMilli))
                toast.cancel()
                onToastListener?.invoke((mToast as Toast))
            }
        }
    }

    fun cancel() {
        if (mToast is EToast) {
            (mToast as EToast).cancel()
        } else if (mToast is Toast) {
            (mToast as Toast).cancel()
        }
    }

    fun setText(text: CharSequence?) {
        if (!TextUtils.isEmpty(text)) {
            if (mToast is EToast) {
                (mToast as EToast).setText(text!!)
            } else if (mToast is Toast) {
                (mToast as Toast).setText(text)
            }
        }
    }

    fun onToastListener(onToastListener: ((Toast) -> Unit)?) = apply {
        this.onToastListener = onToastListener
    }

    class Builder internal constructor(internal val mContext: Context) {
        internal var mText: CharSequence = ""
        internal var mResId: Int = 0
        internal var mDuration: Int = EToast.LENGTH_SHORT
        internal var mStyleType: Int = 0
        internal var mParams: WindowManager.LayoutParams? = null
        internal var mGravity: Int = Gravity.CENTER_HORIZONTAL.or(Gravity.BOTTOM)
        internal var mX: Int = 0
        internal var mY: Int = 72
        fun message(message: CharSequence?) = apply {
            this.mText = message ?: ""
        }

        fun message(@StringRes resId: Int) = apply {
            this.mResId = resId
        }

        fun duration(@EToast.Duration duration: Int) = apply {
            this.mDuration = duration
        }

        fun styleType(@LayoutRes styleType: Int) = apply {
            this.mStyleType = styleType
        }

        fun styleParams(params: WindowManager.LayoutParams) = apply {
            this.mParams = params
        }

        fun setGravity(gravity: Int, xOffset: Int = 0, yOffset: Int = 72) = apply {
            this.mGravity = gravity
            this.mX = xOffset
            this.mY = yOffset
        }

        fun setGravity(gravity: Int) = apply {
            this.mGravity = gravity
        }

        fun build() = ToastCompat(this)
    }
}
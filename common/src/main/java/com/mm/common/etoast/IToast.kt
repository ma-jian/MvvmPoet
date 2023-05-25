package com.mm.common.etoast

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.mm.common.R

/**
 * @since 1.0
 * Created by : m
 */
class IToast private constructor(private val mBuilder: Builder) {
    private var mWindowManager: WindowManager? = null
    private var mNextView: View? = null
    private var mIView: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mParams = WindowManager.LayoutParams()
    var toast: Toast
        private set
    private val dismissListenerList: MutableSet<ToastDismissListener> = HashSet()

    init {
        initToast()
        toast = createToast()
    }

    private fun initToast() {
        mWindowManager = mBuilder.mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        if (mBuilder.mParams != null) {
            mParams.copyFrom(mBuilder.mParams)
        }
        mParams.format = PixelFormat.TRANSPARENT
        mParams.windowAnimations = mBuilder.mAnimation
        mParams.title = "IToast"
        mParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        // We can resolve the Gravity here by using the Locale for getting
        // the layout direction
        val config = mBuilder.mContext.resources.configuration
        val gravity = Gravity.getAbsoluteGravity(mBuilder.mGravity, config.layoutDirection)
        mParams.gravity = gravity
        if (gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL) {
            mParams.horizontalWeight = 1.0f
        }
        if (gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL) {
            mParams.verticalWeight = 1.0f
        }
        mParams.x = mBuilder.mX
        mParams.y = mBuilder.mY
        mParams.packageName = mBuilder.mContext.packageName
    }

    private fun createToast(): Toast {
        val duration = if (mBuilder.mDuration == LENGTH_LONG) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        val toast = Toast.makeText(mBuilder.mContext, mBuilder.mText, duration)
        if (mBuilder.mView != null) {
            val textView = mBuilder.mView!!.findViewById<TextView>(R.id.message)
            if (textView != null) {
                if (!TextUtils.isEmpty(mBuilder.mText)) {
                    textView.text = mBuilder.mText
                } else {
                    mBuilder.mText = textView.text
                }
            }
            if (mBuilder.mParams != null) {
                val layout = RelativeLayout(mBuilder.mContext)
                mBuilder.mView!!.layoutParams = mBuilder.mParams
                layout.addView(mBuilder.mView)
                mBuilder.mView = layout
            }
            toast.view = mBuilder.mView
        }
        toast.setGravity(mBuilder.mGravity, mBuilder.mX, mBuilder.mY)
        mNextView = toast.view
        return toast
    }

    fun show() {
        try {
            val delayTime =
                if (mBuilder.mDuration == Toast.LENGTH_LONG) LENGTH_LONG else if (mBuilder.mDuration == Toast.LENGTH_SHORT) LENGTH_SHORT else mBuilder.mDuration
            if (isIToast) {
                if (mIView !== mNextView) {
                    hide()
                    mIView = mNextView
                    if (mIView!!.parent != null) {
                        mWindowManager!!.removeView(mIView)
                    }
                    mWindowManager!!.addView(mIView, mParams)
                    mainHandler.postDelayed({ cancel() }, delayTime.toLong())
                }
            } else {
                val start = System.nanoTime()
                toast.show()
                val end = System.nanoTime()
                val t = end - start
                val nanosPerMilli = 1000000
                val div = Math.floor(t * 1.0 / nanosPerMilli).toInt()
                mainHandler.postDelayed({ cancel() }, (delayTime - div).toLong())
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun cancel() {
        try {
            if (isIToast) {
                hide()
                mNextView = null
            } else {
                toast.cancel()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            for (listener in dismissListenerList) {
                listener.onDismiss()
            }
        }
    }

    fun addToastDismissListener(dismissListener: ToastDismissListener) {
        dismissListenerList.add(dismissListener)
    }

    var text: CharSequence
        get() = mBuilder.mText
        set(message) {
            if (isIToast) {
                if (mNextView == null) {
                    mBuilder.mText = message
                    toast = createToast()
                } else {
                    val textView = findTextView(mNextView)
                    if (textView is TextView) {
                        textView.text = message
                    }
                }
            } else {
                val textView = findTextView(mNextView)
                if (textView is TextView) {
                    textView.text = message
                }
            }
        }

    private fun findTextView(view: View?): View? {
        if (view is ViewGroup) {
            if (view.findViewById<View?>(R.id.message) != null) {
                val text = view.findViewById<View>(R.id.message)
                if (text is TextView) {
                    return text
                }
            } else {
                findTextView(view.getChildAt(0))
            }
        }
        return view
    }

    private fun hide() {
        if (isIToast) {
            if (mIView != null && mIView!!.parent != null) {
                mWindowManager!!.removeViewImmediate(mIView)
            }
            mIView = null
        } else {
            toast.cancel()
        }
    }

    /**
     * 检查通知栏权限有没有开启
     */
    private val isNotificationEnabled: Boolean
        private get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val notificationManager = mBuilder.mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                return notificationManager.areNotificationsEnabled()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val appOps = mBuilder.mContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = mBuilder.mContext.applicationInfo
                val pkg = mBuilder.mContext.packageName
                val uid = appInfo.uid
                return try {
                    val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                    val checkOpNoThrowMethod =
                        appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String::class.java)
                    val opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")
                    val value = opPostNotificationValue[Int::class.java] as Int
                    checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED
                } catch (exception: Exception) {
                    true
                }
            }
            return true
        }
    private val isIToast: Boolean
        private get() = mBuilder.mType == 2

    class Builder(val mContext: Context) {
        var mText: CharSequence = ""
        var mDuration = LENGTH_SHORT
        var mView: View? = null
        var mParams: WindowManager.LayoutParams? = null
        var mGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        var mX = 0
        var mY: Int
        internal var mAnimation = android.R.style.Animation_Toast
        internal var mType = 1 // type 1 toast 2 自定义

        init {
            mY = dp2px(mContext, 72)
        }

        fun message(@StringRes resId: Int): Builder {
            if (resId != 0) {
                mText = mContext.getString(resId)
            }
            return this
        }

        fun message(text: CharSequence): Builder {
            mText = text
            return this
        }

        fun duration(duration: Int): Builder {
            mDuration = duration
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
            return this
        }

        fun gravity(gravity: Int, xOffset: Int, yOffset: Int): Builder {
            mGravity = gravity
            mX = xOffset
            mY = yOffset
            return this
        }

        fun type(type: Int): Builder {
            mType = type
            return this
        }

        fun build(): IToast {
            return IToast(this)
        }
    }

    companion object {
        const val LENGTH_SHORT = 3000
        const val LENGTH_LONG = 5000

        @JvmStatic
        fun dp2px(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (density * dp + 0.5).toInt()
        }
    }
}
package com.mm.mvvmpoet.etoast

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.mm.mvvmpoet.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by : majian
 * Date : 4/20/21
 * Describe : Toast
 */
internal class EToast private constructor(private val builder: Builder) {
    @IntDef(LENGTH_SHORT, LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    private val windowManager = builder.mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var delayTime = when (builder.mDuration) {
        Toast.LENGTH_SHORT -> LENGTH_SHORT
        Toast.LENGTH_LONG -> LENGTH_LONG
        else -> builder.mDuration
    }

    private var duration = when (builder.mDuration) {
        LENGTH_SHORT -> Toast.LENGTH_SHORT
        LENGTH_LONG -> Toast.LENGTH_LONG
        else -> Toast.LENGTH_LONG
    }

    private var mOnListener: ((Toast) -> Unit)? = null
    private var mView: View? = null
    private var mNextView: View? = null
    private val mParams = WindowManager.LayoutParams()
    internal var toast: Toast = Toast.makeText(builder.mContext, builder.mText, duration).apply {
        if (builder.mStyleType > 0) {
            view = LayoutInflater.from(builder.mContext).inflate(builder.mStyleType, null).apply {
                findViewById<TextView>(R.id.message)?.let {
                    it.text = builder.mText
                }
            }
        }
        mNextView = view
        setGravity(builder.mGravity, builder.mX, builder.mY)
    }

    init {
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.format = PixelFormat.TRANSLUCENT
        mParams.windowAnimations = android.R.style.Animation_Toast
        mParams.title = "EToast"
        mParams.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        // We can resolve the Gravity here by using the Locale for getting
        // the layout direction
        val config = builder.mContext.resources.configuration
        val gravity = Gravity.getAbsoluteGravity(builder.mGravity, config.layoutDirection)
        mParams.gravity = gravity
        if (gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL) {
            mParams.horizontalWeight = 1.0f
        }
        if (gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL) {
            mParams.verticalWeight = 1.0f
        }
        mParams.x = builder.mX
        mParams.y = builder.mY
        mParams.packageName = builder.mContext.packageName
        builder.mParams?.let {
            mParams.copyFrom(it)
        }
    }


    fun show() {
        require(mNextView != null) { "toast make must have been called" }
        try {
            GlobalScope.launch(Dispatchers.Main) {
                if (mView != mNextView) {
                    hide()
                    mView = mNextView
                    mView?.parent?.let {
                        windowManager.removeView(mView)
                    }
                    windowManager.addView(mView, mParams)
                    delay(delayTime.toLong())
                    cancel()
                }
            }
        } catch (e: Exception) {
            LToast.e(e.message)
        }
    }

    fun setText(message: CharSequence) {
        toast.view?.findViewById<TextView>(R.id.message)?.let {
            it.text = message
        } ?: run {
            toast.setText(message)
        }
    }

    private fun hide() {
        mView?.let {
            it.parent?.apply {
                windowManager.removeViewImmediate(it)
            }
            mView = null
        }
    }

    fun cancel() {
        try {
            hide()
            mNextView = null
        } catch (e: Exception) {
            LToast.e(e.message)
        } finally {
            toast.cancel()
            mOnListener?.invoke(toast)
        }
    }

    companion object {
        internal const val LENGTH_SHORT = 2000
        internal const val LENGTH_LONG = 3500
        fun build(context: Context, block: Builder.() -> Unit) =
            Builder().context(context).apply(block).build()
    }

    fun onToastDismissListener(block: ((Toast) -> Unit)?) = apply {
        this.mOnListener = block
    }

    class Builder {
        internal var mText: CharSequence = ""
        internal lateinit var mContext: Context
        internal var mDuration = Toast.LENGTH_SHORT
        internal var mStyleType = 0
        internal var mParams: WindowManager.LayoutParams? = null
        internal var mGravity: Int = Gravity.CENTER_HORIZONTAL.or(Gravity.BOTTOM)
        internal var mX: Int = 0
        internal var mY: Int = 72

        fun context(context: Context) = apply {
            this.mContext = context
        }

        fun message(@StringRes resId: Int) = apply {
            this.mText = mContext.getText(resId).toString()
        }

        fun message(text: CharSequence) = apply {
            this.mText = text
        }

        fun duration(@EToast.Duration duration: Int) = apply {
            this.mDuration = duration
        }

        fun style(@LayoutRes style: Int) = apply {
            this.mStyleType = style
        }

        fun styleParams(params: WindowManager.LayoutParams) = apply {
            this.mParams = params
        }

        fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) = apply {
            this.mGravity = gravity
            this.mX = xOffset
            this.mY = yOffset
        }

        fun build() = EToast(this)
    }
}
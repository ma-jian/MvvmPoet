package com.mm.common.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.mm.common.ActivityDelegate
import com.mm.common.R
import kotlin.math.abs

/**
 * Created by : m
 * @since 1.0
 * 仿ios modal弹窗
 */

object WindowModalAnimator {
    private const val scaleX = 0.94f
    private const val scaleY = 0.94f
    private lateinit var background: Drawable

    fun isScale(): Boolean {
        val window = ActivityDelegate.delegate.get()?.activity?.let {
            it.window?.decorView?.findViewById<View>(android.R.id.content)
        }
        return (window?.scaleX ?: 1f) < 1 && (window?.scaleY ?: 1f) < 1
    }

    /**
     * @param view 被覆盖的窗体
     */
    @JvmStatic
    fun showAnimation(view: View? = null) {
        val window = view ?: ActivityDelegate.delegate.get()?.activity?.let {
            background =
                it.window?.decorView?.background ?: ColorDrawable(ContextCompat.getColor(it, R.color.color_f5f5f5))
            it.window?.decorView?.background = ColorDrawable(ContextCompat.getColor(it, R.color.color_333333))
            it.window?.decorView?.findViewById<View>(android.R.id.content)
        }
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, scaleX)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, scaleY)
        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(window, scaleX, scaleY)
        objectAnimator.duration = 400
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.start()
    }

    @JvmStatic
    fun hideAnimation(view: View? = null) {
        val window = view ?: ActivityDelegate.delegate.get()?.activity?.let {
            it.window?.decorView?.findViewById<View>(android.R.id.content)
        }
        if ((window?.scaleX ?: 1f) < 1 && (window?.scaleY ?: 1f) < 1) {
            val scaleX = PropertyValuesHolder.ofFloat("scaleX", scaleX, 1f)
            val scaleY = PropertyValuesHolder.ofFloat("scaleY", scaleY, 1f)
            val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(window, scaleX, scaleY)
            objectAnimator.duration = 200
            objectAnimator.interpolator = LinearInterpolator()
            objectAnimator.start()
            objectAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    ActivityDelegate.delegate.get()?.activity?.let {
                        it.window?.decorView?.background = background
                    }
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
        } else {
            ActivityDelegate.delegate.get()?.activity?.let {
                it.window?.decorView?.background = background
            }
        }
    }

    @JvmStatic
    fun bottomSlideOffset(offset: Float) {
        ActivityDelegate.delegate.get()?.activity?.let {
            val window = it.window?.decorView?.findViewById<View>(android.R.id.content)
            // 1 - 0.93 0.07 0.55 *  0.08
            val x = if (abs(offset) * 0.1f > (1 - scaleX)) (1 - scaleX) else abs(offset) * 0.1f
            val y = if (abs(offset) * 0.11f > (1 - scaleY)) (1 - scaleY) else abs(offset) * 0.11f
//            val traY = if (-2 * translationY * abs(offset) < -translationY) -translationY
//            else -2 * translationY * abs(offset)
            window?.scaleX = scaleX + x
            window?.scaleY = scaleY + y
//            window?.translationY = traY
        }
    }
}
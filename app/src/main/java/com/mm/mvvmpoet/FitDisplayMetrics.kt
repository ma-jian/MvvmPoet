package com.mm.mvvmpoet

import android.content.ComponentCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.IntDef

/**
 * Created by : majian
 * Date : 4/20/21
 * Describe : 屏幕适配
 */

class FitDisplayMetrics private constructor(val builder: Builder) {
    private var defaultMetrics = Resources.getSystem().displayMetrics
    private val defaultFontScale = defaultMetrics.scaledDensity.times(1.0f)
        .div(defaultMetrics.density)

    companion object {
        const val ORIENTATION_WIDTH = 0
        const val ORIENTATION_HEIGHT = 1
        private val screen = IntArray(2)
        private lateinit var defaultDisplay: DisplayMetrics
        private var fontScale: Float = 1.0f
        private var isOpenScale = false
        private var designDp = 360f
        private var fitByWidth = ORIENTATION_WIDTH

        /**
         * @param context 初始化
         * @param block
         */
        fun build(context: Context, block: Builder.() -> Unit) =
            Builder().apply(block).build().apply {
                context.apply {
                    getSystemService(Context.WINDOW_SERVICE).apply {
                        if (this is WindowManager) {
                            val metrics = DisplayMetrics()
                            defaultDisplay.getMetrics(metrics)
                            screen[0] = metrics.widthPixels
                            screen[1] = metrics.heightPixels
                        }
                    }
                }.registerComponentCallbacks(object : ComponentCallbacks {
                    override fun onConfigurationChanged(newConfig: Configuration) {
                        fontScale = if (newConfig.fontScale > 1) {
                            Resources.getSystem().displayMetrics.scaledDensity.times(1.0f)
                                .div(Resources.getSystem().displayMetrics.density)
                        } else {
                            defaultFontScale
                        }
                    }

                    override fun onLowMemory() {
                    }
                })
                defaultDisplay = defaultMetrics
                fontScale = defaultFontScale
                isOpenScale = builder.fontScale
                designDp = builder.designDp
                fitByWidth = builder.displayOrientation
            }

        /**
         * @param resources 需要适配的资源
         * @param orientation 适配维度 default 横向
         */
        fun fitDisplayMetrics(resources: Resources, orientation: Int = fitByWidth) = run {
            require(screen.size == 2) { "请先初始化build()" }
            val displayPx = if (orientation == ORIENTATION_WIDTH) screen[0] else screen[1]
            val density = displayPx.times(1.0f).div(designDp)
            val densityDpi = density.times(160).toInt()
            DensityConfig(density, if (isOpenScale) density.times(fontScale) else density, densityDpi).apply(resources)
        }

        /**
         * @param newDesign 新设计尺寸适配，默认恢复系统初始化
         */
        fun Context.restDisplayMetrics(newDesign: Float = 0f) = object : ContextWrapper(this) {
            override fun getResources(): Resources {
                val resources = super.getResources()
                return if (newDesign == 0f) {
                    DensityConfig(defaultDisplay.density, defaultDisplay.scaledDensity, defaultDisplay.densityDpi).apply(resources)
                } else {
                    designDp = newDesign
                    fitDisplayMetrics(resources)
                }
            }
        }
    }

    class Builder {
        internal var fontScale: Boolean = false
        internal var displayOrientation = ORIENTATION_WIDTH
        internal var designDp: Float = 360.0f

        /**
         * @param isOpen 是否开启字体适配
         */
        fun openFontScale(isOpen: Boolean) = apply {
            this.fontScale = isOpen
        }

        /**
         * @param design 设计dp尺寸 default 360
         */
        fun designDp(design: Float) = apply {
            this.designDp = design
        }

        /**
         * @param byWidth 适配维度 default 横向
         */
        fun fitDisplayOrientation(@Orientation byWidth: Int) = apply {
            this.displayOrientation = byWidth
        }

        fun build() = FitDisplayMetrics(this)
    }

    internal data class DensityConfig(val density: Float, val scaledDensity: Float, val densityDpi: Int) {
        fun apply(resources: Resources) = run {
            resources.displayMetrics.density = density
            resources.displayMetrics.scaledDensity = scaledDensity
            resources.displayMetrics.densityDpi = densityDpi
            resources
        }
    }

    @IntDef(ORIENTATION_WIDTH, ORIENTATION_HEIGHT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Orientation
}
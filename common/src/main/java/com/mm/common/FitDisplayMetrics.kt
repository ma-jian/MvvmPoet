package com.mm.common

import android.content.ComponentCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.IntDef

/**
 * Created by : m
 * Date : 4/20/21
 * Describe : 指定屏幕宽高适配，支持动态恢复系统配置
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
        private var displayOrientation = ORIENTATION_WIDTH

        /**
         * @param context 初始化
         * @param block
         */
        @Suppress("DEPRECATION")
        @JvmStatic
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
                displayOrientation = builder.displayOrientation
            }

        /**
         * @param resources 需要适配的资源
         * @param orientation 适配维度 default 横向
         */
        @JvmStatic
        fun fitDisplayMetrics(
            resources: Resources,
            @Orientation orientation: Int = displayOrientation,
            newDesign: Float = designDp
        ) = run {
            require(screen.size == 2) { "请先初始化build()" }
            if (orientation != displayOrientation || designDp != newDesign) {
                val displayPx = if (orientation == ORIENTATION_WIDTH) screen[0] else screen[1]
                if (designDp != newDesign) {
                    designDp = newDesign
                }
                val density = displayPx.times(1.0f).div(designDp)
                val densityDpi = density.times(160).toInt()
                DensityConfig(
                    density, if (isOpenScale) density.times(fontScale) else defaultDisplay.scaledDensity, densityDpi
                ).apply(resources)
            } else {
                resources
            }
        }

        @JvmStatic
        @JvmName("restNewDisplayMetrics")
        fun restDisplayMetrics(context: Context, newDesign: Float = 0f) =
            context.restDisplayMetrics(newDesign)

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
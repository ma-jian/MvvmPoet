package com.mm.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.mm.common.R
import com.mm.common.databinding.LayoutToolbarBinding
import com.mm.common.utils.StatusBarUtil
import com.mm.common.utils.dp2px
import java.lang.reflect.Method

/**
 * Created by : m
 * @since 1.0
 * 自定义Toolbar
 */

class MToolbar constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {
    private var mBinding: LayoutToolbarBinding = LayoutToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    var centerTitle: CharSequence? = null
        set(value) {
            mBinding.title.text = value ?: ""
            field = value
        }
        get() = mBinding.title.text

    @StringRes
    var centerTitleRes: Int = 0
        set(value) {
            mBinding.title.text = context.getText(value)
            field = value
        }

    @ColorInt
    var centerTitleColor: Int = ContextCompat.getColor(context, R.color.color_080707)
        set(value) {
            mBinding.title.setTextColor(value)
            field = value
        }
        get() = mBinding.title.textColors.defaultColor

    var centerTitleSize: Float = 16f
        set(value) {
            mBinding.title.textSize = value
            field = value
        }
        get() = mBinding.title.textSize

    var centerTitleMaxEms: Int = 8
        set(value) {
            mBinding.title.maxEms = value
            field = value
        }
        get() = mBinding.title.maxEms


    var centerTitleStyle: Int = Typeface.NORMAL
        set(value) {
            setCenterTypeStyle(value)
            field = value
        }
        get() = mBinding.title.paint.typeface.style

    var title: CharSequence? = null
        set(value) {
            mBinding.customToolbar.title = value ?: ""
            field = value
        }
        get() = mBinding.customToolbar.title

    @StringRes
    var titleRes: Int = 0
        set(value) {
            mBinding.customToolbar.title = context.getText(value)
            field = value
        }

    @StyleRes
    var titleTextAppearance: Int = 0
        set(value) {
            mBinding.customToolbar.setTitleTextAppearance(context, titleTextAppearance)
            field = value
        }

    @StyleRes
    var popupTheme: Int = 0
        set(value) {
            mBinding.customToolbar.popupTheme = value
            field = value
        }
        get() = mBinding.customToolbar.popupTheme

    var titleMarginStart: Int = 0
        set(value) {
            mBinding.customToolbar.titleMarginStart = value
            field = value
        }
        get() = mBinding.customToolbar.titleMarginStart

    var titleMarginTop: Int = 0
        set(value) {
            mBinding.customToolbar.titleMarginTop = value
            field = value
        }
        get() = mBinding.customToolbar.titleMarginTop

    var titleMarginEnd: Int = 0
        set(value) {
            mBinding.customToolbar.titleMarginEnd = value
            field = value
        }
        get() = mBinding.customToolbar.titleMarginEnd

    var titleMarginBottom: Int = 0
        set(value) {
            mBinding.customToolbar.titleMarginBottom = value
            field = value
        }
        get() = mBinding.customToolbar.titleMarginBottom

    var onRtlPropertiesChanged: Int = LayoutDirection.LTR
        set(value) = mBinding.customToolbar.onRtlPropertiesChanged(value)

    @DrawableRes
    var logo: Int = 0
        set(value) {
            mBinding.customToolbar.logo = AppCompatResources.getDrawable(context, value)
            field = value
        }

    val isOverflowMenuShowing: Boolean
        get() = mBinding.customToolbar.isOverflowMenuShowing

    val showOverflowMenu: Boolean
        get() = mBinding.customToolbar.showOverflowMenu()

    val hideOverflowMenu: Boolean
        get() = mBinding.customToolbar.hideOverflowMenu()

    val dismissPopupMenus: Unit
        get() = mBinding.customToolbar.dismissPopupMenus()

    val hasExpandedActionView: Boolean
        get() = mBinding.customToolbar.hasExpandedActionView()

    var navigationIcon: Drawable? = null
        set(value) {
            mBinding.customToolbar.navigationIcon = value
            field = value
        }

    var navigationOnClickListener: OnClickListener? = null
        set(value) {
            mBinding.customToolbar.setNavigationOnClickListener {
                navigationOnClickListener?.onClick(it)
            }
            field = value
        }

    var titleOnClickListener: OnClickListener? = null
        set(value) {
            mBinding.title.setOnClickListener() {
                titleOnClickListener?.onClick(it)
            }
            field = value
        }

    var collapseIcon: Drawable? = null
        set(value) {
            mBinding.customToolbar.collapseIcon = value
            field = value
        }

    var onMenuItemClickListener: Toolbar.OnMenuItemClickListener? = null
        set(value) {
            mBinding.customToolbar.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener value?.onMenuItemClick(it) ?: false
            }
            field = value
        }

    fun setTitleMargin(start: Int, top: Int, end: Int, bottom: Int) {
        mBinding.customToolbar.setTitleMargin(start, top, end, bottom)
    }

    fun setSupportActionBar(activity: AppCompatActivity) {
        activity.setSupportActionBar(mBinding.customToolbar)
    }

    fun getSupportActionBar(): Toolbar {
        return mBinding.customToolbar
    }

    fun collapseActionView() {
        mBinding.customToolbar.collapseActionView()
    }

    fun inflateMenu(@MenuRes resId: Int) {
        mBinding.customToolbar.inflateMenu(resId)
    }

    @SuppressLint("ResourceType")
    fun setTitleAndImage(titles: String, @DrawableRes resId: Int) {
        if (!TextUtils.isEmpty(titles)) mBinding.title.text = titles
        val drawable = ContextCompat.getDrawable(context, resId)
        mBinding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    fun setTitleOnCilckListener(titleOnClickListener: OnClickListener) {
        this.titleOnClickListener = titleOnClickListener
    }

    fun getMenu(): Menu? {
        return mBinding.customToolbar.menu
    }

    fun getToolbar() = mBinding.customToolbar

    fun setCenterTypeStyle(style: Int) {
        mBinding.title.paint.typeface = Typeface.defaultFromStyle(style)
    }

    fun setToolbarBackground(background: Drawable?) {
        mBinding.customToolbar.background = background
    }

    /**
     * 设置setNavigationIcon LayoutParams
     * @param params
     */
    fun setNavigationViewParams(params: Toolbar.LayoutParams) {
        getNavigationView()?.let {
            it.layoutParams = params
        }
    }

    fun getNavigationViewParams(): Toolbar.LayoutParams {
        return getNavigationView()?.layoutParams?.let {
            (it as Toolbar.LayoutParams).apply {
                width = context.dp2px(28f)
                height = context.dp2px(28f)
                marginStart = context.dp2px(9f)
            }
        } ?: Toolbar.LayoutParams(context.dp2px(28f), context.dp2px(28f)).also {
            it.gravity = GravityCompat.START or (Gravity.TOP and Gravity.VERTICAL_GRAVITY_MASK)
            it.marginStart = context.dp2px(9f)
        }
    }

    /**
     * 获取title
     */
    fun getTitleTextView(): TextView? {
        return getField(
            mBinding.customToolbar::class.java, mBinding.customToolbar, "mTitleTextView"
        )
    }

    /**
     * 获取 NavigationView
     */
    fun getNavigationView(): ImageView? {
        getMethod(mBinding.customToolbar::class.java, "ensureNavButtonView")?.invoke(mBinding.customToolbar)
        return getField(
            mBinding.customToolbar::class.java, mBinding.customToolbar, "mNavButtonView"
        )
    }

    init {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MToolbar, defStyleAttr, 0)
        val title = ta.getString(R.styleable.MToolbar_title) ?: ""
        val defaultTitle = ta.getString(R.styleable.MToolbar_center_title) ?: ""
        val centerColor = ta.getColor(
            R.styleable.MToolbar_center_textColor,
            ContextCompat.getColor(context, R.color.color_080707)
        )
        val maxEms = ta.getInt(R.styleable.MToolbar_center_maxEms, 8)
        val navigationIconCircle = ta.getBoolean(R.styleable.MToolbar_navigationIconCircle, false)
        val textSize = ta.getDimension(R.styleable.MToolbar_center_textSize, 16f)
        val typeface = when (ta.getInteger(R.styleable.MToolbar_center_titleStyle, Typeface.BOLD)) {
            0 -> Typeface.NORMAL
            1 -> Typeface.BOLD
            2 -> Typeface.ITALIC
            3 -> Typeface.BOLD_ITALIC
            else -> Typeface.NORMAL
        }
        val toolbarBackground = ta.getResourceId(R.styleable.MToolbar_android_background, R.color.white)
        val navigationIcon = ta.getDrawable(R.styleable.MToolbar_navigationIcon)
        val fitsSystemWindows = ta.getBoolean(R.styleable.MToolbar_android_fitsSystemWindows, true)
        ta.recycle()
        mBinding.customToolbar.title = title
//        mBinding.toolbar.subtitle = title
        mBinding.title.text = defaultTitle
        centerTitleColor = centerColor
        centerTitleMaxEms = maxEms
        centerTitleSize = textSize
        setCenterTypeStyle(typeface)
        setBackgroundResource(toolbarBackground)
        orientation = VERTICAL
        if (fitsSystemWindows) {
            val stationView = View(context)
            stationView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, StatusBarUtil.getStatusBarHeight(context))
            addView(stationView, 0)
        }
//        setToolbarBackground(ContextCompat.getDrawable(context, toolbarBackground))
        getNavigationView()?.let {
            it.layoutParams = getNavigationViewParams()
            if (navigationIconCircle) {
                val shapeModel =
                    ShapeAppearanceModel.builder().setAllCorners(RoundedCornerTreatment())
                        .setAllCornerSizes(48f).build()
                it.background = MaterialShapeDrawable(shapeModel).apply {
                    paintStyle = Paint.Style.FILL
                    fillColor = ColorStateList.valueOf(Color.TRANSPARENT)
                }
            }
            mBinding.customToolbar.navigationIcon = navigationIcon
        }
        getMenu()?.apply {
            val menuView = getField<ActionMenuView>(mBinding.customToolbar::class.java, mBinding.customToolbar, "mMenuView")
            menuView?.let {
                it.layoutParams = (it.layoutParams as Toolbar.LayoutParams).apply {
                    marginEnd = context.dp2px(6f)
                }
            }
        }
    }

    @Suppress("SameParameterValue", "UNCHECKED_CAST")
    private fun <T> getField(targetClass: Class<*>, instance: Any, fieldName: String): T? {
        try {
            val field = targetClass.getDeclaredField(fieldName)
            field.isAccessible = true
            return field[instance] as T
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }


    @Suppress("SameParameterValue", "UNCHECKED_CAST")
    private fun getMethod(
        targetClass: Class<*>, methodName: String, vararg parameterTypes: Class<*>
    ): Method? {
        try {
            val method = targetClass.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
            return method
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("SameParameterValue", "UNCHECKED_CAST")
    private fun <T> invokeMethod(method: Method, instance: Any, vararg args: Any): T? {
        try {
            method.isAccessible = true
            return method.invoke(instance, args) as T?
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

}
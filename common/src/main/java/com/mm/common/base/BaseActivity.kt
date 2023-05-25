package com.mm.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.mm.common.FitDisplayMetrics
import com.mm.common.etoast.ToastCompat
import com.mm.common.utils.StatusBarUtil
import com.mm.common.utils.inflateBindingWithGeneric
import com.mm.common.vm.IViewInterface
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * Created by : m
 * @since 1.0
 * 基类activity
 */

open class BaseActivity<VB : ViewBinding> : AppCompatActivity(), IViewInterface {
    lateinit var mBinding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation()
        }
        super.onCreate(savedInstanceState)
        mBinding = inflateBindingWithGeneric(layoutInflater)
        if (this::mBinding.isInitialized) {
            FitDisplayMetrics.fitDisplayMetrics(resources)
            setContentView(mBinding.root)
        }
        StatusBarUtil.setTransparentForWindow(this)
        StatusBarUtil.setDarkMode(this)
    }

    /**
     * 生成ViewModel
     */
    override fun <T : ViewModel> createViewModel(clazz: Class<T>): T {
        return defaultViewModelProviderFactory.create(clazz)
    }

    override fun showToast(message: CharSequence?) {
        if (!TextUtils.isEmpty(message)) {
            ToastCompat.makeCenterText(message!!).show()
        }
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return
        }
        super.setRequestedOrientation(requestedOrientation)
    }


    @SuppressLint("PrivateApi")
    private fun isTranslucentOrFloating(): Boolean {
        var isTranslucentOrFloating = false
        try {
            val styleableRes = Class.forName("com.android.internal.R\$styleable").getField("Window")[null] as IntArray
            val ta = obtainStyledAttributes(styleableRes)
            val m: Method = ActivityInfo::class.java.getMethod(
                "isTranslucentOrFloating", TypedArray::class.java
            )
            m.isAccessible = true
            isTranslucentOrFloating = m.invoke(null, ta) as Boolean
            m.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isTranslucentOrFloating
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun fixOrientation(): Boolean {
        try {
            val field: Field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val o = field.get(this) as ActivityInfo
            o.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            field.isAccessible = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
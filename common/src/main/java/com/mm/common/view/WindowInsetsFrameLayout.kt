package com.mm.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat

/**
 * Created by : m
 * Date : 2022/2/25
 * 单activity 多fragment fitsSystemWindows被消费后不在下发的问题
 */
class WindowInsetsFrameLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(
        context!!, attrs, defStyleAttr
    ) {
    init {
        setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                requestApplyInsets()
            }

            override fun onChildViewRemoved(parent: View, child: View) {}
        })
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        var result = super.dispatchApplyWindowInsets(insets)
        if (!insets.isConsumed) {
            val count = childCount
            for (i in 0 until count) {
                result = getChildAt(i).dispatchApplyWindowInsets(insets)
            }
        }
        return result
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val childCount = childCount
        for (index in 0 until childCount) {
            getChildAt(index).dispatchApplyWindowInsets(insets)
        }
        return insets
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        ViewCompat.requestApplyInsets(child)
    }
}
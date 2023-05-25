package com.mm.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mm.common.R
import com.mm.common.utils.inflateBindingWithGeneric
import com.mm.common.utils.screenHeight
import com.mm.common.utils.windowStatusBarHeight


/**
 * Created by : m
 * Describe : 基类底部弹出dialog
 * @since 1.0
 */

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {
    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    lateinit var mBinding: VB
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = inflateBindingWithGeneric(inflater, container, attachToParent = false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        ensureContainerAndBehavior()
    }

    fun getBehavior(): BottomSheetBehavior<FrameLayout>? {
        if (behavior == null) {
            // The content hasn't been set, so the behavior doesn't exist yet. Let's create it.
            ensureContainerAndBehavior()
        }
        return behavior
    }

    private fun ensureContainerAndBehavior() {
        dialog?.let {
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            behavior = BottomSheetBehavior.from(bottomSheet)
            if (isMatchParent()) {
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                //设置展开状态
                behavior?.peekHeight = requireContext().screenHeight() - requireContext().windowStatusBarHeight
                behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    //是否全部展开沾满全屏
    open fun isMatchParent() = true
}
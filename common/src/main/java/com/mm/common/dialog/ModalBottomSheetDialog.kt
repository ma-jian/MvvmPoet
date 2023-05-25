package com.mm.common.dialog

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.mm.common.ActivityDelegate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mm.common.R
import com.mm.common.utils.screenHeight


/**
 * Created by : m
 * @since 1.0
 * 模态底部弹窗
 */

abstract class ModalBottomSheetDialog<T : ViewBinding> : BaseBottomSheetDialogFragment<T>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ModalBottomSheetDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        getBehavior()?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                WindowModalAnimator.bottomSlideOffset(slideOffset)
            }
        })

        dialog?.setOnDismissListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            //设置弹出高度
            getBehavior()?.setPeekHeight(requireContext().screenHeight(), true)
            //设置展开状态
            getBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
            ActivityDelegate.delegate.get()?.activity?.let {
                it.window?.decorView?.findViewById<View>(android.R.id.content)?.let { window ->
                    bottomSheet.layoutParams.height = window.height - (window.height * (1 - 0.94f) * 0.7).toInt()
                }
            }
        }
    }

    override fun dismiss() {
        WindowModalAnimator.hideAnimation()
        super.dismiss()
    }

    override fun dismissAllowingStateLoss() {
        WindowModalAnimator.hideAnimation()
        super.dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
//        super.show(manager, tag)
        try {
            val mClass = DialogFragment::class.java
            val shownByMe = mClass.getDeclaredField("mShownByMe")
            shownByMe.isAccessible = true
            if (!(shownByMe.get(this) as Boolean)) {
                WindowModalAnimator.showAnimation()
                shownByMe.set(this, true)
                val dismissed = mClass.getDeclaredField("mDismissed")
                dismissed.isAccessible = true
                dismissed.set(this, false)
                val ft = manager.beginTransaction()
                ft.add(this, tag)
                ft.commitAllowingStateLoss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
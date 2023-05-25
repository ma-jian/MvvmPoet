package com.mm.common.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.os.bundleOf
import com.mm.common.databinding.DialogCommonBinding
import com.mm.common.utils.setOneClickListener

/**
 * Created by : m
 * @since 1.0
 */

class CommonDialogFragment : BaseCompatDialogFragment<DialogCommonBinding>() {
    var onClickListener: View.OnClickListener? = null

    companion object {
        const val TITLE = "data_title"
        const val DATA = "data_message"
        const val BTN_LEFT = "btn_left"
        const val BTN_RIGHT = "btn_right"
        const val SINGLE = "single"

        @JvmStatic
        fun newCommonDialog(title: CharSequence, content: CharSequence): CommonDialogFragment {
            return CommonDialogFragment().apply {
                arguments = bundleOf(DATA to content, TITLE to title)
            }
        }

        @JvmStatic
        fun newCommonDialog(content: CharSequence): CommonDialogFragment {
            return newCommonDialog("", content)
        }

        @JvmStatic
        fun newCommonDialog(
            content: CharSequence, btnRight: CharSequence = "", btnLeft: CharSequence = ""
        ): CommonDialogFragment {
            return CommonDialogFragment().apply {
                arguments = bundleOf(DATA to content, BTN_LEFT to btnLeft, BTN_RIGHT to btnRight)
            }
        }

        @JvmStatic
        fun newSingleCommonDialog(content: CharSequence): CommonDialogFragment {
            return CommonDialogFragment().apply {
                arguments = bundleOf(SINGLE to true, DATA to content)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString(TITLE) ?: ""
        val message = arguments?.getString(DATA) ?: ""
        val btnLeft = arguments?.getString(BTN_LEFT) ?: ""
        val btnRight = arguments?.getString(BTN_RIGHT) ?: ""
        val single = arguments?.getBoolean(SINGLE, false) ?: false
        mBinding.content.text = message
        if (!TextUtils.isEmpty(title)) {
            mBinding.title.text = title
            mBinding.title.visibility = View.VISIBLE
        } else {
            mBinding.title.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(btnLeft)) {
            mBinding.btnCancel.text = btnLeft
        }
        if (!TextUtils.isEmpty(btnRight)) {
            mBinding.btnCancel.text = btnRight
        }
        mBinding.btnCancel.visibility = if (single) View.GONE else View.VISIBLE
        mBinding.midLine.visibility = if (single) View.GONE else View.VISIBLE


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        mBinding.btnCancel.setOneClickListener {
            onClickListener?.onClick(it)
            dismissAllowingStateLoss()
        }

        mBinding.btnOk.setOneClickListener {
            onClickListener?.onClick(it)
            dismissAllowingStateLoss()
        }
    }

}

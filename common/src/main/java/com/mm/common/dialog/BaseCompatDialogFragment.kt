package com.mm.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.viewbinding.ViewBinding
import com.mm.common.R
import com.mm.common.utils.inflateBindingWithGeneric

/**
 * Created by : m
 * @since 1.0 基础弹窗dialog
 */
abstract class BaseCompatDialogFragment<VB : ViewBinding> : AppCompatDialogFragment() {
    lateinit var mBinding: VB
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = inflateBindingWithGeneric(inflater, container, attachToParent = false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CommonDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.addFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}
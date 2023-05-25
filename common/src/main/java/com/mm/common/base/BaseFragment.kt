package com.mm.common.base

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.mm.common.etoast.ToastCompat
import com.mm.common.utils.inflateBindingWithGeneric
import com.mm.common.vm.IViewInterface

/**
 * Created by : m
 * @since 1.0
 */

open class BaseFragment<VB : ViewBinding> : Fragment(), IViewInterface {
    lateinit var mBinding: VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        mBinding = inflateBindingWithGeneric(inflater, container, false)
        return mBinding.root
    }

    override fun <T : ViewModel> createViewModel(clazz: Class<T>): T {
        return defaultViewModelProviderFactory.create(clazz)
    }

    override fun showToast(message: CharSequence?) {
        if (!TextUtils.isEmpty(message)) {
            ToastCompat.makeCenterText(message!!).show()
        }
    }
}
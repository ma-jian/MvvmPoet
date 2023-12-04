package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.mm.common.base.BaseFragment
import com.mm.module_1.Module1Service
import com.mm.module_2.Module2Service
import com.mm.mvvmpoet.databinding.Fragment3LayoutBinding
import com.mm.router.Router

/**
 * Created by : m
 * Date : 4/23/21
 * Describe :
 */

class Fragment3 : BaseFragment<Fragment3LayoutBinding>() {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.text.text = "Fragment3"

        mBinding.module1.setOnClickListener {
            val service = Router.init(this).open("module1/service").doProvider<Module1Service>()
            val moduleName = service?.moduleName()
            val version = service?.version()
            mBinding.text.text = mBinding.text.text.toString() + "\nmoduleName:$moduleName ;version:$version"
        }

        mBinding.module2.setOnClickListener {
            val service = Router.init(this).open("module2/service").doProvider<Module2Service>()
            val moduleName = service?.moduleName()
            mBinding.text.text = mBinding.text.text.toString() + "\nmoduleName2:$moduleName"
            service?.module2Log("Fragment3 传递日志信息")
        }
    }
}
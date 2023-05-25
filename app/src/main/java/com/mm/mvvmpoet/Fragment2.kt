package com.mm.mvvmpoet

import android.os.Bundle
import android.view.View
import com.mm.common.base.BaseFragment
import com.mm.mvvmpoet.databinding.Fragment2LayoutBinding
import com.mm.router.Router

/**
 * Created by : m
 * Date : 4/23/21
 * Describe :
 */

class Fragment2 : BaseFragment<Fragment2LayoutBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.title.text = "Fragment2"

        mBinding.module1.setOnClickListener {
            val string = mBinding.editPath.text.toString()
            Router.init(this).open("module_1").withString("message", string).navigation()
        }

        mBinding.module2.setOnClickListener {
            val string = mBinding.editPath.text.toString()
            Router.init(this).open("module_2").withString("message", string).navigation()
        }

        mBinding.second.setOnClickListener {
            Router.init(this).open("second_activity").navigation()
        }
    }
}
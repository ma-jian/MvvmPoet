package com.mm.module_2

import android.os.Bundle
import android.view.View.OnClickListener
import com.mm.annotation.RouterPath
import com.mm.common.base.BaseActivity
import com.mm.module_1.Module1Service
import com.mm.module_2.databinding.ActivityModule2Binding
import com.mm.router.Router


/**
 * Date : 2023/5/12
 */
@RouterPath("module_2")
class Module2Activity : BaseActivity<ActivityModule2Binding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.toolbar.setSupportActionBar(this)
        mBinding.toolbar.navigationOnClickListener = OnClickListener { finish() }

        mBinding.message.text = intent.getStringExtra("message")

        mBinding.openModule1.setOnClickListener {
            Router.init(this).open("module_1").withString("message", mBinding.editQuery.text.toString()).navigation()
        }

        mBinding.module1Service.setOnClickListener {
            val module1 = Router.init(this).open(Module1Service::class.java).doProvider<Module1Service>()
            val moduleName = module1?.moduleName()
            val version = module1?.version()
            mBinding.message.text = mBinding.message.text.toString() + "\nmodule1Name:$moduleName ;version:$version"
        }
    }
}
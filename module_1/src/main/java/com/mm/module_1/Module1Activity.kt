package com.mm.module_1

import android.os.Bundle
import android.view.View.OnClickListener
import com.mm.annotation.RouterPath
import com.mm.common.base.BaseActivity
import com.mm.module_1.databinding.ActivityModule1Binding
import com.mm.router.Router


/**
 * Date : 2023/5/12
 */
@RouterPath("module_1")
class Module1Activity : BaseActivity<ActivityModule1Binding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.toolbar.setSupportActionBar(this)
        mBinding.toolbar.navigationOnClickListener = OnClickListener { finish() }

        mBinding.message.text =  intent.getStringExtra("message")

        mBinding.openModule2.setOnClickListener {
            Router.init(this).open("module_2").withString("message", mBinding.editQuery.text.toString()).navigation()
        }
    }
}
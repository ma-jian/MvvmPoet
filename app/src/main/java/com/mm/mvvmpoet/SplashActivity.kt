package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.os.Bundle
import com.mm.common.base.BaseActivity
import com.mm.mvvmpoet.databinding.ActivitySplashBinding
import com.mm.router.Router


/**
 * Date : 2023/6/8
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Router.init(this).open("module_main").navigation {
            finish()
        }
    }
}
package com.mm.common.crash

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.mm.common.base.BaseActivity
import com.mm.common.databinding.ActivityCrashhandlerBinding


/**
 * Date : 2023/3/2
 * @since 1.0
 */
class CrashHandlerActivity : BaseActivity<ActivityCrashhandlerBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.toolbar.setSupportActionBar(this)
        mBinding.toolbar.navigationOnClickListener = View.OnClickListener { finish() }
        val throwable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("throwable",Throwable::class.java)
        } else {
            intent.getSerializableExtra("throwable") as Throwable
        }
        val message = Log.getStackTraceString(throwable)
        mBinding.message.text = message
        if (!TextUtils.isEmpty(message)) {
            try {
                mBinding.toolbar.title = "(?<=\\{).*(?=\\})".toRegex().find(message)?.value
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
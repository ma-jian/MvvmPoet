package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.os.Bundle
import com.mm.annotation.RouterPath
import com.mm.common.base.BaseActivity
import com.mm.mvvmpoet.databinding.ActivityMainBinding
import kotlinx.coroutines.*

/**
 * 主页面
 */
@RouterPath("module_main")
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val fragments = arrayListOf(Fragment1(), Fragment2(), Fragment3())
    private var preIndex: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFragment()
        mBinding.navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_0 -> switch(0)
                R.id.tab_1 -> switch(1)
                R.id.tab_2 -> switch(2)
            }
            true
        }
    }

    private fun initFragment() {
        fragments.forEachIndexed { index, frag ->
            if (!frag.isAdded) {
                val transaction = supportFragmentManager.beginTransaction().add(R.id.fragment_container, frag, frag.tag)
                if (index == 0) {
                    transaction.show(frag).commitAllowingStateLoss()
                    preIndex = 0
                } else {
                    transaction.hide(frag).commitAllowingStateLoss()
                }
            }
        }
    }

    private fun switch(index: Int) {
        val fragment = fragments[index]
        val preFragment = if (preIndex < fragments.size) fragments[preIndex] else fragments[0]
        supportFragmentManager.beginTransaction().hide(preFragment).show(fragment).commitAllowingStateLoss()
        preIndex = index
    }
}
package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mm.common.base.BaseActivity
import com.mm.mvvmpoet.databinding.ActivitySecondBinding
import com.mm.router.annotation.RouterPath

/**
 * Created by : m
 * Date : 4/16/21
 * Describe :
 */
@RouterPath("second_activity", des = "SecondActivity")
class SecondActivity : BaseActivity<ActivitySecondBinding>() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val beginTransaction = supportFragmentManager.beginTransaction()
        val tab1 = Fragment1()
        val tab2 = Fragment3()
        beginTransaction.add(R.id.container, tab1, "tab1")
        beginTransaction.add(R.id.container, tab2, "tab2")
        beginTransaction.show(tab1).hide(tab2).commit()
        var showing: Fragment = tab1
        findViewById<View>(R.id.tab1).setOnClickListener {
            supportFragmentManager.beginTransaction().hide(showing).show(tab1).commit()
            showing = tab1
        }
        findViewById<View>(R.id.tab2).setOnClickListener {
            supportFragmentManager.beginTransaction().hide(showing).show(tab2).commit()
            showing = tab2
        }
    }

}
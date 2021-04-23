package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mm.lib_util.FitDisplayMetrics.Companion.restDisplayMetrics
import com.mm.lib_util.etoast.ToastGlobal
import kotlinx.android.synthetic.main.activity_second.*

/**
 * Created by : majian
 * Date : 4/16/21
 * Describe :
 */
class SecondActivity : FragmentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        //        findViewById<TextView>(R.id.textView).text = "hello，world"
        textView.text = "hello，world"

        textView.setOnClickListener { view ->
            println("$view")
            AlertDialog.Builder(this.restDisplayMetrics()).apply {
                setTitle("我是dialog1")
                setMessage("dialog \ndensity :${resources.displayMetrics?.density} ;\ndensityDpi :${resources.displayMetrics?.densityDpi} ;\nscaledDensity :${resources.displayMetrics?.scaledDensity}")
                setPositiveButton("确定") { _, _ ->
                    ToastGlobal.show("我是dialog1 消失了")
                }
            }.create().show()
        }
        val beginTransaction = supportFragmentManager.beginTransaction()
        val fragment1 = Fragment1()
        val fragment2 = Fragment2()
        beginTransaction.add(R.id.container, fragment1, "Fragment1")
        beginTransaction.add(R.id.container, fragment2, "Fragment2")
        beginTransaction.show(fragment1).hide(fragment2).commit()
        var showing: Fragment = fragment1
        findViewById<View>(R.id.tab1).setOnClickListener {
            supportFragmentManager.beginTransaction().hide(showing).show(fragment1).commit()
            showing = fragment1
        }
        findViewById<View>(R.id.tab2).setOnClickListener {
            supportFragmentManager.beginTransaction().hide(showing).show(fragment2).commit()
            showing = fragment2
        }
    }

}
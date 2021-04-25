package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.mm.lib_http.RetrofitGlobal
import com.mm.lib_http.asCallFlow
import com.mm.lib_http.emitFlow
import com.mm.lib_util.DialogQueue
import com.mm.lib_util.FitDisplayMetrics
import com.mm.lib_util.FitDisplayMetrics.Companion.restDisplayMetrics
import com.mm.lib_util.etoast.ToastGlobal
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.name
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resources: Resources = FitDisplayMetrics.fitDisplayMetrics(resources)
        setContentView(R.layout.activity_main)
        val editText = findViewById<EditText>(R.id.editName)
        val textView = findViewById<TextView>(R.id.textView)

        val exception = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            textView.text = Log.getStackTraceString(throwable)
        }

        findViewById<View>(R.id.retrofit).setOnClickListener {
            val create = RetrofitGlobal.create<DemoService>()
            GlobalScope.launch(Dispatchers.Main + exception) {
                create.getUser(editText.text.toString()).asCallFlow().collect {
                    textView.text = "${Thread.currentThread().name} : $it"
                }
            }
        }

        findViewById<View>(R.id.second).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
        findViewById<View>(R.id.flow).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main + exception) {
                emitFlow {
                    data()
                }.collect {
                    textView.text = "${Thread.currentThread().name} : $it"
                }
            }
        }

        findViewById<View>(R.id.toast).setOnClickListener {
            ToastGlobal.showByQueue("我是toast")
            ToastGlobal.showByQueue("我是自定义Toast", R.layout.custom_toast_view_success)
            ToastGlobal.showByQueue("我是自定义Toast2") {
                duration(1000)
            }
        }

        textView.text = "density :${resources.displayMetrics.density} ; densityDpi :${resources.displayMetrics.densityDpi} ; scaledDensity :${resources.displayMetrics?.scaledDensity} "
        findViewById<View>(R.id.dialog).setOnClickListener {
            val dialog = AlertDialog.Builder(this.restDisplayMetrics()).apply {
                title = "我是dialog"
                setMessage("dialog \ndensity :${resources.displayMetrics?.density} ;\ndensityDpi :${resources.displayMetrics?.densityDpi} ;\nscaledDensity :${resources.displayMetrics?.scaledDensity}")
                setPositiveButton("确定") { _, _ ->
                    ToastGlobal.show("我是dialog弹窗")
                }
            }.create()
            dialog.show()
        }

        findViewById<View>(R.id.dialogQueue).setOnClickListener {
            createdialog()
        }
        findViewById<View>(R.id.javaActivity).setOnClickListener {
            startActivity(Intent(this, JActivity::class.java))
        }
    }

    private fun createdialog() {
        val dialog = AlertDialog.Builder(this.restDisplayMetrics()).apply {
            setTitle("我是dialog1")
            setMessage("dialog \ndensity :${resources.displayMetrics?.density} ;\ndensityDpi :${resources.displayMetrics?.densityDpi} ;\nscaledDensity :${resources.displayMetrics?.scaledDensity}")
            setPositiveButton("确定") { _, _ ->
                ToastGlobal.show("我是dialog1 消失了")
            }
        }.create()
        DialogQueue.addDialog(dialog, 1) {
            targetActivity = { arrayListOf(SecondActivity::class.java) }
        }

        val dialog2 = AlertDialog.Builder(this.restDisplayMetrics()).apply {
            setTitle("我是dialog2")
            setMessage("dialog \ndensity :${resources.displayMetrics?.density} ;\ndensityDpi :${resources.displayMetrics?.densityDpi} ;\nscaledDensity :${resources.displayMetrics?.scaledDensity}")
            setPositiveButton("确定") { _, _ ->
                ToastGlobal.show("我是dialog2 消失了")
            }
        }.create()
        DialogQueue.addDialog(dialog2, 1)

        val dialog3 = AlertDialog.Builder(this.restDisplayMetrics()).apply {
            setTitle("我是dialog3")
            setMessage("dialog \ndensity :${resources.displayMetrics?.density} ;\ndensityDpi :${resources.displayMetrics?.densityDpi} ;\nscaledDensity :${resources.displayMetrics?.scaledDensity}").setNegativeButton("取消") { _, _ ->
                ToastGlobal.show("我是dialog3 消失了")
            }
        }.create()
        DialogQueue.addDialog(dialog3, 2) {
            override = true
        }

        val dialog4 = DialogFragment(R.layout.custom_toast_view_success)
        DialogQueue.addDialog(dialog4, 1) {
            delay = 1000
            targetActivity = { arrayListOf(SecondActivity::class.java) }
            targetFragment = { arrayListOf(Fragment2::class.java) }
        }

        val dialog5 = DialogFragment(R.layout.custom_toast_view_success)
        DialogQueue.addDialog(dialog5, 5)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e(TAG, "onConfigurationChanged :${resources.displayMetrics.density} ; densityDpi :${resources.displayMetrics.densityDpi} ; scaledDensity :${resources.displayMetrics.scaledDensity} ")

    }

    private suspend fun data(): String {
        delay(3000)
        return "我是消息@@@@ 我迟来了 3秒"
    }


    override fun onBackPressed() {
        //        super.onBackPressed()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}
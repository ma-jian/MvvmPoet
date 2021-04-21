package com.mm.mvvmpoet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mm.lib_http.RetrofitGlobal
import com.mm.mvvmpoet.FitDisplayMetrics.Companion.restDisplayMetrics
import com.mm.mvvmpoet.etoast.ToastGlobal
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.name

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val editText = findViewById<EditText>(R.id.editName)
        val textView = findViewById<TextView>(R.id.textView)
        findViewById<View>(R.id.retrofit).setOnClickListener {
            val create = RetrofitGlobal.create<DemoService>()
            create.getUser(editText.text.toString()).enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        textView.text = response.body().toString()
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                }
            })
        }

        findViewById<View>(R.id.second).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        val exception = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            textView.text = Log.getStackTraceString(throwable)
        }
        findViewById<View>(R.id.flow).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main + exception) {
                flowEmit {
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

        val resources: Resources = FitDisplayMetrics.fitDisplayMetrics(resources)
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e(TAG, "onConfigurationChanged :${resources.displayMetrics.density} ; densityDpi :${resources.displayMetrics.densityDpi} ; scaledDensity :${resources.displayMetrics.scaledDensity} ")

    }

    private suspend fun data(): String {
        delay(3000)
        return "我是消息@@@@"
    }


    suspend fun <T> flowEmit(block: suspend () -> T) = supervisorScope {
        flow {
            emit(block.invoke())
        }.onEach {
            throw IllegalStateException("我是flow抛出异常")
        }.flowOn(Dispatchers.IO).catch { e ->
            currentCoroutineContext().apply {
                this[CoroutineExceptionHandler]?.let {
                    it.handleException(this, e)
                }
            }
        }
    }
}
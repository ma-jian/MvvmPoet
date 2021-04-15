package com.mm.mvvmpoet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.mm.lib_http.RetrofitConfiguration
import com.mm.lib_http.RetrofitGlobal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val editText = findViewById<EditText>(R.id.editTextTextPersonName)
        val textView = findViewById<TextView>(R.id.textView)
        findViewById<View>(R.id.button).setOnClickListener {
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

    }
}
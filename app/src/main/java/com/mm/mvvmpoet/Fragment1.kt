package com.mm.mvvmpoet


import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.mm.common.base.BaseFragment
import com.mm.common.http.LogInterceptor
import com.mm.common.utils.LogUtil
import com.mm.http.DynamicHostInterceptor
import com.mm.http.HOST
import com.mm.http.ResponseConverter
import com.mm.http.RetrofitCache
import com.mm.http.asResultFlow
import com.mm.http.cache.CacheHelper
import com.mm.http.cache.StrategyType
import com.mm.http.uiScope
import com.mm.mvvmpoet.databinding.Fragment1LayoutBinding
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Call
import retrofit2.await

/**
 * Created by : m
 * Date : 4/23/21
 * Describe :
 */

class Fragment1 : BaseFragment<Fragment1LayoutBinding>() {

    private var cache = StrategyType.NO_CACHE
    private lateinit var service: DemoService
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.title.text = "Fragment1"

        service = createCache().create(DemoService::class.java)

        mBinding.retrofit.setOnClickListener {
            uiScope {
                LogUtil.e("cache $cache")
                val result = call().await()
                mBinding.message.text = result.toString()
            }
        }

        mBinding.flow.setOnClickListener {
            uiScope {
                LogUtil.e("cache $cache")
                call().asResultFlow().collect {
                    it.onSuccess { result ->
                        mBinding.message.text = result.toString()
                    }

                    it.onFailure { error ->
                        mBinding.message.text = error.toString()
                    }
                }
            }
        }

        val cacheType = arrayListOf<String>(
            "NO_CACHE", "FORCE_NETWORK", "FORCE_CACHE", "IF_CACHE_ELSE_NETWORK", "IF_NETWORK_ELSE_CACHE", "CACHE_AND_NETWORK"
        )

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cacheType)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinner.adapter = spinnerAdapter
        mBinding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = cacheType[position]
                cache = when (type) {
                    "NO_CACHE", "FORCE_NETWORK" -> 1
                    "FORCE_CACHE" -> 2
                    "IF_CACHE_ELSE_NETWORK" -> 3
                    "IF_NETWORK_ELSE_CACHE" -> 4
                    "CACHE_AND_NETWORK" -> 5
                    else -> -1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun call(): Call<Any> {
        return when (cache) {
            1 -> service.getUser(mBinding.editName.text.toString())
            2 -> service.getUserCache(mBinding.editName.text.toString())
            3 -> service.getUserCacheOrNet(mBinding.editName.text.toString())
            4 -> service.getUserNetOrCache(mBinding.editName.text.toString())
            5 -> service.getUserCacheAndNet(mBinding.editName.text.toString())
            else -> service.getUser(mBinding.editName.text.toString())
        }
    }


    private fun createCache(): RetrofitCache {
        val cacheHelper = CacheHelper(requireContext().cacheDir, Long.MAX_VALUE)
        return RetrofitCache.Builder().cache(cacheHelper)
            .addResponseConverterFactory(object : ResponseConverter.Factory() {
                override fun converterResponse(retrofit: RetrofitCache): ResponseConverter<Any>? {
                    //自定义修改结果并返回Response
                    return super.converterResponse(retrofit)
                }
            }).addHostInterceptor(object : DynamicHostInterceptor {
                override fun hostUrl(host: HOST): HttpUrl {
                    return when (host.hostType) {
                        1 -> "https://api.github.com/".toHttpUrl()
                        else -> super.hostUrl(host)
                    }
                }
            }).addInterceptor(LogInterceptor()).build()
    }
}
package com.mm.common

import com.tencent.mmkv.MMKV


/**
 * Created by : m
 * @since 1.0
 * 网络地址统一管理
 */

object UrlManager {
    private val defaultMMKV = MMKV.defaultMMKV()

    /**
     * 服务接口
     */
    const val DOMAIN_SERVER_URL = "https://api.github.com/"

    //网页host
    @JvmStatic
    var DOMAIN_URL: String = DOMAIN_SERVER_URL
        set(value) {
            field = if (!value.endsWith("/")) {
                "$value/"
            } else {
                value
            }
            defaultMMKV.putString(Constants.KEY.DOMAIN_URL, field)
        }
        get() = defaultMMKV.getString(Constants.KEY.DOMAIN_URL, DOMAIN_SERVER_URL) ?: DOMAIN_SERVER_URL
}
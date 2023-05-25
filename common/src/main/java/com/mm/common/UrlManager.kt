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
    //这里的接口不要轻易改动了。免得以后忘了修改回来。需要调试接口 在Host写死即可
    const val LIGHTNING_SERVER_URL = "https://testuat.apis.cloudcc.cn/"
    const val DOMAIN_SERVER_URL = "https://testuat.cloudcc.cn/"
    const val SITE_SERVER_URL = "https://site.cloudcc.com/"
    const val HAI_WAI_URL = "https://open-frn.cloudcc.com/"
    const val GLOBAL_SERVER_URL = "https://global.apis.cloudcc.cn/"

    //登录接口
    @JvmStatic
    var SITE_URL: String = SITE_SERVER_URL
        set(value) {
            field = if (!value.endsWith("/")) {
                "$value/"
            } else {
                value
            }
            defaultMMKV.putString(Constants.KEY.SERVER_URL, field)
        }
        get() = defaultMMKV.getString(Constants.KEY.SERVER_URL, SITE_SERVER_URL) ?: SITE_SERVER_URL

    //小程序Host
    @JvmStatic
    var GLOBAL_URL: String = GLOBAL_SERVER_URL
        set(value) {
            field = if (!value.endsWith("/")) {
                "$value/"
            } else {
                value
            }
            defaultMMKV.putString(Constants.KEY.GLOBAL_URL, field)
        }
        get() = defaultMMKV.getString(Constants.KEY.GLOBAL_URL, GLOBAL_SERVER_URL) ?: GLOBAL_SERVER_URL

    @JvmStatic
    var LIGHTNING_URL: String = SITE_SERVER_URL
        set(value) {
            field = if (!value.endsWith("/")) {
                "$value/"
            } else {
                value
            }
            defaultMMKV.putString(Constants.KEY.LIGHTNING_URL, field)
        }
        get() = defaultMMKV.getString(Constants.KEY.LIGHTNING_URL, SITE_SERVER_URL) ?: SITE_SERVER_URL

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
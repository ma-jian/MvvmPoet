package com.mm.lib_http

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe : 为指定Service配置Host，默认[Env.RELEASE]
 * @param debugUrl 测试环境host -> [Env.DEBUG]
 * @param preUrl 预发布环境host -> [Env.PRE_RELEASE]
 * @param releaseUrl 正式环境host -> [Env.RELEASE]
 * @param dynamicHost 是否支持动态下发host
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class HOST(
    val debugUrl: String = "",
    val preUrl: String = "",
    val releaseUrl: String,
    val dynamicHost: Boolean = false,
    val needSystemParam: Boolean = true,
    val signMethod: Int = -1,
) {
    companion object {
        const val SIGN_METHOD_1 = 1
        const val SIGN_METHOD_2 = 2
        const val SIGN_METHOD_3 = 3
    }
}

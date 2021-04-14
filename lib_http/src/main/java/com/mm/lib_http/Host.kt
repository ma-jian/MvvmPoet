package com.mm.lib_http

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class HOST(
    val debugUrl: String = "",
    val preUrl: String = "",
    val releaseUrl: String,
    val dynamicHostKey: String = "",
    val needSystemParam: Boolean = true,
    val signMethod: Int = -1,
) {
    companion object {
        const val SIGN_METHOD_1 = 1
        const val SIGN_METHOD_2 = 2
        const val SIGN_METHOD_3 = 3
    }
}

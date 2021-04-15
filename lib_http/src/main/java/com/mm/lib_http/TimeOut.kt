package com.mm.lib_http

import java.util.concurrent.TimeUnit

/**
 * Created by : majian
 * Date : 4/14/21
 * Describe :
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class TimeOut(
    val CONNECT_TIMEOUT: Int = 0,
    val READ_TIMEOUT: Int = 0,
    val WRITE_TIMEOUT: Int = 0,
    val unit: TimeUnit = TimeUnit.SECONDS
)

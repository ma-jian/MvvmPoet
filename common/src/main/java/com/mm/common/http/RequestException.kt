package com.mm.common.http

/**
 * Created by : m
 * @since 1.0
 * 数据请求错误 一般属于网络、或者解析异常等情况
 */

class RequestException : StateException {

    constructor(throwable: Throwable, code: String) : super(throwable, code)

    constructor(code: String, message: String?) : super(code, message)
}
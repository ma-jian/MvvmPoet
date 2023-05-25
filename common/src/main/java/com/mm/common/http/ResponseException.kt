package com.mm.common.http

/**
 * Created by : m
 * Date : 2022/3/4
 * 数据返回错误，返回数据不成功的状态
 */

class ResponseException : StateException {

    constructor(throwable: Throwable, code: String) : super(throwable, code)

    constructor(code: String, message: String?) : super(code, message)
}
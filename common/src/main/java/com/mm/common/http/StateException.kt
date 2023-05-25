package com.mm.common.http

/**
 * Created by : m
 * @since 1.0
 * 运行时异常错误
 */

open class StateException : RuntimeException {
    var code: String = "-1"
    final override var message: String = ""

    constructor(throwable: Throwable, code: String) : super(throwable) {
        this.code = code
        this.message = throwable.message ?: ""
    }

    constructor(code: String, message: String?) {
        this.code = code
        this.message = message ?: ""
    }
}
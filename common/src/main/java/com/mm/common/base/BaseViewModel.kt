package com.mm.common.base

import androidx.lifecycle.ViewModel
import com.mm.common.http.ExceptionHandle
import com.mm.common.http.StateException
import com.mm.common.utils.LogUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

/**
 * Created by : m
 * @since 1.0
 */

open class BaseViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = exceptionHandler + Dispatchers.Main

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(ExceptionHandle.handleException(exception))
    }

    private fun handleException(throwable: StateException) {
        LogUtil.e("${throwable.code} :${throwable.message}")
    }

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancelChildren()
    }
}
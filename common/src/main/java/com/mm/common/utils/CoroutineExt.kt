package com.mm.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Deferred
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by : m
 * @since 1.0
 */

internal const val DEFAULT_TIMEOUT = 5000L

fun <T> Deferred<T>.asLiveData(
    context: CoroutineContext = EmptyCoroutineContext, timeoutInMs: Long = DEFAULT_TIMEOUT
): LiveData<T> = liveData(context, timeoutInMs) {
    emit(this@asLiveData.await())
}
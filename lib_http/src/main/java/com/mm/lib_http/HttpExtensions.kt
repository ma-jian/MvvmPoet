@file:JvmName("HttpExtensions")

package com.mm.lib_http

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.*
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by : majian
 * Date : 4/24/21
 * Describe : 通过flow流对网络库扩展
 */

suspend fun <T> doBackground(block: suspend () -> T) = withContext(Dispatchers.IO) {
    block.invoke()
}

suspend fun <T : Any> Call<T>.asCallFlow(): Flow<T> = supervisorScope {
    flow {
        emit(this@asCallFlow)
    }.transform<Call<T>, T> { call ->
        emit(call.await())
    }.flowOn(Dispatchers.IO).catch { e ->
        currentCoroutineContext().apply {
            this[CoroutineExceptionHandler]?.let {
                it.handleException(this, e)
            } ?: run {
                //handleCoroutineExceptionImpl(this, e)
                LHttp.e(Log.getStackTraceString(e))
            }
        }
    }.flowOn(Dispatchers.Main)
}

suspend fun <T : Any> callFlow(block: suspend () -> Call<T>) = block.invoke().asCallFlow()

suspend fun <T : Any> T?.asEmitFlow() = supervisorScope {
    flow {
        emit(this@asEmitFlow)
    }.flowOn(Dispatchers.IO).catch { e ->
        currentCoroutineContext().apply {
            this[CoroutineExceptionHandler]?.let {
                it.handleException(this, e)
            } ?: run {
                //handleCoroutineExceptionImpl(this, e)
                LHttp.e(Log.getStackTraceString(e))
            }
        }
    }.flowOn(Dispatchers.Main)
}

suspend fun <T : Any> emitFlow(block: suspend () -> T) = block.invoke().asEmitFlow()

private val handlers: List<CoroutineExceptionHandler> = ServiceLoader
    .load(CoroutineExceptionHandler::class.java, CoroutineExceptionHandler::class.java.classLoader)
    .iterator().asSequence().toList()

private fun handleCoroutineExceptionImpl(context: CoroutineContext, exception: Throwable) {
    // use additional extension handlers
    for (handler in handlers) {
        try {
            handler.handleException(context, exception)
        } catch (t: Throwable) {
            // Use thread's handler if custom handler failed to handle exception
            val currentThread = Thread.currentThread()
            currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, handlerException(exception, t))
        }
    }

    // use thread's handler
    val currentThread = Thread.currentThread()
    currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, exception)
}

private fun handlerException(originalException: Throwable, thrownException: Throwable): Throwable {
    if (originalException === thrownException) return originalException
    return RuntimeException("Exception while trying to handle coroutine exception", thrownException).apply {
        addSuppressed(originalException)
    }
}
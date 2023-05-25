package com.mm.common.vm

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mm.common.http.ExceptionHandle
import com.mm.common.http.ResponseException
import com.mm.common.http.StateException
import com.mm.common.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

/**
 * Created by : m
 * @since 1.0
 */

//请求状态 仅保留 成功和失败状态
sealed class RequestState<out T> {

    data class Success<T>(val data: T?) : RequestState<T>()

    data class Error(val exception: StateException) : RequestState<Nothing>()

    companion object {
        @JvmStatic
        fun requestException(t: Throwable) = Error(ExceptionHandle.handleException(t))


        @JvmStatic
        fun responseException(code: String, message: String?) = Error(ResponseException(code, message))
    }
}

typealias StateMutableLiveData<T> = MutableLiveData<RequestState<T>>

typealias StateLiveData<T> = LiveData<RequestState<T>>

class ResultBuilder<T> {
    var data: () -> T? = { null }
    var onError: () -> ResponseException? = { null }
}

/**
 * 返回带有状态的 LiveData
 * @param stateLiveData 定义好的 [StateMutableLiveData]
 */
@MainThread
fun <T> CoroutineScope.flowStateLiveData(
    stateLiveData: StateMutableLiveData<T>? = null, init: suspend ResultBuilder<T>.() -> Unit
): StateLiveData<T> {
    return (stateLiveData ?: StateMutableLiveData<T>()).also { liveData ->
        val result = ResultBuilder<T>()
        CoroutineScope(coroutineContext + Dispatchers.Main).launch {
            flow {
                init.invoke(result)
                result.onError.invoke()?.let {
                    throw it
                } ?: emit(result.data.invoke())
            }.flowOn(Dispatchers.IO).catch { e ->
                if (e is ResponseException) {
                    liveData.value = RequestState.Error(e)
                } else {
                    //网络出错
                    LogUtil.e(
                        "OkHttpClient", if (e is UnknownHostException) {
                            e.message ?: e.toString()
                        } else Log.getStackTraceString(e)
                    )
                    val requestException = ExceptionHandle.handleException(e)
                    liveData.value = RequestState.Error(requestException)
                }
            }.flowOn(Dispatchers.Main).collect {
                liveData.value = RequestState.Success(it)
            }
        }
    }
}

@MainThread
fun <T> CoroutineScope.requestStateLiveData(
    stateLiveData: StateMutableLiveData<T>? = null, init: suspend ResultBuilder<T>.() -> Unit
): StateLiveData<T> {
    return (stateLiveData ?: StateMutableLiveData<T>()).also { liveData ->
        val result = ResultBuilder<T>()
        CoroutineScope(coroutineContext + Dispatchers.Main).launch {
            coroutineContext.job.invokeOnCompletion {
                it?.let { e ->
                    val requestException = ExceptionHandle.handleException(e)
                    liveData.value = RequestState.Error(requestException)
                }
            }
            init.invoke(result)
            result.onError.invoke()?.let {
                liveData.value = RequestState.Error(it)
            } ?: kotlin.run {
                liveData.value = RequestState.Success(result.data.invoke())
            }
        }
    }
}
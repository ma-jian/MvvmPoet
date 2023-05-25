package com.mm.common.http

import android.net.ParseException
import com.google.gson.JsonParseException
import com.mm.common.http.ExceptionHandle.ERROR.UNKNOWN
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Created by m
 * @since 1.0
 */
class ExceptionHandle {

    object ERROR {
        /**
         * 未知错误
         */
        internal const val UNKNOWN = -1000

        /**
         * 解析错误
         */
        internal const val PARSE_ERROR = -1001

        /**
         * 网络错误
         */
        internal const val NETWORD_ERROR = -1002

        /**
         * 协议出错
         */
        internal const val HTTP_ERROR = -1003

        /**
         * 证书出错
         */
        internal const val SSL_ERROR = -1005

        /**
         * 连接超时
         */
        internal const val TIMEOUT_ERROR = -1006
    }

    companion object {
        private const val UNAUTHORIZED = 401
        private const val FORBIDDEN = 403
        private const val NOT_FOUND = 404
        private const val REQUEST_TIMEOUT = 408
        private const val INTERNAL_SERVER_ERROR = 500
        private const val BAD_GATEWAY = 502
        private const val SERVICE_UNAVAILABLE = 503
        private const val GATEWAY_TIMEOUT = 504

        @JvmStatic
        fun handleException(e: Throwable): RequestException {
            val ex: RequestException
            if (e is HttpException) {
                ex = RequestException(e, "${e.code()}")
                when (e.code()) {
                    UNAUTHORIZED, FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> ex.message =
                        "网络不佳，请确定您的网络"
                    else -> ex.message = "网络不佳，请确定您的网络"
                }
                return ex
            } else if (e is JsonParseException || e is JSONException || e is ParseException) {
                ex = RequestException(e, ERROR.PARSE_ERROR.toString())
                ex.message = "解析错误"
                return ex
            } else if (e is ConnectException) {
                ex = RequestException(e, ERROR.NETWORD_ERROR.toString())
                ex.message = "连接失败"
                return ex
            } else if (e is javax.net.ssl.SSLHandshakeException) {
                ex = RequestException(e, ERROR.SSL_ERROR.toString())
                ex.message = "证书验证失败"
                return ex
            } else if (e is java.net.SocketTimeoutException) {
                ex = RequestException(e, ERROR.TIMEOUT_ERROR.toString())
                ex.message = "网络不佳，连接超时"
                return ex
            } else if (e is UnknownHostException) {
                ex = RequestException(e, NOT_FOUND.toString())
                ex.message = e.message ?: ""
                return ex
            } else if (e is IOException) {
                ex = RequestException(e, BAD_GATEWAY.toString())
                ex.message = "网络不佳，请确定您的网络"
                return ex
            }else {
                ex = RequestException(e, UNKNOWN.toString())
                ex.message = "系统忙，请稍后重试"
                return ex
            }
        }
    }
}


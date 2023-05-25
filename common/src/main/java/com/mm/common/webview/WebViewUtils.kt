package com.mm.common.webview


/**
 * Created by : m
 * Date : 2022/3/3
 * //处理url参数问题
 * public static String removeBlank(String url) {
 *
 *
 * return url;
 * }
 *
 *
 * //是否允许访问
 * public static boolean canHandleUrl(String url) {
 *
 *
 * return true;
 * }
 */
object WebViewUtils {
    //处理url参数问题
    @JvmStatic
    fun removeBlank(url: String): String {
        return url
    }

    //是否允许访问
    @JvmStatic
    fun canHandleUrl(url: String): Boolean {
        return url.startsWith("http://") ||
                url.startsWith("https://") ||
                url.startsWith("ftp://") ||
                url.startsWith("file://")
    }

    //是否系统启动访问
    @JvmStatic
    fun systemHandleUrl(url: String): Boolean {
        return url.startsWith("tel:") ||
                url.startsWith("sms:") ||
                url.startsWith("smsto:") ||
                url.startsWith("mailto:") ||
                url.startsWith("market:")
    }
}
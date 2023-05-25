package com.mm.common.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

/**
 * Created by : m
 * Date : 2022/3/3
 */

public interface IWebViewClient {
    boolean shouldOverrideUrlLoading(WebView view, String url);

    default boolean notifyThirdLoadUrl(String url) {
        return false;
    }

    default void onActivityNotFound() {
    }

    default boolean onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        return false;
    }

    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageReallyFinish(WebView view, String url);
}

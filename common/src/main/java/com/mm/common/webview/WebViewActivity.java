package com.mm.common.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.mm.common.base.BaseActivity;
import com.mm.common.databinding.ActivityWebviewBinding;

/**
 * Created by : m
 * Date : 2022/3/3
 */
public class WebViewActivity extends BaseActivity<ActivityWebviewBinding> {
    public static String WEB_TITLE = "web_title";
    public static String WEB_URL = "web_url";

    public static void startActivity(Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WEB_TITLE, title);
        intent.putExtra(WEB_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        mBinding.toolbar.setSupportActionBar(this);
        mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setDefaultWebSettings(mBinding.webView);
        String title = getIntent().getStringExtra(WEB_TITLE);
        String url = getIntent().getStringExtra(WEB_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
        }
        mBinding.toolbar.setCenterTitle(title);
        mBinding.webView.loadUrl(url);
        mBinding.progressBar.setColor("#407cff", "#007aff"); // 设置渐变色
        mBinding.progressBar.show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setDefaultWebSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        //支持获取手势焦点
        webView.requestFocusFromTouch();
        //支持JS
        webSettings.setJavaScriptEnabled(true);
        //支持插件
        webSettings.setPluginState(WebSettings.PluginState.ON);
        //设置适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //支持缩放
        webSettings.setSupportZoom(false);
        //隐藏原生的缩放控件
        webSettings.setDisplayZoomControls(false);
        //支持内容重新布局
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.supportMultipleWindows();
        webSettings.setSupportMultipleWindows(true);
        //设置缓存模式
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置可访问文件
        webSettings.setAllowFileAccess(true);
        //当webview调用requestFocus时为webview设置节点
        webSettings.setNeedInitialFocus(true);
        //支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setNeedInitialFocus(true);
        //设置编码格式
        webSettings.setDefaultTextEncodingName("UTF-8");

        webChromeClient();
        webViewClient();
    }

    private void webChromeClient() {
        mBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mBinding.progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title) && !title.startsWith("http")) {
                    mBinding.toolbar.setCenterTitle(title);
                }
            }
        });
    }

    private void webViewClient() {
        mBinding.webView.setWebViewClient(new IWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageReallyFinish(WebView view, String url) {
                mBinding.progressBar.hide();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.webView.onResume();
        mBinding.webView.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.webView.onPause();
        mBinding.webView.pauseTimers();
    }

    @Override
    public void onBackPressed() {
        if (mBinding.webView.canGoBack()) {
            mBinding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.getRoot().removeView(mBinding.webView);
        mBinding.webView.stopLoading();
        mBinding.webView.clearMatches();
        mBinding.webView.clearHistory();
        mBinding.webView.clearSslPreferences();
        mBinding.webView.clearCache(true);
        mBinding.webView.loadUrl("about:blank");
        mBinding.webView.removeAllViews();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBinding.webView.removeJavascriptInterface("AndroidNative");
        }
        mBinding.webView.destroy();
    }
}
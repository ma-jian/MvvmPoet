package com.mm.common.webview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

/**
 * Created by : m
 * Date : 2022/3/3
 */

public class CWebView extends BaseWebView implements DownloadListener {
    private IWebViewClient mIWebViewClient;
    private boolean mIsBlankPageRedirect;  //是否因重定向导致的空白页面。

    public CWebView(Context context) {
        super(context);
        init();
    }

    public CWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setWebViewClient(IWebViewClient iWebViewClient) {
        this.mIWebViewClient = iWebViewClient;
    }

    private void init() {
        setWebViewClient(mWebViewClient);
    }

    protected void back() {
        //		if (canGoBack()) {
        //			goBack();
        //		}
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mIsBlankPageRedirect = true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private final WebViewClient mWebViewClient = new WebViewClient() {
        private boolean handleByCaller = false;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (null != mIWebViewClient) {
                mIWebViewClient.onPageStarted(view, url, favicon);
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            url = WebViewUtils.removeBlank(url);
            //允许启动第三方应用客户端
            if (WebViewUtils.canHandleUrl(url)) {
                // 如果不是用户触发的操作，就没有必要交给上层处理了，直接走url拦截规则。
                if (null != mIWebViewClient && isTouchByUser()) {
                    handleByCaller = mIWebViewClient.shouldOverrideUrlLoading(view, url);
                }
                if (!handleByCaller) {
                    handleByCaller = handleOverrideUrl(url);
                }
                return handleByCaller || super.shouldOverrideUrlLoading(view, url);
            } else if (WebViewUtils.systemHandleUrl(url)) {
                if (null != mIWebViewClient) {
                    handleByCaller = mIWebViewClient.shouldOverrideUrlLoading(view, url);
                }
                if (!handleByCaller) {
                    handleByCaller = WebActivityRouter.systemLink(getContext(), url);
                }
                return handleByCaller || super.shouldOverrideUrlLoading(view, url);
            } else {
                try {
                    if (notifyOpenLoading(url)) {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        // 如果手机还没安装app，则跳转到应用市场
                        if (getContext().getPackageManager().resolveActivity(intent, 0) == null) {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage()));
                        }
                        getContext().startActivity(intent);
                        if (!mIsBlankPageRedirect) {
                            // 如果遇到白屏问题，手动后退
                            back();
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return handleByCaller || super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        private boolean handleOverrideUrl(final String url) {
            if (!TextUtils.isEmpty(url)) {
                //处理系统协议或cloudcc协议调起本地页面
                WebActivityRouter.IntentBuilder intentBuilder = new WebActivityRouter.IntentBuilder(url);
                return WebActivityRouter.startFromWeb(intentBuilder);
            } else {
                return false;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mIsBlankPageRedirect = true;
            if (null != mIWebViewClient) {
                mIWebViewClient.onPageReallyFinish(view, url);
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (null != mIWebViewClient) {
                //外部已经处理掉错误 不需要在加载默认错误页面
                if (!mIWebViewClient.onReceivedError(view, request, error)) {
                    String language = Locale.getDefault().getLanguage();
                    if ("zh".equals(language)) {
                        view.loadUrl("file:///android_asset/html/error.html");
                    } else {
                        view.loadUrl("file:///android_asset/html/en_error.html");
                    }
                }
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            if (null != mIWebViewClient) {
                mIWebViewClient.onReceivedSslError(view, handler, error);
            }
        }
    };

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        downloadByBrowser(url);
    }

    @Override
    public void setOverScrollMode(int mode) {
        try {
            super.setOverScrollMode(mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过浏览器下载
     *
     * @param url
     */
    private void downloadByBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //是否允许打开第三方应用
    private boolean notifyOpenLoading(String url) {
        if (mIWebViewClient != null) {
            return mIWebViewClient.notifyThirdLoadUrl(url);
        }
        return false;
    }
}

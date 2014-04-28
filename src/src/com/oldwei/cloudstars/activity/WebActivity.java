package com.oldwei.cloudstars.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oldwei.cloudstars.R;
import com.oldwei.cloudstarts.vo.LinkVo;

public class WebActivity extends Activity {

    public static String WEB_LOADED_LINKVO = "WebActivity.linkVo";
    private int mPercetageMax = 100;
    private WebView mWebView;
    private LinkVo mLinkVo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.web_activity);
        mLinkVo = getIntent().getParcelableExtra(WEB_LOADED_LINKVO);
        initView();
        initListener();
        loadView();
    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.web_view);
    }

    private void initListener() {
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                WebActivity.this.setTitle(mLinkVo.getTitle());
                if (newProgress == mPercetageMax) {
                    // finish the loading
                } else {
                    WebActivity.this.setProgress(newProgress);

                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                // Handle the error
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        // mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.loadUrl(mLinkVo.getUrl());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        finish();
        return false;
    }
}

package com.oldwei.cloudstars.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.oldwei.cloudstars.R;

public class WebActivity extends BaseActivty {

    public static String WEB_LOADED_URL = "WebActivity.url";
    private WebView mWebView;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        mUrl = getIntent().getStringExtra(WEB_LOADED_URL);
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
                if (newProgress == 100) {
                    WebActivity.this.setTitle("加载完成");
                } else {
                    WebActivity.this.setTitle("加载中.......");

                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(0);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        mWebView.loadUrl(mUrl);
    }

}

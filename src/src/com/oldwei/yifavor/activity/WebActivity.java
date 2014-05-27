package com.oldwei.yifavor.activity;

import java.io.File;
import java.sql.SQLException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.model.LinkModel;
import com.oldwei.yifavor.service.LinkService;
import com.oldwei.yifavor.utils.NetUtils;

public class WebActivity extends Activity {

    public static String WEB_LOADED_LINK = "WebActivity.LinkModel";
    private static String CACHE_FILE_NAME = "web_cache";
    private int mPercetageMax = 100;
    private WebView mWebView;
    private LinkModel mLinkModel;
    private Button mOpenAnotherBrowser;
    private Button mAddNewLink;
    private LinkModel mCurrentLinkModel = new LinkModel();

    // TODO: Add a stack to save the link scanning records.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        mLinkModel = getIntent().getParcelableExtra(WEB_LOADED_LINK);
        mCurrentLinkModel.setUrl(mLinkModel.getUrl());
        initView();
        initListener();
        setH5Cache();
        setCacheWay();
        loadView();
    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.web_view);
        mOpenAnotherBrowser = (Button) findViewById(R.id.web_page_open_another);
        mOpenAnotherBrowser.setOnClickListener(mOnClickListener);
        mAddNewLink = (Button) findViewById(R.id.web_page_add_new);
        mAddNewLink.setOnClickListener(mOnClickListener);
    }

    private void setCacheWay() {
        if (NetUtils.isNetAlive()) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

    }

    private void setH5Cache() {
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setAppCachePath(this.getFilesDir().getAbsolutePath() + File.separator + CACHE_FILE_NAME);
        mWebView.getSettings().setAppCacheMaxSize(Long.MAX_VALUE);
    }

    private void initListener() {
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == mPercetageMax) {
                    // finish the loading
                } else {

                }
                WebActivity.this.setProgress(newProgress);
            }

            @Override
            public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
                // FIXME Auto-generated method stub
                super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                mCurrentLinkModel.setTitle(title);
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // FIXME Auto-generated method stub
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                mCurrentLinkModel.setIcon(url);
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Handle the error
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url) != null && !url.startsWith("about:")) {
                    view.loadUrl(url);
                    mCurrentLinkModel.setUrl(url);
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.loadUrl(mLinkModel.getUrl());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
            case R.id.web_page_open_another:
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(mCurrentLinkModel.getUrl());
                intent.setData(content_url);
                startActivity(intent);
                break;
            case R.id.web_page_add_new:
                try {
                    new LinkService().add(mCurrentLinkModel);
                    Toast.makeText(WebActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Toast.makeText(WebActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
            }
        }
    };
}

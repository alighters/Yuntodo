package com.oldwei.yifavor.activity;

import android.content.Intent;
import android.os.Bundle;

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.YiFavorApplication;
import com.oldwei.yifavor.helper.LoadDataHelper;

public class LoadingActivity extends BaseActivty {

    public static boolean DEBUG_MODE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);
        initView();
        loadData();
        forwardView();
    }

    private void initView() {

    }

    private void loadData() {
        if (YiFavorApplication.isFirstVisited()) {
            LoadDataHelper.saveCategoriesData();
            LoadDataHelper.saveLinksData();
        }
    }

    private void forwardView() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}

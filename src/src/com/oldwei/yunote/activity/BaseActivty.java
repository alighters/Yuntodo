package com.oldwei.yunote.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class BaseActivty extends ActionBarActivity  {
    private ActionBar actionBar;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();  
        actionBar.setDisplayShowHomeEnabled(true);  
        
    }

}

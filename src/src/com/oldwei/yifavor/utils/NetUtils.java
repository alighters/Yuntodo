package com.oldwei.yifavor.utils;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.oldwei.yifavor.YiFavorApplication;

public class NetUtils {

    public static boolean isNetAlive() {
        WifiManager wm = (WifiManager) YiFavorApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        return wm.isWifiEnabled() ? true : false;
    }
}

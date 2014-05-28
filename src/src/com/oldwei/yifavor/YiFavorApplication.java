package com.oldwei.yifavor;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class YiFavorApplication extends Application {
    private static Context mContext;
    public static final String Prefs = "YiFavorPrefs";
    private static String IS_FIRST_VISITED = "is_first_visited";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        setImageLoaderSettings();

    }

    public static Context getContext() {
        return mContext;
    }

    private void setImageLoaderSettings() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).cacheInMemory(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2)
                .defaultDisplayImageOptions(options).denyCacheImageMultipleSizesInMemory()
                // Not necessary in common
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    public static boolean isFirstVisited() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Prefs, Context.MODE_PRIVATE);
        boolean isFirst = sharedPref.getBoolean(IS_FIRST_VISITED, true);
        if (isFirst) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(IS_FIRST_VISITED, false);
            editor.commit();
        }
        return isFirst;
    }
}

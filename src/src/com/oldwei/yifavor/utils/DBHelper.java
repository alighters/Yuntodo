package com.oldwei.yifavor.utils;

import java.sql.SQLException;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.model.LinkModel;

public class DBHelper {
    private String TAG = DataHelper.class.getName();
    private static DBHelper instance;
    private DataHelper dataHelper;

    private DBHelper() {
        dataHelper = new DataHelper();
    }

    public static DBHelper getInstance() {
        if (instance == null) {
            instance = new DBHelper();
        }
        return instance;
    }

    public Dao<CategoryModel, Integer> getCatogoryDao() {
        Dao<CategoryModel, Integer> dao = null;
        try {
            dao = dataHelper.getCateogoryDao();
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return dao;
    }

    public Dao<LinkModel, Integer> getLinkDao() {
        Dao<LinkModel, Integer> dao = null;
        try {
            dao = dataHelper.getLinkDao();
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return dao;
    }
}

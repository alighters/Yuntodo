package com.oldwei.yifavor.utils;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.oldwei.yifavor.YiFavorApplication;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.model.LinkModel;

public class DataHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "YiFavorOrmLite.db";
    private static final int DATABASE_VERSION = 1;
    private Dao<CategoryModel, Integer> categoryDao;
    private Dao<LinkModel, Integer> linkDao;

    public DataHelper() {
        super(YiFavorApplication.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource arg1) {
        try {
            TableUtils.createTable(connectionSource, CategoryModel.class);
            TableUtils.createTable(connectionSource, LinkModel.class);
        } catch (SQLException e) {
            Log.e(DataHelper.class.getName(), "创建数据库失败", e);
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource arg1, int arg2, int arg3) {
        try {
            TableUtils.dropTable(connectionSource, CategoryModel.class, true);
            TableUtils.dropTable(connectionSource, LinkModel.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DataHelper.class.getName(), "更新数据库失败", e);
            e.printStackTrace();
        }

    }

    @Override
    public void close() {
        super.close();
        categoryDao = null;
        linkDao = null;
    }

    public Dao<CategoryModel, Integer> getCateogoryDao() throws SQLException {
        if (categoryDao == null) {
            categoryDao = getDao(CategoryModel.class);
        }
        return categoryDao;
    }

    public Dao<LinkModel, Integer> getLinkDao() throws SQLException {
        if (linkDao == null) {
            linkDao = getDao(LinkModel.class);
        }
        return linkDao;
    }
}

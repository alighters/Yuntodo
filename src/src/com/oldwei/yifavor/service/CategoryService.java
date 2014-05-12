package com.oldwei.yifavor.service;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.utils.DBHelper;

public class CategoryService {
    private Dao<CategoryModel, Integer> categoryDao;

    public CategoryService() {
        categoryDao = DBHelper.getInstance().getCatogoryDao();
    }

    public List<CategoryModel> queryAll() throws SQLException {
        List<CategoryModel> list = null;
        QueryBuilder<CategoryModel, Integer> qb = categoryDao.queryBuilder();
        qb.orderBy("orderId", true);
        list = qb.query();
        return list;
    }

    public void add(CategoryModel model) throws SQLException {
        // 获取Model的最大orderId;
        QueryBuilder<CategoryModel, Integer> qb = categoryDao.queryBuilder();
        qb.selectRaw("MAX(orderId)");
        GenericRawResults<String[]> result;
        result = categoryDao.queryRaw(qb.prepareStatementString());
        String[] values = result.getFirstResult();
        if (values[0] != null)
            model.setOrderId(Integer.valueOf(values[0]) + 1);
        categoryDao.createOrUpdate(model);
    }
    
    public void update(CategoryModel model) throws SQLException{
        categoryDao.update(model);
    }
}

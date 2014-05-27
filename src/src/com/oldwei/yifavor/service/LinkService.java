package com.oldwei.yifavor.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.model.LinkModel;
import com.oldwei.yifavor.utils.DBHelper;

public class LinkService {
    private Dao<LinkModel, Integer> linkDao;

    public LinkService() {
        linkDao = DBHelper.getInstance().getLinkDao();
    }

    public List<LinkModel> queryAll() throws SQLException {
        List<LinkModel> list = null;
        list = linkDao.queryForAll();
        return list;
    }

    public void add(LinkModel model) throws SQLException {
        linkDao.createOrUpdate(model);
    }

    public void update(LinkModel model) throws SQLException {
        linkDao.createOrUpdate(model);
    }

    public List<LinkModel> getLinksByCategory(CategoryModel model) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("categoryId", model.getId());

        return linkDao.queryForFieldValues(map);
    }

    public int delete(Integer id) throws SQLException {
        return linkDao.deleteById(id);
    }
}

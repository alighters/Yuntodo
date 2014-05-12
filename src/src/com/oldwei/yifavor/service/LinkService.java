package com.oldwei.yifavor.service;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
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

    /**
     * Add a new link.
     * 
     * @param model
     * @return A Integer value, if the value is big than 0, means adding the
     *         model successfully, else failing to add the model.
     * @throws SQLException
     */
    public int add(LinkModel model) throws SQLException {
        int result = linkDao.create(model);
        return result;
    }

    public void update(LinkModel model) throws SQLException {
        linkDao.createOrUpdate(model);
    }
}

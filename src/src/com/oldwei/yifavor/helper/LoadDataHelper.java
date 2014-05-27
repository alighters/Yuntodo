package com.oldwei.yifavor.helper;

import java.sql.SQLException;

import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.model.LinkModel;
import com.oldwei.yifavor.service.CategoryService;
import com.oldwei.yifavor.service.LinkService;
import com.oldwei.yifavor.utils.JSONUtils;

public class LoadDataHelper {

	public static void saveLinksData() {
		LinkService service = new LinkService();
		for (LinkModel model : JSONUtils.loadLinkList()) {
			try {
				service.update(model);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveCategoriesData() {
		CategoryService service = new CategoryService();
		for (CategoryModel model : JSONUtils.loadCategoryList()) {
			try {
				service.update(model);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

package com.oldwei.yifavor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oldwei.yifavor.YiFavorApplication;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.model.LinkModel;

/**
 * Load the data about the model from json files.
 * 
 * @author David.Wei
 * 
 */
public class JSONUtils {

    public static List<LinkModel> loadLinkList() {
        List<LinkModel> LinkModelList = new ArrayList<LinkModel>();
        InputStreamReader inputReader = null;
        BufferedReader reader = null;
        InputStream inputStream = null;
        try {
            inputStream = YiFavorApplication.getContext().getAssets().open("linklist.json");
            inputReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            JSONObject json = new JSONObject(new String(result.getBytes(), "utf-8"));
            JSONArray jsonArray = json.getJSONArray("links");
            JSONObject jsonObject;
            LinkModel vo;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                vo = new LinkModel();
                vo.setIcon(jsonObject.getString("icon"));
                vo.setUrl(jsonObject.getString("url"));
                vo.setTitle(jsonObject.getString("title"));
                vo.setCategoryId(jsonObject.getInt("categoryId"));
                LinkModelList.add(vo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                inputReader.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return LinkModelList;
    }

    public static List<CategoryModel> loadCategoryList() {
        List<CategoryModel> LinkModelList = new ArrayList<CategoryModel>();
        InputStreamReader inputReader = null;
        BufferedReader reader = null;
        InputStream inputStream = null;
        try {
            inputStream = YiFavorApplication.getContext().getAssets().open("linkcategory.json");
            inputReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            JSONObject json = new JSONObject(new String(result.getBytes(), "utf-8"));
            JSONArray jsonArray = json.getJSONArray("categories");
            JSONObject jsonObject;
            CategoryModel vo;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                vo = new CategoryModel();
                vo.setId(jsonObject.getInt("id"));
                vo.setName(jsonObject.getString("name"));
                LinkModelList.add(vo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                inputReader.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return LinkModelList;
    }

}

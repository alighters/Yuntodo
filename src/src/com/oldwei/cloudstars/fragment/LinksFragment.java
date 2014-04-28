package com.oldwei.cloudstars.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.oldwei.cloudstars.R;
import com.oldwei.cloudstars.activity.WebActivity;
import com.oldwei.cloudstars.adapter.LinkListAdapter;
import com.oldwei.cloudstarts.vo.LinkVo;

public class LinksFragment extends Fragment {

    private ListView mListView;
    private View mView;
    private BaseAdapter mListAdapter;
    private List<LinkVo> mLinkVoList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.home_link_fragment, null);
        initView();
        initData();
        return mView;
    }

    private void initView() {
        mListView = (ListView) mView.findViewById(R.id.home_link_listview);
    }

    private void initData() {
        setData();
        mListAdapter = new LinkListAdapter(getActivity(), mLinkVoList);
        mListView.setAdapter(mListAdapter);
         mListView.setOnItemClickListener(mListViewItemClickListener);
    }

    public void refresh() {
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * Get the data from json file in the asset folder.
     */
    private void setData() {
        mLinkVoList = new ArrayList<LinkVo>();
        InputStreamReader inputReader = null;
        BufferedReader reader = null;
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getAssets().open("linklist.json");
            inputReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            JSONObject json = new JSONObject(new String(result.getBytes(),
                    "utf-8"));
            JSONArray jsonArray = json.getJSONArray("links");
            JSONObject jsonObject;
            LinkVo vo;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                vo = new LinkVo();
                vo.setIcon(jsonObject.getString("icon"));
                vo.setUrl(jsonObject.getString("url"));
                vo.setTitle(jsonObject.getString("title"));
                mLinkVoList.add(vo);
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
    }

    OnItemClickListener mListViewItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Intent it = new Intent(getActivity(), WebActivity.class);
            it.putExtra(WebActivity.WEB_LOADED_LINKVO,
                    mLinkVoList.get(position));
            startActivity(it);
        }
    };

}

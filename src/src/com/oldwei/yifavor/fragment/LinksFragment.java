package com.oldwei.yifavor.fragment;

import java.util.ArrayList;
import java.util.List;

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

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.activity.WebActivity;
import com.oldwei.yifavor.adapter.LinkListAdapter;
import com.oldwei.yifavor.model.LinkModel;

public class LinksFragment extends Fragment {

    private ListView mListView;
    private View mView;
    private BaseAdapter mListAdapter;
    private List<LinkModel> mLinkModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.home_link_fragment, null);
        initView();
        initData();
        return mView;
    }

    private void initView() {
        mListView = (ListView) mView.findViewById(R.id.home_link_listview);
    }

    private void initData() {
        mLinkModelList = new ArrayList<LinkModel>();
        mListAdapter = new LinkListAdapter(getActivity(), mLinkModelList);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(mListViewItemClickListener);
    }

    /**
     * According to the data to update the view.
     * 
     * @param LinkModelList
     */
    public void refreshData(List<LinkModel> LinkModelList) {
        mLinkModelList.clear();
        mLinkModelList.addAll(LinkModelList);
        mListAdapter.notifyDataSetChanged();
    }

    OnItemClickListener mListViewItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent it = new Intent(getActivity(), WebActivity.class);
            it.putExtra(WebActivity.WEB_LOADED_LINK, mLinkModelList.get(position));
            startActivity(it);
        }
    };

}

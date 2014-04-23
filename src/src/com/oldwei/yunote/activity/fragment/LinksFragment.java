package com.oldwei.yunote.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.oldwei.yunote.R;
import com.oldwei.yunote.activity.adapter.LinkListAdapter;
import com.oldwei.yunote.vo.LinkVo;

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
    }

    public void refresh() {
        mListAdapter.notifyDataSetChanged();
    }

    private void setData() {
        mLinkVoList = new ArrayList<LinkVo>();
        LinkVo vo1 = new LinkVo();
        vo1.setTitle("���̺��̵߳�����");
        LinkVo vo2 = new LinkVo();
        vo2.setTitle("Android����ǳ��");
        LinkVo vo3 = new LinkVo();
        vo3.setTitle("΢�Ź���ƽ̨");
        LinkVo vo4 = new LinkVo();
        vo4.setTitle("���̺��̵߳�����");
        LinkVo vo5 = new LinkVo();
        vo5.setTitle("Ϊ֪�ʼ�");
        LinkVo vo6 = new LinkVo();
        vo6.setTitle("Activity������");
        mLinkVoList.add(vo1);
        mLinkVoList.add(vo2);
        mLinkVoList.add(vo3);
        mLinkVoList.add(vo4);
        mLinkVoList.add(vo5);
        mLinkVoList.add(vo6);
    }
}

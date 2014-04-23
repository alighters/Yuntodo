package com.oldwei.yunote.activity.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oldwei.yunote.R;
import com.oldwei.yunote.vo.LinkVo;

public class LinkListAdapter extends BaseAdapter {

    private List<LinkVo> mLinkVoList;
    private Context mContext;

    public LinkListAdapter(Context context, List<LinkVo> linkVoList) {
        this.mContext = context;
        this.mLinkVoList = linkVoList;
    }

    @Override
    public int getCount() {
        if (mLinkVoList != null)
            return mLinkVoList.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mLinkVoList != null)
            return mLinkVoList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.home_link_fragment_item, null);
            TextView titleText = (TextView) convertView
                    .findViewById(R.id.link_text);

            holder = new ViewHolder();
            holder.titleText = titleText;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.titleText.setText(mLinkVoList.get(position).getTitle());
        return convertView;
    }

    class ViewHolder {
        TextView titleText;

    }

}

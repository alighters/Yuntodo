package com.oldwei.yifavor.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oldwei.yifavor.R;
import com.oldwei.yifavor.model.LinkModel;

public class LinkListAdapter extends BaseAdapter {
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.url_default)
            .showImageForEmptyUri(R.drawable.url_default) // resource or
            .showImageOnFail(R.drawable.url_default) // resource or drawable
            .cacheInMemory(true) // default
            .cacheOnDisc(true) // default
            .bitmapConfig(Bitmap.Config.RGB_565) // default
            .build();

    private List<LinkModel> mLinkModelList;
    private Context mContext;

    public LinkListAdapter(Context context, List<LinkModel> LinkModelList) {
        this.mContext = context;
        this.mLinkModelList = LinkModelList;
    }

    @Override
    public int getCount() {
        if (mLinkModelList != null)
            return mLinkModelList.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mLinkModelList != null)
            return mLinkModelList.get(position);
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
            ImageView iconView = (ImageView) convertView
                    .findViewById(R.id.link_icon);
            holder = new ViewHolder();
            holder.titleText = titleText;
            holder.iconView = iconView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.titleText.setText(mLinkModelList.get(position).getTitle());
        ImageLoader.getInstance().displayImage(
                mLinkModelList.get(position).getIcon(), holder.iconView, options);
        return convertView;
    }

    class ViewHolder {
        TextView titleText;
        ImageView iconView;
    }

}

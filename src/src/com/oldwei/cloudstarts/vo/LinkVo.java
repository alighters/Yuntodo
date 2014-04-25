package com.oldwei.cloudstarts.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The link model from the cloud, including the title and link.
 * 
 * @author David.Wei
 * 
 */
public class LinkVo implements Parcelable {

    private String icon;
    private String title;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeString(title);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<LinkVo> CREATOR = new Creator<LinkVo>() {

        @Override
        public LinkVo[] newArray(int size) {
            return new LinkVo[size];
        }

        @Override
        public LinkVo createFromParcel(Parcel source) {
            LinkVo vo = new LinkVo();
            vo.setIcon(source.readString());
            vo.setTitle(source.readString());
            vo.setUrl(source.readString());
            return vo;
        }
    };

}

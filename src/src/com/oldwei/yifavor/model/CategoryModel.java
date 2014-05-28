package com.oldwei.yifavor.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

public class CategoryModel implements Parcelable {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField
    private int count;
    @DatabaseField(unique = true)
    private int orderId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getOrderId() {
        return this.orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(count);
        dest.writeInt(orderId);
    }

    public static final Parcelable.Creator<CategoryModel> CREATOR = new Creator<CategoryModel>() {

        @Override
        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }

        @Override
        public CategoryModel createFromParcel(Parcel source) {
            CategoryModel vo = new CategoryModel();
            vo.setId(source.readInt());
            vo.setName(source.readString());
            vo.setCount(source.readInt());
            vo.setOrderId(source.readInt());
            return vo;
        }
    };
}

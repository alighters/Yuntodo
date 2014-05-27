package com.oldwei.yifavor.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

public class LinkModel implements Parcelable {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private String icon;

	@DatabaseField(canBeNull = false, index = true)
	private String title;
	@DatabaseField(canBeNull = false)
	private String url;
	@DatabaseField
	private int categoryId;
	@DatabaseField(unique = true)
	private int orderId;
	@DatabaseField
	private String time;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getOrderId() {
		return this.orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(icon);
		dest.writeString(title);
		dest.writeString(url);
		dest.writeInt(categoryId);
		dest.writeInt(orderId);
		dest.writeString(time);

	}

	public static final Parcelable.Creator<LinkModel> CREATOR = new Creator<LinkModel>() {

		@Override
		public LinkModel[] newArray(int size) {
			return new LinkModel[size];
		}

		@Override
		public LinkModel createFromParcel(Parcel source) {
			LinkModel model = new LinkModel();
			model.setId(source.readInt());
			model.setIcon(source.readString());
			model.setTitle(source.readString());
			model.setUrl(source.readString());
			model.setCategoryId(source.readInt());
			model.setOrderId(source.readInt());
			model.setTime(source.readString());
			return model;
		}
	};
}

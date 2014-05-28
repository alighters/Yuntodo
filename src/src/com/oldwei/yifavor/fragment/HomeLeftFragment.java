package com.oldwei.yifavor.fragment;

import java.sql.SQLException;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.service.CategoryService;

public class HomeLeftFragment extends Fragment {

    private View mView;
    private ListView mListView;
    private Button mCategoryAddBtn;
    private RelativeLayout mCategoryAddLayout;
    private Button mCategoryAddCancelBtn;
    private Button mCategoryAddOkBtn;
    private List<CategoryModel> mCategoryList;
    private CategoryListAdapter mCategoryListAdapter;
    private CategoryItemClickListener mCategoryItemClickListener;
    private EditText mAddCategoryText;

    private CategoryService mCategoryService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.home_left_fragment, null);
        mCategoryService = new CategoryService();
        initView();
        return mView;
    }

    private void initView() {
        loadData();
        mListView = (ListView) mView.findViewById(R.id.home_link_category_listview);
        mAddCategoryText = (EditText) mView.findViewById(R.id.home_left_add_category_text);
        mCategoryAddLayout = (RelativeLayout) mView.findViewById(R.id.home_left_category_add_layout);
        mCategoryAddBtn = (Button) mView.findViewById(R.id.home_link_category_add_btn);
        mCategoryAddBtn.setOnClickListener(mOnClickListener);
        mCategoryAddCancelBtn = (Button) mView.findViewById(R.id.home_link_category_add_cancel_btn);
        mCategoryAddCancelBtn.setOnClickListener(mOnClickListener);
        mCategoryAddOkBtn = (Button) mView.findViewById(R.id.home_link_category_add_ok_btn);
        mCategoryAddOkBtn.setOnClickListener(mOnClickListener);
        mCategoryListAdapter = new CategoryListAdapter();
        mListView.setAdapter(mCategoryListAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public void loadData() {
        try {
            mCategoryList = mCategoryService.queryAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCategoryItemClickListener.getCurCategory(mCategoryList.get(position));
        }

    };

    private OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.home_link_category_add_btn:
                mAddCategoryText.setVisibility(View.VISIBLE);
                mCategoryAddLayout.setVisibility(View.VISIBLE);
                mCategoryAddBtn.setVisibility(View.GONE);
                break;
            case R.id.home_link_category_add_cancel_btn:
                mAddCategoryText.setText("");
                mAddCategoryText.setVisibility(View.GONE);
                mCategoryAddLayout.setVisibility(View.GONE);
                mCategoryAddBtn.setVisibility(View.VISIBLE);
                break;
            case R.id.home_link_category_add_ok_btn:
                CategoryModel model = new CategoryModel();
                model.setName(mAddCategoryText.getText().toString().trim());
                try {
                    new CategoryService().add(model);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    loadData();
                    mCategoryListAdapter.notifyDataSetChanged();
                }
                mAddCategoryText.setText("");
                mAddCategoryText.setVisibility(View.GONE);
                mCategoryAddLayout.setVisibility(View.GONE);
                mCategoryAddBtn.setVisibility(View.VISIBLE);
                break;

            default:
                break;
            }

        }
    };

    class CategoryListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mCategoryList != null) {
                return mCategoryList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mCategoryList != null) {
                return mCategoryList.get(position);
            }
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.home_left_fragment_item, null);
                holder = new ViewHolder();
                holder.nameText = (TextView) convertView.findViewById(R.id.home_left_item_category_name);
                holder.countText = (TextView) convertView.findViewById(R.id.home_left_item_category_count);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.nameText.setText(mCategoryList.get(position).getName());
            holder.countText.setText(String.valueOf(mCategoryList.get(position).getCount()));
            return convertView;
        }

        class ViewHolder {
            TextView nameText;
            TextView countText;
        }
    }

    public void setCategoryItemClickListener(CategoryItemClickListener categoryItemClickListener) {
        this.mCategoryItemClickListener = categoryItemClickListener;
    }

    public interface CategoryItemClickListener {
        public void getCurCategory(CategoryModel model);
    }
}

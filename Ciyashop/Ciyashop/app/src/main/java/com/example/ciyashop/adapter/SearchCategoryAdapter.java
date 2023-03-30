package com.example.ciyashop.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.activity.SearchCategoryInnerListActivity;
import com.example.ciyashop.activity.SearchCategoryListActivity;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SearchCategoryAdapter extends RecyclerView.Adapter<SearchCategoryAdapter.CategoryViewHolder> implements OnItemClickListener {

    private final int REQUEST_CODE = 101;
    SearchInnerCategoryAdapter searchInnerCategoryAdapter;
    private List<Home.AllCategory> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private String from = "";
    LinearLayout llMain;

    public SearchCategoryAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.AllCategory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setFrom(String from) {
        if (from != null) {
            this.from = from;
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_catgory, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        llMain = holder.llMain;
        if (from.equals(RequestParamUtils.filter)) {
            holder.llMain.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).id + "");
                intent.putExtra(RequestParamUtils.SEARCH, SearchCategoryListActivity.search);
                intent.putExtra(RequestParamUtils.ORDER_BY, SearchCategoryListActivity.sortBy);
                intent.putExtra(RequestParamUtils.POSITION, SearchCategoryListActivity.sortPosition);

                ActivityOptions options = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Pair<View, String> pair = new Pair<>(llMain, "imagetransition");
                    options = ActivityOptions.makeSceneTransitionAnimation(activity, pair);
                }

                //activity.setResult(RESULT_OK, intent);
                if (options != null) {
                    activity.startActivity(intent, options.toBundle());
                }
                activity.finish();
            });

        } else {
            holder.llMain.setOnClickListener(view -> {
                Intent intent = new Intent(activity, SearchCategoryInnerListActivity.class);
                intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).id);
                activity.startActivity(intent);
//                    ActivityOptions options = null;
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                        Pair pair = new Pair(llmain, "imagetransition");
//                        options = ActivityOptions.makeSceneTransitionAnimation(activity, pair);
//                    }
//                    activity.startActivity(intent,options.toBundle());
            });

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).name, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).name));
        }

        if (!list.get(position).image.src.equals("")) {
            Glide.with(activity.getBaseContext()).load(list.get(position).image.src + "").into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.blackround);
        }
        holder.ivImage.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        TextView tvName;
        ImageView ivImage;

        public CategoryViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            tvName = view.findViewById(R.id.tvName);
            ivImage = view.findViewById(R.id.ivImage);
        }
    }
}
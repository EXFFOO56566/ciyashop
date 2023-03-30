package com.example.ciyashop.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ciyashop.R;
import com.example.ciyashop.adapter.SearchInnerCategoryAdapter;
import com.example.ciyashop.databinding.ActivitySearchCategoryInnerListBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCategoryInnerListActivity extends BaseActivity implements OnItemClickListener {

    private final List<Home.AllCategory> list = new ArrayList<>();
    private final Map<Integer, List<Home.AllCategory>> childList = new HashMap<>();
    private int cat_id;
    private Bundle bundle;
    private SearchInnerCategoryAdapter searchInnerCategoryAdapter;

    private ActivitySearchCategoryInnerListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchCategoryInnerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbarTheme();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.all_category));
        getIntentData();
        getList(cat_id);
        if (list.size() == 0) {
            finish();
            Intent intent = new Intent(this, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.CATEGORY, cat_id + "");
            intent.putExtra(RequestParamUtils.ORDER_BY, SearchCategoryListActivity.sortBy);
            intent.putExtra(RequestParamUtils.POSITION, SearchCategoryListActivity.sortPosition);
            startActivity(intent);
        }
        showSearch();
        showCart();
        showBackButton();
        setSearchAdapter();
    }

    public void getIntentData() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            cat_id = bundle.getInt(RequestParamUtils.CATEGORY);
        }
    }

    public void setSearchAdapter() {
        searchInnerCategoryAdapter = new SearchInnerCategoryAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvSearchCategory.setLayoutManager(mLayoutManager);
        binding.rvSearchCategory.setAdapter(searchInnerCategoryAdapter);
        binding.rvSearchCategory.setNestedScrollingEnabled(false);
        searchInnerCategoryAdapter.addAll(list, childList);
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public void getList(int id) {
        for (int i = 0; i < Constant.MAINCATEGORYLIST.size(); i++) {
            if (Constant.MAINCATEGORYLIST.get(i).parent == id) {
                list.add(Constant.MAINCATEGORYLIST.get(i));
            }
        }

        for (int j = 0; j < list.size(); j++) {
            List<Home.AllCategory> tempList = new ArrayList<>();
            for (int k = 0; k < Constant.MAINCATEGORYLIST.size(); k++) {
                if (list.get(j).id.intValue() == Constant.MAINCATEGORYLIST.get(k).parent.intValue()) {
                    tempList.add(Constant.MAINCATEGORYLIST.get(k));
                }
            }
            childList.put(list.get(j).id, tempList);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showCart();
    }
}

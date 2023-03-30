package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.Ciyashop;
import com.ciyashop.library.apicall.ConstantValue;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.SearchHomeAdapter;
import com.example.ciyashop.databinding.ActivitySearchFromHomeBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.SearchLive;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchFromHomeActivity extends BaseActivity implements OnItemClickListener {

    private SearchHomeAdapter searchHomeAdapter;
    private DatabaseHelper databaseHelper;
    private boolean isApiCalled = false;

    private ActivitySearchFromHomeBinding binding;
    private LayoutEmptyBinding emptyBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchFromHomeBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        }

        setToolbarTheme();
        databaseHelper = new DatabaseHelper(SearchFromHomeActivity.this);
        setThemeColor();
        setScreenLayoutDirection();
        showBackButton();
        searchClick();
        setEmptyColor();
        setFilter();
        setSearchAdapter();
        setBottomBar("search", binding.svHome);
    }

    private void setThemeColor() {
        binding.line.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.etSearch.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.etSearch.setHintTextColor(Color.parseColor(getPreferences().getString(Constant.APP_TRANSPARENT_VERY_LIGHT, Constant.SECONDARY_COLOR)));
    }

    public void setFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // filter your list from your input
//                filter(s.toString());

                if (s.toString().length() >= 3) {
                    if (binding.etSearch.getText().toString().toLowerCase().equals(ConstantValue.FLAGSTRINGVALUE)) {
                        Log.e("Condition", " become true");
                        new Ciyashop(SearchFromHomeActivity.this).setFlag(new Ciyashop(SearchFromHomeActivity.this).getPreferences(), true);
                    }
                    getCategoryListData();
                }
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });
    }

    public void filter(String text) {
        List<SearchLive> temp = new ArrayList<>();
        for (SearchLive d : databaseHelper.getSearchHistoryList()) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.name.contains(text)) {
                temp.add(d);
            }
        }
//        update recyclerview
        searchHomeAdapter.updateList(temp);
    }

    public void setSearchAdapter() {
        searchHomeAdapter = new SearchHomeAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvSearch.setLayoutManager(mLayoutManager);
        binding.rvSearch.setAdapter(searchHomeAdapter);
        binding.rvSearch.setNestedScrollingEnabled(false);
        if (databaseHelper.getSearchHistoryList() != null) {
            searchHomeAdapter.addAll(databaseHelper.getSearchHistoryList());
        }
        if (searchHomeAdapter.getItemCount() == 0) {
            setEmptyLayout(true);
            searchHomeAdapter.addAll(new ArrayList<>());
        } else {
            setEmptyLayout(false);
        }
    }

    public void searchClick() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    performSearch();
//                try {
//                    logSearchedEvent("Shopping", binding.etSearch.getText().toString(), true);
//                } catch (Exception e) {
//                    Log.e("TAG", "Exception: " + e.getMessage());
//                }
                Intent intent = new Intent(SearchFromHomeActivity.this, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.SEARCH, binding.etSearch.getText().toString());
                startActivity(intent);
                new Handler().postDelayed(() -> {
                    if (!databaseHelper.getSearchItem(binding.etSearch.getText().toString()) && binding.etSearch.getText().toString().length() > 0) {
                        databaseHelper.addToSearchHistory(binding.etSearch.getText().toString());
                    }

                    if (databaseHelper.getSearchHistoryList() != null) {
                        searchHomeAdapter.addAll(databaseHelper.getSearchHistoryList());
                    }
                    binding.etSearch.setText("");
                }, 200);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onItemClick(int position, String name, int outerPos) {
        if (isApiCalled) {
            getProductDetail(outerPos + "");
        } else {
            Intent intent = new Intent(this, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.SEARCH, name);
            intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
            startActivity(intent);
        }
    }

    public void getCategoryListData() {
        if (Utils.isInternetConnected(this)) {
            PostApi postApi = new PostApi(SearchFromHomeActivity.this, RequestParamUtils.getCategoryListData, this, getlanuage());
            try {
                JSONObject jsonObject;
                if (FilterSelectedList.filterJson.equals("")) {
                    jsonObject = new JSONObject();
                } else {
                    jsonObject = new JSONObject(FilterSelectedList.filterJson);
                }
                jsonObject.put(RequestParamUtils.SEARCH, binding.etSearch.getText().toString());
                jsonObject.put(RequestParamUtils.ISAPPVALIDATION, new Ciyashop(SearchFromHomeActivity.this).getPreferences());
                postApi.callPostApi(new URLS().LIVESEARCH + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void getProductDetail(String productId) {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, "getProductDetail", this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.INCLUDE, productId);
                postApi.callPostApi(new URLS().PRODUCT_URL + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getCategoryListData)) {
            isApiCalled = true;
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<SearchLive> searchList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String name = object.getString(RequestParamUtils.name);
                        if (name.length() != 0) {
                            searchList.add(new SearchLive(object.getInt(RequestParamUtils.ID)
                                    , object.getString(RequestParamUtils.name)));
                        }
                    }
                    if (searchList.size() == 0) {
                        setEmptyLayout(true);
                        searchHomeAdapter.addAll(new ArrayList<>());
                    } else {
                        setEmptyLayout(false);
                        searchHomeAdapter.addAll(searchList);
                    }
                } catch (Exception e) {
                    Log.e("Exception is ", e.getMessage());
                    setEmptyLayout(true);
                }
            }
        }
        if (methodName.equals("getProductDetail")) {
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    CategoryList categoryListRider = new Gson().fromJson(
                            jsonArray.get(0).toString(), new TypeToken<CategoryList>() {
                            }.getType());
                    Constant.CATEGORYDETAIL = categoryListRider;

                    if (categoryListRider.type.equals("external")) {

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(categoryListRider.externalUrl));
                        startActivity(browserIntent);
                    } else {
                        Intent intent = new Intent(this, ProductDetailActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                dismissProgress();
            }
        }
    }

    public void setEmptyLayout(boolean isEmpty) {
        if (isEmpty) {
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
            binding.rvSearch.setVisibility(View.GONE);
            emptyBinding.tvEmptyTitle.setText(R.string.search_list_empty);
        } else {
            emptyBinding.llEmpty.setVisibility(View.GONE);
            binding.rvSearch.setVisibility(View.VISIBLE);
        }
        emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!databaseHelper.getSearchItem(binding.etSearch.getText().toString()) && binding.etSearch.getText().toString().length() > 0) {
            databaseHelper.addToSearchHistory(binding.etSearch.getText().toString());
        }
    }
}

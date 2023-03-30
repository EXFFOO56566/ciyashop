package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.SellerProductAdapter;
import com.example.ciyashop.adapter.SellerReviewAdapter;
import com.example.ciyashop.customview.GridSpacingItemDecoration;
import com.example.ciyashop.databinding.ActivitySellerInfoBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.SellerData;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SellerInfoActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private SellerProductAdapter sellerProductAdapter;
    private SellerReviewAdapter sellerReviewAdapter;
    private String sellerInfo;
    private String sellerId;
    private int page = 1;

    private boolean loading = true;
    private DatabaseHelper databaseHelper;

    List<CategoryList> list = new ArrayList<>();
    JSONArray jsonArray = new JSONArray();

    int pastVisibleItems, visibleItemCount, totalItemCount;
    boolean setNoItemFound;

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    private ActivitySellerInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySellerInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();

        setScreenLayoutDirection();
        databaseHelper = new DatabaseHelper(this);
        runOnUiThread(this::setGridRecycleView);
        binding.collapsingToolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.PRIMARY_COLOR)));
        binding.collapsingToolbar.setTitle(getResources().getString(R.string.Seller_Information));
        binding.collapsingToolbar.setContentScrimColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparent_white));
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.collapsingToolbar.setStatusBarScrimColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        setReviewData();
        sellerId = getIntent().getExtras().getString(RequestParamUtils.ID);
        getSellerInfo(true);
        setThemeColor();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initCollapsingToolbar();
    }

    public void setThemeColor() {
        binding.tvName.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.tvContactSeller.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvViewAllReview.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getSellerInfo(boolean dialog) {
        if (Utils.isInternetConnected(this)) {
            if (dialog) {
                showProgress("");
            }
            PostApi postApi = new PostApi(SellerInfoActivity.this, RequestParamUtils.seller, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.PAGE, page);
                jsonObject.put(RequestParamUtils.sellerId, sellerId);
                postApi.callPostApi(new URLS().SELLER + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    private void initCollapsingToolbar() {

        Drawable drawable = binding.toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
        }
        AppBarLayout appBarLayout = findViewById(R.id.appbar);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            Drawable drawable1 = binding.toolbar.getNavigationIcon();
            drawable1.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    binding.toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout1.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    binding.toolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    }
                    drawable1 = binding.toolbar.getNavigationIcon();
                    drawable1.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    binding.toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
                mCurrentState = State.IDLE;
            }
        });
    }

    public void setGridRecycleView() {
        sellerProductAdapter = new SellerProductAdapter(this, this);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(SellerInfoActivity.this, 2, LinearLayoutManager.VERTICAL, false);
        binding.rvCategoryGrid.setLayoutManager(mLayoutManager);
        binding.rvCategoryGrid.setAdapter(sellerProductAdapter);
        binding.rvCategoryGrid.setNestedScrollingEnabled(false);
        binding.rvCategoryGrid.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        binding.nsvSellerData.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 0) {  //check for scroll down
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        if (!setNoItemFound) {
                            loading = false;
                            page = page + 1;
                            getSellerInfo(false);
                        }
                    }
                }
            }
        });
    }

    public void setReviewData() {
        sellerReviewAdapter = new SellerReviewAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvReview.setLayoutManager(mLayoutManager);
        binding.rvReview.setAdapter(sellerReviewAdapter);
        binding.rvReview.setNestedScrollingEnabled(false);
    }

    public void setClickEvent() {
        binding.tvContactSeller.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactSellerActivity.class);
            intent.putExtra(RequestParamUtils.ID, sellerId);
            startActivity(intent);
        });

        binding.tvViewAllReview.setOnClickListener(v -> {
            Intent intent = new Intent(SellerInfoActivity.this, SellerReviewActivity.class);
            intent.putExtra(RequestParamUtils.sellerInfo, sellerInfo);
            startActivity(intent);
        });
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        try {
            String str = jsonArray.get(position).toString();

            JSONObject jsonObject = new JSONObject(str);
            JSONObject jsonObjectSeller = new JSONObject(sellerInfo);
            JSONObject jsonObjectSeller1 = jsonObjectSeller.getJSONObject(RequestParamUtils.sellerInfo);
            jsonObjectSeller1.put(RequestParamUtils.isSeller, true);

            jsonObject.put(RequestParamUtils.sellerInfo, jsonObjectSeller1);

            Constant.CATEGORYDETAIL = new Gson().fromJson(
                    jsonObject.toString(), new TypeToken<CategoryList>() {
                    }.getType());
            Intent intent = new Intent(this, ProductDetailActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.seller)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (loading) {
                        Log.e("if ", "Called");
                        sellerInfo = response;
                        SellerData sellerDataRider = new Gson().fromJson(
                                response, new TypeToken<SellerData>() {
                                }.getType());

                        if (sellerDataRider.sellerInfo.bannerUrl != null && sellerDataRider.sellerInfo.bannerUrl.length() > 0) {
                            binding.ivBannerImage.setVisibility(View.VISIBLE);
                            String bannerUrl = sellerDataRider.sellerInfo.bannerUrl.replace("\\", "");
                            Glide.with(this).load(bannerUrl).error(R.drawable.male).into(binding.ivBannerImage);
                        } else {
                            binding.ivBannerImage.setVisibility(View.INVISIBLE);
                        }
                        if (sellerDataRider.sellerInfo.avatar != null && sellerDataRider.sellerInfo.avatar.length() > 0) {
                            binding.civProfileImage.setVisibility(View.VISIBLE);
                            Glide.with(this).load(sellerDataRider.sellerInfo.avatar.replace("\\", "")).into(binding.civProfileImage);
                        } else {
                            binding.ivBannerImage.setVisibility(View.INVISIBLE);
                        }

                        binding.tvName.setText(sellerDataRider.sellerInfo.storeName);
                        try {
                            binding.tvRating.setText(String.valueOf(Float.parseFloat(sellerDataRider.sellerInfo.sellerRating.rating)));
                        } catch (Exception e) {
                            Log.e("this", "onResponse: " + e);
                            binding.tvRating.setText("0.00");
                        }

                        if (sellerDataRider.sellerInfo.storeDescription != null && sellerDataRider.sellerInfo.storeDescription.length() != 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvStoreDescription.setText(Html.fromHtml(sellerDataRider.sellerInfo.storeDescription, Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                binding.tvStoreDescription.setText(Html.fromHtml(sellerDataRider.sellerInfo.storeDescription));
                            }
                            binding.tvStoreDescription.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvStoreDescription.setVisibility(View.GONE);
                        }

                        if (sellerDataRider.sellerInfo.sellerAddress.length() != 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvSellerAddress.setText(Html.fromHtml(sellerDataRider.sellerInfo.sellerAddress, Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                binding.tvSellerAddress.setText(Html.fromHtml(sellerDataRider.sellerInfo.sellerAddress));
                            }
                            binding.tvSellerAddress.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvSellerAddress.setVisibility(View.GONE);
                        }

                        if (sellerDataRider.sellerInfo.contactSeller) {
                            binding.tvContactSeller.setClickable(true);
                            binding.tvContactSeller.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvContactSeller.setClickable(false);
                            binding.tvContactSeller.setVisibility(View.GONE);
                        }

                        if (sellerDataRider.sellerInfo.reviewList != null && sellerDataRider.sellerInfo.reviewList.size() > 0) {
                            sellerReviewAdapter.addAll(sellerDataRider.sellerInfo.reviewList);
                        } else {
                            binding.llReview.setVisibility(View.GONE);
                        }

                        JSONArray jsonArray1 = jsonObject.getJSONArray(RequestParamUtils.products);
                        if (jsonArray1.length() > 0) {
                            jsonArray = concatArray(jsonArray, jsonArray1);
//                            new setDataInRecycleview().execute(jsonArray1.toString());
                            prepareRecyclerViewInAsync(jsonArray1.toString());
                        } else {
                            setNoItemFound = true;
                        }
                    } else {
                        Log.e("else ", "Called");
                        JSONArray jsonArray1 = jsonObject.getJSONArray(RequestParamUtils.products);
                        if (jsonArray1.length() > 0) {
                            jsonArray = concatArray(jsonArray, jsonArray1);
//                            new setDataInRecycleview().execute(jsonArray1.toString());
                            prepareRecyclerViewInAsync(jsonArray1.toString());
                        } else {
                            setNoItemFound = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                loading = true;
                binding.nsvSellerData.setVisibility(View.VISIBLE);
            }
        }
    }

    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    private void prepareRecyclerViewInAsync(String param) {
        executor.execute(() -> {
            list = new ArrayList<>();
            try {
                JSONArray array = new JSONArray(param);
                for (int i = 0; i < array.length(); i++) {
                    String jsonResponse = array.get(i).toString();
                    CategoryList categoryListRider = new Gson().fromJson(
                            jsonResponse, new TypeToken<CategoryList>() {
                            }.getType());
                    list.add(categoryListRider);
                }
            } catch (JSONException e) {
                Log.e("Json Exception is ", e.getMessage());
            }

            handler.post(() -> sellerProductAdapter.addAll(list));
        });
    }


    /*public class setDataInRecycleview extends AsyncTask<String, String, List<CategoryList>> {
        @Override
        protected List<CategoryList> doInBackground(String... params) {
            Log.e("DoInBackground", "Called");
            list = new ArrayList<>();
            try {
                JSONArray array = new JSONArray(params[0]);

                for (int i = 0; i < array.length(); i++) {
                    String jsonResponse = array.get(i).toString();
                    CategoryList categoryListRider = new Gson().fromJson(
                            jsonResponse, new TypeToken<CategoryList>() {
                            }.getType());
                    list.add(categoryListRider);
                }
            } catch (JSONException e) {
                Log.e("Json Exception is ", e.getMessage());
            }
            return list;
        }


        @Override
        protected void onPostExecute(List<CategoryList> categoryLists) {
            super.onPostExecute(categoryLists);
            Log.e("On Post", "Called");
            sellerProductAdapter.addAll(categoryLists);
        }
    }*/


    private JSONArray concatArray(JSONArray arr1, JSONArray arr2)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }
}

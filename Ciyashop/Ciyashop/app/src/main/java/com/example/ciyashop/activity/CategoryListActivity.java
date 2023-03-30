package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.CategoryGridAdapter;
import com.example.ciyashop.adapter.CategoryListAdapter;
import com.example.ciyashop.adapter.SortAdapter;
import com.example.ciyashop.customview.EqualSpacingItemDecoration;
import com.example.ciyashop.customview.GridSpacingItemDecoration;
import com.example.ciyashop.databinding.ActivityCategoryListBinding;
import com.example.ciyashop.databinding.LayoutCategoryListShimmerBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.FilterOtherOption;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CategoryListActivity extends BaseActivity implements OnResponseListner {

    private final int REQUEST_CODE = 101;

    private ActivityCategoryListBinding binding;
    private LayoutCategoryListShimmerBinding shimmerBinding;
    private LayoutEmptyBinding emptyBinding;

    int pastVisibleItems, visibleItemCount, totalItemCount;
    static List<CategoryList> categoryLists = new ArrayList<>();
    Boolean setNoItemFound = false;
    private static CategoryGridAdapter categoryGridAdapter;
    private CategoryListAdapter categoryListAdapter;
    private SortAdapter sortAdapter;
    private boolean isGrid = false;
    private Bundle bundle;
    private String categoryId, sortBy;
    private int page = 1;
    private int sortPosition;
    private String search;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private boolean loading = true;
    private boolean isDealOfDayFound = false;
    private final boolean isRecentlyAddedFound = false;
    private final boolean isSelectedProductFound = false;
    private String productIds;
    private Boolean feature = false;
    String customerId;
    private GridLayoutManager mLayoutManager;
    private LinearLayoutManager mLayoutManagerList;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        shimmerBinding = LayoutCategoryListShimmerBinding.bind(binding.getRoot());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        prepareActivityLauncher();
        setClickEvent();
        setScreenLayoutDirection();
        setToolbarTheme();
        setEmptyColor();
        settvImage();
        showSearch();
        showCart();
        setThemeColor();
        showBackButton();
        getIntentData();
        FilterSelectedList.filterJson = "";
        getCategoryListData(sortBy + "", true);
        setGridRecycleView();
        setListRecycleView();
        setScrollListener();
        setBottomSheet();
        setSortAdapter();
        binding.llProgress.setVisibility(View.GONE);
        setBottomBar("list", null);
        emptyBinding.tvEmptyDesc.setText(R.string.simply_browse_item);
    }

    private void prepareActivityLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null && result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    finish();
                    startActivity(data);
                }
            }
        });
    }

    private void setThemeColor() {
        binding.ivListOrGrid.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivGrid.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvSort.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvFilter.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TextViewCompat.setCompoundDrawableTintList(binding.tvFilter, ColorStateList.valueOf(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
            TextViewCompat.setCompoundDrawableTintList(binding.tvSort, ColorStateList.valueOf(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        }

        Drawable unwrappedDrawable = emptyBinding.tvContinueShopping.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    public void getIntentData() {
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        bundle = getIntent().getExtras();
        if (bundle != null) {
            categoryId = bundle.getString(RequestParamUtils.CATEGORY);
            sortBy = bundle.getString(RequestParamUtils.ORDER_BY);
            sortPosition = bundle.getInt(RequestParamUtils.POSITION);
            search = bundle.getString(RequestParamUtils.SEARCH);
            search = bundle.getString(RequestParamUtils.SEARCH);
            feature = bundle.getBoolean(RequestParamUtils.FEATURE);
            if (bundle.getString(RequestParamUtils.DEAL_OF_DAY) != null) {
                isDealOfDayFound = true;
                productIds = bundle.getString(RequestParamUtils.DEAL_OF_DAY);
            } else {
                isDealOfDayFound = false;
            }

        }
    }

    public void getCategoryListData(String sortType, boolean isDialogShow) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                // showProgress("");
                if (Config.SHIMMER_VIEW) {
                    shimmerBinding.shimmerViewContainer.startShimmer();
                    shimmerBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
                } else {
                    shimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    showProgress("");
                }
            }

            PostApi postApi = new PostApi(CategoryListActivity.this, RequestParamUtils.getCategoryListData, this, getlanuage());
            try {
                JSONObject jsonObject;
                if (FilterSelectedList.filterJson.equals("")) {
                    jsonObject = new JSONObject();
                } else {
                    jsonObject = new JSONObject(FilterSelectedList.filterJson);
                }
                if (isDealOfDayFound) {
                    jsonObject.put(RequestParamUtils.INCLUDE, productIds);
                }
                if (feature) {
                    jsonObject.put(RequestParamUtils.FEATURE, true);
                }
                jsonObject.put(RequestParamUtils.CATEGORY, categoryId);
                jsonObject.put(RequestParamUtils.USER_ID, customerId);
                jsonObject.put(RequestParamUtils.PAGE, page);
                jsonObject.put(RequestParamUtils.ORDER_BY, sortType);
                jsonObject.put(RequestParamUtils.SEARCH, search);
                postApi.callPostApi(new URLS().PRODUCT_URL + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void setBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        //By default set BottomSheet Behavior as Collapsed and Height 0
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(0);
        //If you want to handle callback of Sheet Behavior you can use below code
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet", "State Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d("Bottom Sheet", "State Dragging");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d("Bottom Sheet", "State Expanded");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d("Bottom Sheet", "State Hidden");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d("Bottom Sheet", "State Settling");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    public void setGridRecycleView() {
        //ivListOrGrid.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
        categoryGridAdapter = new CategoryGridAdapter(this);
        // rvCategoryGrid.getItemAnimator().setChangeDuration(700);
        mLayoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        binding.rvCategoryGrid.setLayoutManager(mLayoutManager);
        // categoryGridAdapter.setHasStableIds(true);
        binding.rvCategoryGrid.setAdapter(categoryGridAdapter);
        binding.rvCategoryGrid.setNestedScrollingEnabled(false);
        binding.rvCategoryGrid.setHasFixedSize(true);
        binding.rvCategoryGrid.setItemViewCacheSize(20);
        binding.rvCategoryGrid.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        categoryGridAdapter.notifyDataSetChanged();
        binding.rvCategoryGrid.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
    }

    public void setListRecycleView() {
        categoryListAdapter = new CategoryListAdapter(this);
        mLayoutManagerList = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvCategoryList.setLayoutManager(mLayoutManagerList);
        binding.rvCategoryList.setAdapter(categoryListAdapter);
        binding.rvCategoryList.setNestedScrollingEnabled(false);
        categoryListAdapter.notifyDataSetChanged();
        binding.rvCategoryList.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.VERTICAL)); // 16px. In practice, you'll want to use getDimensionPixelSize

    }

    private void setScrollListener() {
        binding.nsCategoryList.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (binding.rvCategoryGrid.getVisibility() == View.VISIBLE) {
                    if (scrollY > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                        if (loading) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                if (!setNoItemFound) {
                                    loading = false;
                                    page = page + 1;
                                    Log.e("End ", "Last Item Wow and page no:- " + page);
                                    binding.llProgress.setVisibility(View.VISIBLE);
                                    // progress_wheel.setBarColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

                                    int secondaryColor = Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR));
                                    ColorFilter colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(secondaryColor, BlendModeCompat.SRC_ATOP);

                                    binding.progressWheel.getIndeterminateDrawable().setColorFilter(colorFilter);
                                    getCategoryListData(Constant.getSortList(CategoryListActivity.this).get(sortAdapter.getSelectedPosition()).getSyntext(), false);
                                    //Do pagination.. i.e. fetch new data
                                }
                            }
                        }
                    }
                } else {
                    if (scrollY > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManagerList.getChildCount();
                        totalItemCount = mLayoutManagerList.getItemCount();
                        pastVisibleItems = mLayoutManagerList.findFirstVisibleItemPosition();
                        if (loading) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                if (!setNoItemFound) {
                                    loading = false;
                                    page = page + 1;
                                    Log.e("End ", "Last Item Wow and page no:- " + page);
                                    binding.llProgress.setVisibility(View.VISIBLE);
                                    binding.progressWheel.getIndeterminateDrawable().setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                                    getCategoryListData(Constant.getSortList(CategoryListActivity.this).get(sortAdapter.getSelectedPosition()).getSyntext(), false);
                                    //Do pagination.. i.e. fetch new data
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void setSortAdapter() {
        List<String> sortList = new ArrayList<>();
        sortAdapter = new SortAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvSort.setLayoutManager(mLayoutManager);
        binding.rvSort.setAdapter(sortAdapter);
        binding.rvSort.setNestedScrollingEnabled(false);
        for (int i = 0; i < Constant.getSortList(this).size(); i++) {
            sortList.add(Constant.getSortList(this).get(i).getName());
        }
        sortAdapter.addAll(sortList);
        sortAdapter.setSelectedPosition(sortPosition);
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getCategoryListData)) {
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    categoryLists = new ArrayList<>();
                    if (loading || FilterSelectedList.isFilterCalled) {
                        FilterSelectedList.isFilterCalled = false;
                    }
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            CategoryList categoryListRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<CategoryList>() {
                                    }.getType());
                            categoryLists.add(categoryListRider);
                        }
                    } catch (Exception e) {
                        Log.e("Exception ==> ", e.getMessage());
                    }
                    binding.llCategory.setVisibility(View.VISIBLE);
                    emptyBinding.llEmpty.setVisibility(View.GONE);
                    categoryGridAdapter.addAll(categoryLists);
                    categoryListAdapter.addAll(categoryLists);
                    //   dismissProgress();
                    loading = true;

                    if (Config.SHIMMER_VIEW) {
                        shimmerBinding.shimmerViewContainer.stopShimmer();
                        shimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    } else {
                        dismissProgress();
                    }
                } catch (Exception e) {
                    //dismissProgress();
                    if (Config.SHIMMER_VIEW) {
                        shimmerBinding.shimmerViewContainer.stopShimmer();
                        shimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    } else {
                        dismissProgress();
                    }

                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getString("message").equals(getString(R.string.no_product_found))) {
                            setNoItemFound = true;
                            if (categoryListAdapter.getItemCount() == 0 || categoryGridAdapter.getItemCount() == 0) {
                                binding.llCategory.setVisibility(View.GONE);
                                emptyBinding.llEmpty.setVisibility(View.VISIBLE);
                                emptyBinding.tvEmptyTitle.setText(getString(R.string.no_product_found));
                                emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
                            }
                        }
                    } catch (JSONException e1) {
                        Log.e("noProductJSONException", e1.getMessage());
                    }
                    if (loading || FilterSelectedList.isFilterCalled) {
                        categoryLists = new ArrayList<>();
                        FilterSelectedList.isFilterCalled = false;
                        binding.llCategory.setVisibility(View.GONE);
                        emptyBinding.llEmpty.setVisibility(View.VISIBLE);
                        emptyBinding.tvEmptyTitle.setText(getString(R.string.no_product_found));
                        emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
                    }
                }
            } else {
                emptyBinding.llEmpty.setVisibility(View.VISIBLE);
                emptyBinding.tvEmptyTitle.setText(getString(R.string.no_product_found));
                emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
            }
            binding.llProgress.setVisibility(View.GONE);
        } else if (methodName.equals(RequestParamUtils.removeWishList) || methodName.equals(RequestParamUtils.addWishList)) {
            dismissProgress();
        }  else if (methodName.equals(RequestParamUtils.IsUserExists)){
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("is_user_exists");
                    Log.e("TAG", "onResponse:+++++ "+""+status );
                    if (!status.equals("true")) {
                        setLogoutFornonExistUserDialog();
                        //   Toast.makeText(this, "No Exist", Toast.LENGTH_LONG).show();

                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                }
            }

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(this + "OnRestart: IsFilter" + FilterSelectedList.isFilterCalled, "Json : " + FilterSelectedList.filterJson);


        if (FilterSelectedList.isFilterCalled) {
            page = 1;
            setNoItemFound = false;
            loading = true;
            categoryListAdapter.newList();
            categoryGridAdapter.newList();
            getCategoryListData(Constant.getSortList(CategoryListActivity.this).get(sortAdapter.getSelectedPosition()).getSyntext(), true);
        }
        showCart();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Run whatever background code you want here.

                clearFilter();

            }
        }).start();

    }

    public void clearFilter() {
        for (int i = 0; i < FilterSelectedList.selectedOtherOptionList.size(); i++) {
            FilterOtherOption filterOtherOption = FilterSelectedList.selectedOtherOptionList.get(i);
            List<String> option = filterOtherOption.options;
            for (int j = 0; j < option.size(); j++) {
                option.set(j, "");
            }
        }
        if (FilterSelectedList.selectedColorOptionList.size() > 0) {
            for (int k = 0; k < FilterSelectedList.selectedColorOptionList.get(0).options.size(); k++) {
                FilterSelectedList.selectedColorOptionList.get(0).options.set(k, "");
            }
        }
        FilterSelectedList.isFilterCalled = false;
        FilterActivity.clearFilter = true;
    }

    public void setClickEvent() {
        binding.tvSort.setOnClickListener(v -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                //If state is in collapse mode expand it
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                //else if state is expanded collapse it
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        binding.ivGrid.setOnClickListener(v -> {
            runOnUiThread(() -> {
                binding.rvCategoryGrid.setVisibility(View.VISIBLE);
                binding.rvCategoryGrid.scheduleLayoutAnimation();
                binding.rvCategoryList.setVisibility(View.GONE);
                mLayoutManager.setSpanCount(2);
            });
            isGrid = true;
        });

        binding.ivListOrGrid.setOnClickListener(v -> {
            runOnUiThread(() -> {
                binding.rvCategoryGrid.setVisibility(View.GONE);
                binding.rvCategoryList.setVisibility(View.VISIBLE);
            });
            isGrid = false;
        });

        binding.tvDone.setOnClickListener(v -> {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            page = 1;
            categoryListAdapter.newList();
            categoryGridAdapter.newList();
            getCategoryListData(Constant.getSortList(this).get(sortAdapter.getSelectedPosition()).getSyntext(), true);
        });

        binding.tvCancel.setOnClickListener(v -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        binding.llFilter.setOnClickListener(v -> {
            if (categoryId == null || categoryId.equals("")) {
                Intent intent = new Intent(CategoryListActivity.this, SearchCategoryListActivity.class);
                intent.putExtra(RequestParamUtils.from, RequestParamUtils.filter);
                intent.putExtra(RequestParamUtils.SEARCH, search);
                intent.putExtra(RequestParamUtils.ORDER_BY, Constant.getSortList(CategoryListActivity.this).get(sortAdapter.getSelectedPosition()).getSyntext());
                intent.putExtra(RequestParamUtils.POSITION, sortAdapter.getSelectedPosition());
                Log.e("Harsh", "llFilterClick: " + new Gson().toJson(intent));
                // startActivityForResult is deprecated
                // and onActivityResult() is also replaced in above code
                activityResultLauncher.launch(intent);
//                startActivityForResult(intent, REQUEST_CODE);
            } else {
                Intent intent = new Intent(CategoryListActivity.this, FilterActivity.class);
                intent.putExtra(RequestParamUtils.CATEGORY, categoryId);
                intent.putExtra(RequestParamUtils.onSale, isDealOfDayFound);
                Log.e("Harsh", "llFilterClick: " + new Gson().toJson(intent));
                startActivity(intent);
            }
        });
    }

}

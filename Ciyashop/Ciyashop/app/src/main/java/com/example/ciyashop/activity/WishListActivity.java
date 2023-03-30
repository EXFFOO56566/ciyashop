package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.WishListAdapter;
import com.example.ciyashop.databinding.ActivityWishListBinding;
import com.example.ciyashop.databinding.ItemListCategoryShimmerBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WishListActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private ActivityWishListBinding binding;
    private LayoutEmptyBinding emptyBinding;
    private ItemListCategoryShimmerBinding listCategoryShimmerBinding;

    int pastVisibleItems, visibleItemCount, totalItemCount;
    boolean setNoItemFound;
    List<CategoryList> list = new ArrayList<>();
    private WishListAdapter wishListAdapter;
    private int page = 1;
    private DatabaseHelper databaseHelper;
    private boolean loading = true;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWishListBinding.inflate(getLayoutInflater());
        listCategoryShimmerBinding = ItemListCategoryShimmerBinding.bind(binding.getRoot());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        emptyBinding.llEmpty.setVisibility(View.GONE);
        setClickEvent();


        setScreenLayoutDirection();
        databaseHelper = new DatabaseHelper(this);
        settvTitle(getResources().getString(R.string.my_wish_list));
        showCart();
        hideSearchNotification();
        setToolbarTheme();
        setEmptyColor();
        showBackButton();
        setThemeColor();
        binding.llProgress.setVisibility(View.GONE);
        if (Constant.IS_WISH_LIST_ACTIVE) {
            getWishList(true);
            setWishListAdapter();
        } else {
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        }

        setBottomBar("wishList", binding.svWishList);
        emptyBinding.tvEmptyDesc.setText(R.string.wish_list_no_data);
    }

    private void setThemeColor() {
        binding.tvNoOfItems.setTextColor((Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Constant.IS_WISH_LIST_ACTIVE) {
            list = new ArrayList<>();
            getWishList(true);
            setWishListAdapter();
        } else {
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void getWishList(boolean dialog) {
        String id = null;
        List<String> wishList = databaseHelper.getWishList();
        if (wishList.size() > 0) {
            for (int i = 0; i < wishList.size(); i++) {
                if (id == null) {
                    id = wishList.get(i);
                } else {
                    id = String.format("%s,%s", id, wishList.get(i));
                }
            }
            getWishListData(id, dialog);
        } else {
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void setWishListAdapter() {
        wishListAdapter = new WishListAdapter(this, this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvWishList.setLayoutManager(mLayoutManager);
        binding.rvWishList.setAdapter(wishListAdapter);
        binding.rvWishList.setNestedScrollingEnabled(false);
        binding.svWishList.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            if (!setNoItemFound) {
                                loading = false;
                                page = page + 1;
                                binding.llProgress.setVisibility(View.VISIBLE);
                                binding.progressWheel.getIndeterminateDrawable().setColorFilter(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                                Log.e("End ", "Last Item Wow and page no:- " + page);
                                getWishList(false);
                            }
                        }
                    }
                }
            }
        });
//        rvWishList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if (dy > 0) //check for scroll down
//                {
//                    visibleItemCount = mLayoutManager.getChildCount();
//                    totalItemCount = mLayoutManager.getItemCount();
//                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
//
//                    if (loading) {
//                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
//                            if (setNoItemFound != true) {
//                                loading = false;
//                                page = page + 1;
//                                llProgress.setVisibility(View.VISIBLE);
//                                progress_wheel.setBarColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
//                                Log.e("End ", "Last Item Wow  and page no:- " + page);
//                                getWishList(false);
//                            }
//                        }
//                    }
//                }
//            }
//        });
    }

    public void getWishListData(String data, boolean dialog) {
        if (Utils.isInternetConnected(this)) {
            if (dialog) {
                //  showProgress("");
                if (Config.SHIMMER_VIEW) {
                    listCategoryShimmerBinding.shimmerViewContainer.startShimmer();
                    listCategoryShimmerBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
                } else {
                    listCategoryShimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    showProgress("");
                }
            }
            PostApi postApi = new PostApi(WishListActivity.this, RequestParamUtils.getWishListData, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.PAGE, page);
                jsonObject.put(RequestParamUtils.INCLUDE, data);
                postApi.callPostApi(new URLS().PRODUCT_URL + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        String userid = getPreferences().getString(RequestParamUtils.ID, "");
        if (!userid.equals("")) {
            removeWishList(true, userid, position + "");
        } else {
            if (outerPos - 1 == 0) {
                noDataFound();
            }
        }
        String itemCount = (outerPos - 1) + " " + getString(R.string.items);
        binding.tvNoOfItems.setText(itemCount);
    }

    public void removeWishList(boolean isDialogShow, String userid, String productId) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }
            PostApi postApi = new PostApi(WishListActivity.this, RequestParamUtils.removeWishList, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.USER_ID, userid);
                jsonObject.put(RequestParamUtils.PRODUCT_ID, productId);
                postApi.callPostApi(new URLS().REMOVE_FROM_WISHLIST, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getWishListData)) {
            binding.llProgress.setVisibility(View.GONE);
            loading = true;
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String jsonResponse = jsonArray.get(i).toString();
                        CategoryList categoryListRider = new Gson().fromJson(
                                jsonResponse, new TypeToken<CategoryList>() {
                                }.getType());
                        list.add(categoryListRider);
                    }
                    wishListAdapter.addAll(list);
                    binding.rvWishList.scheduleLayoutAnimation();
                    String itemCount = list.size() + " " + getString(R.string.items);
                    binding.tvNoOfItems.setText(itemCount);
                    emptyBinding.llEmpty.setVisibility(View.GONE);
                    //    dismissProgress();
                    if (Config.SHIMMER_VIEW) {
                        listCategoryShimmerBinding.shimmerViewContainer.stopShimmer();
                        listCategoryShimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    } else {
                        dismissProgress();
                    }
                } catch (Exception e) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getString("message").equals("No product found")) {
                            setNoItemFound = true;
                            if (wishListAdapter.getItemCount() == 0) {
                                noDataFound();
                                emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
                            }
                        }
                    } catch (JSONException e1) {
                        Log.e("noProductJSONException", e1.getMessage());
                    }
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    // dismissProgress();
                    if (Config.SHIMMER_VIEW) {
                        listCategoryShimmerBinding.shimmerViewContainer.stopShimmer();
                        listCategoryShimmerBinding.shimmerViewContainer.setVisibility(View.GONE);
                    } else {
                        dismissProgress();
                    }
                }
            }
        }
        else if (methodName.equals("removeWishList")) {
            if (wishListAdapter.getItemCount() == 0) {
                noDataFound();
            }
            dismissProgress();
        }else if (methodName.equals(RequestParamUtils.IsUserExists)){

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

    public void noDataFound() {
        emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        emptyBinding.tvEmptyTitle.setText(getString(R.string.no_product_found));
    }

    public void setClickEvent() {
        emptyBinding.tvContinueShopping.setOnClickListener(v -> {
            Intent i = new Intent(WishListActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}



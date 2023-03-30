package com.example.ciyashop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.Ciyashop;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.BannerViewPagerAdapter;
import com.example.ciyashop.adapter.CategoryAdapter;
import com.example.ciyashop.adapter.DynamicItemAdapter;
import com.example.ciyashop.adapter.HomeTopCategoryAdapter;
import com.example.ciyashop.adapter.NavigationDrawerAdapter;
import com.example.ciyashop.adapter.RecentViewAdapter;
import com.example.ciyashop.adapter.RecentlyAddedAdapter;
import com.example.ciyashop.adapter.SelectProductAdapter;
import com.example.ciyashop.adapter.SelectedItemAdapter;
import com.example.ciyashop.adapter.SixReasonAdapter;
import com.example.ciyashop.adapter.SpecialOfferAdapter;
import com.example.ciyashop.adapter.TopRatedProductAdapter;
import com.example.ciyashop.adapter.VerticalBannerAdapter;
import com.example.ciyashop.customview.EqualSpacingItemDecoration;
import com.example.ciyashop.customview.GridSpacingItemDecoration;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.databinding.ActivityHomeBinding;
import com.example.ciyashop.databinding.ScrollHomeViewBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.SyncWishList;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.model.NavigationList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityHomeBinding binding;
    private ScrollHomeViewBinding homeBinding;

    private ImageView[] dots;

    private int currentPosition;

    private BannerViewPagerAdapter bannerViewPagerAdapter;

    private HomeTopCategoryAdapter homeTopCategoryAdapter;

    private CategoryAdapter categoryAdapter;

    private VerticalBannerAdapter verticalBannerAdapter;

    private SixReasonAdapter sixReasonAdapter;

    private RecentViewAdapter recentViewAdapter;

    TopRatedProductAdapter topRatedProductAdapter;

    SelectProductAdapter selectProductAdapter;

    DynamicItemAdapter mAdapter;

    SelectedItemAdapter selectedItemAdapter;

    RecentlyAddedAdapter recentlyAddedAdapter;

    private View listHeaderView;

    private TextView tvName;

    private boolean isHead = false;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private NavigationDrawerAdapter navigationDrawerAdapter;

    private DatabaseHelper databaseHelper;

    private boolean isAutoScroll = false;

    private long mBackPressed;

    private Handler handler;

    Home homeRider;

    private String token;

    public String TAG = "Home Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isInfiniteScrollEnable()) {
            finish();
            Intent intent = new Intent(this, InfiniteScrollActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            if(!getPreferences().getString(RequestParamUtils.ID, "").isEmpty()){
             //   CheckfordeletedUser();
            }
            binding = ActivityHomeBinding.inflate(getLayoutInflater());
            homeBinding = ScrollHomeViewBinding.bind(binding.getRoot());
            setContentView(binding.getRoot());

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            Config.IS_RTL = getPreferences().getBoolean(Constant.RTL, false);
            setHomecolorTheme(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR));
            setScreenLayoutDirection();
            settvImage();
            homeBinding.ivBack.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_drawer, null));

            // Get token and Save Notification Token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.isComplete()) {
                    token = task.getResult();
                    Constant.DEVICE_TOKEN = token;
                }
            });

            SharedPreferences.Editor pre = getPreferences().edit();
            pre.putString(RequestParamUtils.NOTIFICATION_TOKEN, token);
            pre.apply();
            setBottomBar("home", homeBinding.svHome);
            getHomeData();
            initDrawer();
            swipeView();
            showCart();
            setHomeCategoryData();
            setView();
            categoryData();
            setToolbarTheme();
//            verticalBannerData();
            setSixReasonAdapter();
            setRecentViewAdapter();
//            getRecentData();
            setClickEvent();

            //sync home
            new SyncWishList(HomeActivity.this).syncWishList(getPreferences().getString(RequestParamUtils.ID, ""), false);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // TODO: Remove this code after the UrlTable2 has been checked in.
    public void getRecentData() {
        databaseHelper = new DatabaseHelper(HomeActivity.this);
        List<CategoryList> recentList = databaseHelper.getRecentViewList();
        recentViewAdapter.addAll(recentList);
        if (recentList.size() > 0) {
            homeBinding.llRecentView.setVisibility(View.VISIBLE);
        } else {
            homeBinding.llRecentView.setVisibility(View.GONE);
        }
    }

    //TODO: API call to get home data from Backend
    public void getHomeData() {
        if (Utils.isInternetConnected(this)) {

            showProgress("");
            try {
                PostApi postApi = new PostApi(this, RequestParamUtils.getHomeData, this, getlanuage());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.appVersion, URLS.version);
                jsonObject.put(RequestParamUtils.appkey, URLS.PURCHASE_KEY);

//                Log.e("?lang=fr: ", getPreferences().getString(RequestParamUtils.DefaultLanguage, "") );

                postApi.callPostApi(new URLS().HOME + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Home", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    //TODO:Refresh Data from backend
    public void swipeView() {
        homeBinding.swipeContainer.setOnRefreshListener(() -> {
            homeBinding.llTopCategory.setVisibility(View.GONE);
            getHomeData();
        });
        // Configure the refreshing colors
        homeBinding.swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.orange,
                R.color.red,
                R.color.blue
        );
    }

    //TODO: set Drawer Data
    public void initDrawer() {
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.MATCH_PARENT);
        if (Config.IS_RTL) {
            params.gravity = GravityCompat.END;
        } else {
            params.gravity = GravityCompat.START;
        }
        binding.drawerListView.setLayoutParams(params);


        LayoutInflater inflater = getLayoutInflater();
        listHeaderView = inflater.inflate(R.layout.nav_header, null, false);
        tvName = listHeaderView.findViewById(R.id.tvName);
        if (!isHead) {
            binding.drawerListView.addHeaderView(listHeaderView);
            isHead = true;
        }
        navigationDrawerAdapter = new NavigationDrawerAdapter(this);
        binding.drawerListView.setAdapter(navigationDrawerAdapter);

        homeBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.drawerLayout.openDrawer(binding.drawerListView);

            }
        });


        actionBarDrawerToggle = new ActionBarDrawerToggle(HomeActivity.this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // creates call to onPrepareOptionsMenu()
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        binding.drawerListView.setOnItemClickListener((parent, view, position, id) -> selectItemFragment(position - 1));
        setBottomBar("home", homeBinding.svHome);
    }

    private void selectItemFragment(int position) {
        if (position != -1) {
            if (position < navigationDrawerAdapter.getSeparator()) {
                Intent intent = new Intent(this, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.CATEGORY, navigationDrawerAdapter.getList().get(position).mainCatId);
                intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
                startActivity(intent);
            } else if (position == navigationDrawerAdapter.getSeparator()) {
                Intent intent = new Intent(this, SearchCategoryListActivity.class);
                startActivity(intent);
            } else {
                selectLocalFragment(navigationDrawerAdapter.getList().get(position).mainCatName);
            }
        }
        binding.drawerListView.setItemChecked(position, true);
        new Handler().postDelayed(() -> binding.drawerLayout.closeDrawer(binding.drawerListView), 200);
    }

    public void selectLocalFragment(String name) {
        if (name.equals(getResources().getString(R.string.notification))) {
            Intent notificationIntent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        } else if (name.equals(getResources().getString(R.string.my_reward))) {
            Intent rewardIntent = new Intent(HomeActivity.this, RewardsActivity.class);
            startActivity(rewardIntent);
        } else if (name.equals(getResources().getString(R.string.my_cart))) {
            Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(cartIntent);
        } else if (name.equals(getResources().getString(R.string.my_wish_list))) {
            Intent wishListIntent = new Intent(HomeActivity.this, WishListActivity.class);
            startActivity(wishListIntent);
        } else if (name.equals(getResources().getString(R.string.my_account))) {
            Intent accountIntent = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(accountIntent);
        } else if (name.equals(getResources().getString(R.string.my_orders))) {
            Intent myOrderIntent = new Intent(HomeActivity.this, MyOrderActivity.class);
            startActivity(myOrderIntent);
        }
    }

    //TODO used to set home category data
    public void setHomeCategoryData() {
        homeBinding.rvTopCategory.setVisibility(View.VISIBLE);

        homeTopCategoryAdapter = new HomeTopCategoryAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homeBinding.rvTopCategory.setLayoutManager(mLayoutManager);
        homeBinding.rvTopCategory.setAdapter(homeTopCategoryAdapter);
        homeBinding.rvTopCategory.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(homeBinding.rvTopCategory, false);
        homeBinding.rvTopCategory.setHasFixedSize(true);
        homeBinding.rvTopCategory.setItemViewCacheSize(20);

        homeBinding.rvTopCategory.setDrawingCacheEnabled(true);
        homeBinding.rvTopCategory.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

    }

    //TODO used to set category data
    public void categoryData() {
        categoryAdapter = new CategoryAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homeBinding.rvCategory.setLayoutManager(mLayoutManager);
        homeBinding.rvCategory.setHasFixedSize(true);
        homeBinding.rvCategory.setAdapter(categoryAdapter);
        homeBinding.rvCategory.setNestedScrollingEnabled(false);
        if (!Config.IS_RTL) {
            homeBinding.rvCategory.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize

        }
        homeBinding.rvCategory.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
    }

    public void verticalBannerData() {
        verticalBannerAdapter = new VerticalBannerAdapter(HomeActivity.this, this);
        homeBinding.rvVerticalBanner.setAdapter(verticalBannerAdapter);
        homeBinding.rvVerticalBanner.setAnimationEnabled(true);
        homeBinding.rvVerticalBanner.setFadeEnabled(true);
        homeBinding.rvVerticalBanner.setFadeFactor(0.6f);
//        rvVerticalBanner.setCurrentItem(1);
    }

    public void setSixReasonAdapter() {
        sixReasonAdapter = new SixReasonAdapter(this, this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        homeBinding.rvSixReason.setLayoutManager(mLayoutManager);
        homeBinding.rvSixReason.setAdapter(sixReasonAdapter);
        homeBinding.rvSixReason.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(homeBinding.rvSixReason, false);
        homeBinding.rvSixReason.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        homeBinding.rvSixReason.setHasFixedSize(true);
        homeBinding.rvSixReason.setItemViewCacheSize(20);
    }

    public void setRecentViewAdapter() {
        homeBinding.tvRecentNameOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        homeBinding.tvRecentNameTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        recentViewAdapter = new RecentViewAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homeBinding.rvRecentOffer.setLayoutManager(mLayoutManager);
        homeBinding.rvRecentOffer.setAdapter(recentViewAdapter);
        homeBinding.rvRecentOffer.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(homeBinding.rvRecentOffer, false);
        homeBinding.rvRecentOffer.setHasFixedSize(true);
        homeBinding.rvRecentOffer.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        homeBinding.rvRecentOffer.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize
        recentViewAdapter.notifyDataSetChanged();

    }

    private void setView() {
        bannerViewPagerAdapter = new BannerViewPagerAdapter(this);
        homeBinding.vpBanner.setAdapter(bannerViewPagerAdapter);
        homeBinding.vpBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position, bannerViewPagerAdapter.getCount());
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void autoScroll() {
        new Handler().postDelayed(() -> new Handler().postDelayed(() -> {
            if (currentPosition == bannerViewPagerAdapter.getCount() - 1) {
                currentPosition = 0;
            } else {
                currentPosition = currentPosition + 1;
            }
            homeBinding.vpBanner.setCurrentItem(currentPosition);
            addBottomDots(currentPosition, bannerViewPagerAdapter.getCount());
            autoScroll();
        }, 6000), 1000);
    }

    private void addBottomDots(int currentPage, int length) {
        homeBinding.layoutDots.removeAllViews();
        dots = new ImageView[length];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_dash, null));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 15, 0);
            dots[i].setLayoutParams(lp);
            homeBinding.layoutDots.addView(dots[i]);
        }
        if (dots.length > 0 && dots.length >= currentPage) {
            dots[currentPage].setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public void setClickEvent() {
        homeBinding.ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchFromHomeActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getHomeData)) {
            if (response != null && response.length() > 0) {
                homeBinding.swipeContainer.setRefreshing(false);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    //Convert json response into gson and made model class
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    homeRider = gson.fromJson(
                            jsonObject.toString(), new TypeToken<Home>() {
                            }.getType());

                    try {
                        Constant.IS_ADD_TO_CART_ACTIVE = jsonObject.has("pgs_woo_api_add_to_cart_option") && jsonObject.getString("pgs_woo_api_add_to_cart_option").equals("enable");
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }

                    try {
                        if (jsonObject.has("pgs_woo_api_catalog_mode_option") && jsonObject.getString("pgs_woo_api_catalog_mode_option").equals("enable")) {
                            Config.IS_CATALOG_MODE_OPTION = true;
                            showCart();
                        } else {
                            Config.IS_CATALOG_MODE_OPTION = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (homeRider.webViewPages != null && !homeRider.webViewPages.isEmpty()) {
                        Constant.WEBVIEWPAGES = new ArrayList<>();
                        Constant.WEBVIEWPAGES.addAll(homeRider.webViewPages);
                    }


                    if (Config.IS_CATALOG_MODE_OPTION) {
                        llCart.setVisibility(View.GONE);
                    } else {
                        llCart.setVisibility(View.VISIBLE);
                    }

                    checkReview(jsonObject);

//                    runOnUiThread(new Runnable() {
//                        @SuppressLint("NewApi")
//                        @Override
//                        public void run() {

                    homeBinding.llMain.setVisibility(View.VISIBLE);
                    llBottomBar.setVisibility(View.VISIBLE);


                    if (homeRider != null) {
                        if (homeRider.isAppValidation != null) {
                            new Ciyashop(HomeActivity.this).setFlag(homeRider.isAppValidation, false);
                        }

                        //set all constant value from the response
                        setConstantValue();

                        //set delete diloge data
                        Constant.delete_account_alert_title=homeRider.delete_account_alert_title;

                        Constant.delete_account_alert_message=homeRider.delete_account_alert_message;




                        //set  all color from the response into preferences
                        setColorPreferences(homeRider.appColor.primaryColor, homeRider.appColor.secondaryColor, homeRider.appColor.headerColor);


                        //set color into toolbar of home activity
                        setHomecolorTheme(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR));


                        //set theme color from the response
                        setThemeIconColor();


                        //set lungage into local
                        setLocale(homeRider.siteLanguage);
                        setText();

                        //CheckOut URLs get
                        if (homeRider.checkoutRedirectUrl != null && homeRider.checkoutRedirectUrl.size() > 0) {
                            setCheckoutURL(homeRider.checkoutRedirectUrl);
                        }

                        //set carousel product
                        if (homeRider.productsCarousel != null) {
                            AddNewCarousel();
                        } else {
                            homeBinding.llMenus.removeAllViews();
                            if (homeRider.popularProducts != null) {
                                AddPopularProducts();
                            }
                            if (homeRider.scheduledSaleProducts != null) {
                                AddSpecialDealProducts();
                            }
                        }

                        //set lungage list  from the response
                        if (homeRider.isWpmlActive != null && homeRider.isWpmlActive) {
                            if (homeRider.wpmlLanguages != null) {
                                Constant.LANGUAGELIST = (homeRider.wpmlLanguages);
                            }
                        } else {
                            SharedPreferences.Editor pre = getPreferences().edit();
                            pre.putString(RequestParamUtils.LANGUAGE, "");
                            pre.apply();
                        }

                        //set currency list from response
                        setCurrency(response);
                    }

                    if (homeRider != null) {
                        for (int i = 0; i < homeRider.allCategories.size(); i++) {
                            if (homeRider.allCategories.get(i).name.equals("Uncategorized")) {
                                homeRider.allCategories.remove(i);
                            }
                        }
                    }

                    Constant.MAINCATEGORYLIST.clear();
                    Constant.MAINCATEGORYLIST.addAll(homeRider.allCategories);

                    //set main category list from response
                    setMainCategoryList(homeRider.mainCategory);

                    //set banner slider from response
                    setSliderList(homeRider.mainSlider);

                    //set top banner
                    setCategoryList(homeRider.categoryBanners);

                    //set vertical banner
                    setVerticalBannerList(homeRider.bannerAd);

                    //set feature box or six reason from response
                    if (homeRider.featureBoxStatus != null && homeRider.featureBoxStatus.equals("enable")) {
                        setSixReasonsList(homeRider.featureBox, homeRider.featureBoxHeading);
                    } else {
                        homeBinding.llSixReason.setVisibility(View.GONE);
                    }

                    SharedPreferences.Editor editor = getPreferences().edit();
                    editor.putString(Constant.APPLOGO, homeRider.appLogo);
                    editor.putString(Constant.APPLOGO_LIGHT, homeRider.appLogoLight);
                    editor.apply();
                    settvImage();
                    try {
                        if (homeRider.notificationIcon != null && homeRider.notificationIcon.contains("https:")) {
                            Glide.with(this).load(homeRider.notificationIcon).into(ivNotification);
                        } else {
                            Glide.with(this).load("https:" + homeRider.notificationIcon).into(ivNotification);
                        }
                        ivNotification.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

                    } catch (Exception e) {
                        Log.e("TAG", "Exception: " + e.getMessage());
                    }

                    if (homeRider.product_banners_cat_value != null && homeRider.product_banners_cat_value.equals("enable")) {
                        Log.e(TAG, "AddCustomSection: ");

                    }
                    Log.e(TAG, "AddCustomSection: " + new Gson().toJson(homeRider.custom_section));
                    AddCustomSection();

                    if (homeRider.isVerified.equals("no")) {
                        getPreferences().edit().putBoolean(RequestParamUtils.VERIFIED, false).apply();
                    } else {
                        getPreferences().edit().putBoolean(RequestParamUtils.VERIFIED, true).apply();
                    }

                    dismissProgress();
                } catch (Exception e) {
                    // dismissProgress();
                    dismissProgress();

                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }


            getRecentData();

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

    public void setText() {
        setBottomBar("home", homeBinding.svHome);
    }

    private void setCheckoutURL(List<String> checkoutRedirectUrl) {
        Constant.CheckoutURL.clear();
        Constant.CheckoutURL = new ArrayList<>();
        Constant.CheckoutURL.addAll(checkoutRedirectUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setThemeIconColor() {
        homeBinding.crMain.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        homeBinding.tvRecentNameOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        homeBinding.tvRecentNameTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void setConstantValue() {

        if (homeRider.pgsAppContactInfo != null) {
            if (homeRider.pgsAppContactInfo.addressLine1 != null) {
                Log.e(TAG, "setConstantValue: Address 1: " + homeRider.pgsAppContactInfo.addressLine1);
                Constant.ADDRESS_LINE1 = homeRider.pgsAppContactInfo.addressLine1;
            }
            if (homeRider.pgsAppContactInfo.addressLine2 != null) {
                Log.e(TAG, "setConstantValue: Address 2: " + homeRider.pgsAppContactInfo.addressLine2);
                Constant.ADDRESS_LINE2 = homeRider.pgsAppContactInfo.addressLine2;
            }
            if (homeRider.pgsAppContactInfo.email != null) {
                Log.e(TAG, "setConstantValue: Email: " + homeRider.pgsAppContactInfo.email);
                Constant.EMAIL = homeRider.pgsAppContactInfo.email;
            }
            if (homeRider.pgsAppContactInfo.phone != null) {
                Log.e(TAG, "setConstantValue: Phone: " + homeRider.pgsAppContactInfo.phone);
                Constant.PHONE = homeRider.pgsAppContactInfo.phone;

            }
            if (homeRider.pgsAppContactInfo.whatsappNo != null) {
                Log.e(TAG, "setConstantValue: Whatsapp: " + homeRider.pgsAppContactInfo.whatsappNo);
                Constant.WHATSAPP = homeRider.pgsAppContactInfo.whatsappNo;
            }
            if (homeRider.pgsAppContactInfo.whatsappFloatingButton != null) {
                Constant.WHATSAPPENABLE = homeRider.pgsAppContactInfo.whatsappFloatingButton;
                //Constant.WHATSAPPENABLE = "disable";
            }
            if (homeRider.priceFormateOptions.currencyCode != null) {
                Constant.CURRENCYCODE = Html.fromHtml(homeRider.priceFormateOptions.currencyCode).toString();
            }
        }
        if (homeRider.isCurrencySwitcherActive != null) {
            Constant.IS_CURRENCY_SWITCHER_ACTIVE = homeRider.isCurrencySwitcherActive;
        }
        if (homeRider.isGuestCheckoutActive != null) {
            Constant.IS_GUEST_CHECKOUT_ACTIVE = homeRider.isGuestCheckoutActive;
        }
        if (homeRider.isWpmlActive != null) {
            Constant.IS_WPML_ACTIVE = homeRider.isWpmlActive;
        }
        Log.e("Home Activity", "isOrderTrackingActive: " + homeRider.isOrderTrackingActive);
        if (homeRider.isOrderTrackingActive != null) {
            Constant.IS_ORDER_TRACKING_ACTIVE = homeRider.isOrderTrackingActive;

        }

        if (homeRider.isRewardPointsActive != null) {
            Constant.IS_REWARD_POINT_ACTIVE = homeRider.isRewardPointsActive;
        }

        if (homeRider.isWishlistActive != null) {
            Log.e("Home Activity", "isWishlistActive: " + homeRider.isWishlistActive);
            Constant.IS_WISH_LIST_ACTIVE = homeRider.isWishlistActive;
        }

        if (homeRider.isYithFeaturedVideoActive != null) {
            Constant.IS_YITH_FEATURED_VIDEO_ACTIVE = homeRider.isYithFeaturedVideoActive;
        }

        if (homeRider.pgsAppSocialLinks != null) {
            Log.e(TAG, "setConstantValue: Social Links:  " + homeRider.pgsAppSocialLinks);
            Constant.SOCIALLINK = homeRider.pgsAppSocialLinks;
        }

        if (homeRider.priceFormateOptions != null) {
            if (homeRider.priceFormateOptions.currencyPos != null) {
                Constant.CURRENCYSYMBOLPOSTION = homeRider.priceFormateOptions.currencyPos;
            }
            if (homeRider.priceFormateOptions.currencySymbol != null) {
                Constant.CURRENCYSYMBOL = Html.fromHtml(homeRider.priceFormateOptions.currencySymbol).toString();
                SharedPreferences.Editor pre = getPreferences().edit();
                pre.putString(Constant.CURRENCYSYMBOLPref, Html.fromHtml(homeRider.priceFormateOptions.currencySymbol).toString());
                pre.apply();
            }
            if (homeRider.priceFormateOptions.decimals != null) {
                Constant.Decimal = homeRider.priceFormateOptions.decimals;
            }
            if (homeRider.priceFormateOptions.decimalSeparator != null) {
                Constant.DECIMALSEPRETER = homeRider.priceFormateOptions.decimalSeparator;
            }
            if (homeRider.priceFormateOptions.thousandSeparator != null) {
                Constant.THOUSANDSSEPRETER = homeRider.priceFormateOptions.thousandSeparator;
            }
        }
        if (homeRider.appLogo != null) {
            Constant.APPLOGO = homeRider.appLogo;
        }
        if (homeRider.appLogoLight != null) {
            Constant.APPLOGO_LIGHT = homeRider.appLogoLight;
        }

        if (getPreferences().getString(RequestParamUtils.LANGUAGE, "").equals("")) {
            if (homeRider.isRtl != null) {
                Config.IS_RTL = homeRider.isRtl;
                getPreferences().edit().putBoolean(Constant.RTL, Config.IS_RTL).apply();
            }
        } else {
            Config.IS_RTL = getPreferences().getBoolean(Constant.RTL, false);
        }

        if (homeRider.pgsWooApiDeliverPincode != null) {
            Config.WOO_API_DELIVER_PINCODE = homeRider.pgsWooApiDeliverPincode.status != null && homeRider.pgsWooApiDeliverPincode.status.equals("enable");
            if (homeRider.pgsWooApiDeliverPincode.settingOptions == null) {
                Home.SettingOptions settingOptions = new Home().getSettingOption();
                settingOptions.availableatText = getString(R.string.available_text);
                settingOptions.codAvailableMsg = getString(R.string.cod_available_msg);
                settingOptions.codDataLabel = getString(R.string.cod_data_label);
                settingOptions.codHelpText = getString(R.string.cod_help_text);
                settingOptions.codNotAvailableMsg = getString(R.string.cod_not_available_msg);
                settingOptions.delDataLabel = getString(R.string.del_data_label);
                settingOptions.delHelpText = getString(R.string.del_help_text);
                settingOptions.delSaturday = getString(R.string.del_saturday);
                settingOptions.delSunday = getString(R.string.del_sunday);
                settingOptions.errorMsgBlank = getString(R.string.error_msg_blank);
                settingOptions.errorMsgCheckPincode = getString(R.string.error_msg_check_pincode);
                settingOptions.pincodePlaceholderTxt = getString(R.string.pincode_placeholder_txt);
                Constant.settingOptions = settingOptions;
            } else {
                Constant.settingOptions = homeRider.pgsWooApiDeliverPincode.settingOptions;
            }
        }
    }

    public void setColorPreferences(String primaryColor, String secondaryColor, String HeaderColor) {


        SharedPreferences.Editor editor = getPreferences().edit();

        //primary color
        if (!primaryColor.equals("")) {

            String PrimaryColorString =primaryColor;
            try {
                Color.parseColor(primaryColor);
                PrimaryColorString = primaryColor;
                Log.e(TAG, "onResponse:color1 try " + PrimaryColorString);
            } catch (Exception e) {
                PrimaryColorString = Constant.PRIMARY_COLOR;
                Log.e(TAG, "onResponse:color1 catch "+e.getMessage() +PrimaryColorString);
            }

            String colorSubString="";
            try{

                 colorSubString = (secondaryColor.substring(primaryColor.lastIndexOf("#") + 1));
                Log.e(TAG, "setColorPreferences: "+colorSubString );
            }catch (Exception e){
                Log.e(TAG, "onResponse:color1 catch  APP_TRANSPARENT"+e.getMessage() +PrimaryColorString);
            }


            editor.putString(Constant.APP_COLOR, PrimaryColorString);
            editor.putString(Constant.APP_TRANSPARENT, "#80" + colorSubString);
            editor.putString(Constant.APP_TRANSPARENT_VERY_LIGHT, "#44" + colorSubString);

        } else {

            editor.putString(Constant.APP_COLOR, Constant.PRIMARY_COLOR);
            editor.putString(Constant.APP_TRANSPARENT, "#00000000");
        }

        //secondary color
        if (!secondaryColor.equals("")) {
           String SecondColorString =secondaryColor;
            try {
                Color.parseColor(secondaryColor);
                SecondColorString = secondaryColor;
                Log.e(TAG, "onResponse:color2 try" +SecondColorString);
            } catch (Exception e) {
                SecondColorString = Constant.SECONDARY_COLOR;
                Log.e(TAG, "onResponse:color2 catch "+e.getMessage()+SecondColorString );
            }
            editor.putString(Constant.SECOND_COLOR, SecondColorString);

        } else {
            editor.putString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR);
        }

        //Headcolor
        if (HeaderColor != null && !HeaderColor.equals("")) {

            String HeadColorString=HeaderColor;
            //check color is parceable or not
            try {
                Color.parseColor(HeadColorString);
                HeadColorString = HeadColorString;
                Log.e(TAG, "onResponse:color3 try "  );
            } catch (Exception e) {

                HeadColorString = Constant.PRIMARY_COLOR;
                Log.e(TAG, "onResponse:color3 catch "+e.getMessage() );
            }


            editor.putString(Constant.HEADER_COLOR, HeadColorString);
        } else {
            editor.putString(Constant.HEADER_COLOR, Constant.PRIMARY_COLOR);
        }
        editor.apply();
        showCart();
    }

    public void setMainCategoryList(List<Home.MainCategory> list) {
        if (list != null) {
            List<Home.MainCategory> mainCategoryList = new ArrayList<>();
            if (list.size() > 0) {
                if (list.size() > 4) {
                    for (int i = 0; i <= 4; i++) {
                        mainCategoryList.add(list.get(i));
                    }
                } else {
                    mainCategoryList.addAll(list);
                }
                Home.MainCategory mainCategory = new Home().getInstranceMainCategory();
                mainCategory.mainCatName = getString(R.string.more);
                mainCategoryList.add(mainCategory);
                homeTopCategoryAdapter.addAll(mainCategoryList);
                homeBinding.llTopCategory.setVisibility(View.VISIBLE);

            } else {
                homeBinding.llTopCategory.setVisibility(View.GONE);
            }
            navigationDrawerAdapter.setSeparator(mainCategoryList.size() - 1);
            List<Home.MainCategory> drawerList = new ArrayList<>();
            drawerList.addAll(mainCategoryList);
            NavigationList.getInstance(this);
            for (int i = 0; i < NavigationList.getImageList().size(); i++) {
                Home.MainCategory mainCategory = new Home().getInstranceMainCategory();
                mainCategory.mainCatName = NavigationList.getTitleList().get(i);
                mainCategory.mainCatImage = NavigationList.getImageList().get(i) + "";
                mainCategory.mainCatId = i + "";
                drawerList.add(mainCategory);
            }
            navigationDrawerAdapter.addAll(drawerList);
        } else {
            homeBinding.llTopCategory.setVisibility(View.GONE);
            navigationDrawerAdapter.setSeparator(0);
            List<Home.MainCategory> drawerList = new ArrayList<>();
            NavigationList.getInstance(this);
            for (int i = 0; i < NavigationList.getImageList().size(); i++) {
                Home.MainCategory mainCategory = new Home().getInstranceMainCategory();
                mainCategory.mainCatName = NavigationList.getTitleList().get(i);
                mainCategory.mainCatImage = NavigationList.getImageList().get(i) + "";
                mainCategory.mainCatId = i + "";
                drawerList.add(mainCategory);
            }
            navigationDrawerAdapter.addAll(drawerList);
        }
    }

    public void setSliderList(List<Home.MainSlider> list) {
        if (list != null) {
            if (list.size() > 0) {
                bannerViewPagerAdapter.addAll(list);
                if (!isAutoScroll) {
                    addBottomDots(0, bannerViewPagerAdapter.getCount());
                    autoScroll();
                    isAutoScroll = true;
                }
                homeBinding.llBanner.setVisibility(View.VISIBLE);
            } else {
                homeBinding.llBanner.setVisibility(View.GONE);
            }
        } else {
            homeBinding.llBanner.setVisibility(View.GONE);
        }
    }

    public void setCategoryList(List<Home.CategoryBanner> list) {
        if (list != null) {
            if (list.size() > 0) {
                categoryAdapter.addAll(list);
                homeBinding.llCategory.setVisibility(View.VISIBLE);
            } else {
                homeBinding.llCategory.setVisibility(View.GONE);
            }
        } else {
            homeBinding.llCategory.setVisibility(View.GONE);
        }
    }

    public void setVerticalBannerList(List<Home.BannerAd> list) {
        if (list != null) {
            if (list.size() > 0) {
                verticalBannerData();
                verticalBannerAdapter.addAll(list);
                homeBinding.rvVerticalBanner.setCurrentItem(1);
                homeBinding.llVerticalBanner.setVisibility(View.VISIBLE);
            } else {
                homeBinding.llVerticalBanner.setVisibility(View.GONE);
            }
        } else {
            homeBinding.llVerticalBanner.setVisibility(View.GONE);
        }
    }

    public void setSixReasonsList(List<Home.FeatureBox> list, String title) {
        if (list != null) {
            if (list.size() > 0) {
                sixReasonAdapter.addAll(list);

                if (list.size() == 1) {
                    if (list.get(0).featureContent.equals("")) {
                        homeBinding.llSixReason.setVisibility(View.GONE);
                    } else {
                        homeBinding.llSixReason.setVisibility(View.VISIBLE);
                    }
                } else {
                    homeBinding.llSixReason.setVisibility(View.VISIBLE);
                }
            } else {
                homeBinding.llSixReason.setVisibility(View.GONE);
            }
//            tvSixResonTitle.setText(title);
        } else {
            homeBinding.llSixReason.setVisibility(View.GONE);
        }

        if (title.contains(" ")) {
            String[] array = title.split(" ");
            if (array.length > 0) {
                homeBinding.tvReasonNameOne.setText(array[0].toUpperCase());
                String secondName = array[1];
                for (int i = 2; i < array.length; i++) {

                    secondName = String.format("%s %s", secondName, array[i]);

                }

                homeBinding.tvReasonNameTwo.setText(secondName.toUpperCase());
            } else {
                homeBinding.tvReasonNameOne.setText(title);
            }
        } else {
            homeBinding.tvReasonNameOne.setText(title);
        }
        homeBinding.tvReasonNameOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        homeBinding.tvReasonNameTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));


    }

    private void setTimer(final TextView tvHour, TextView tvMinute, TextView tvSecond, final LinearLayout llSpecialOffer) {
        if (handler != null) {
            handler.removeCallbacks(null);

        } else {
            handler = new Handler();
        }

        final int delay = 1000; //milliseconds
        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                handler.postDelayed(this, delay);
                String timer = tvHour.getText().toString() + ":" + tvMinute.getText().toString() + ":" + tvSecond.getText().toString();
                long time = convertInMilliSecond(timer) - 1000;
                if (time == 0) {
                    llSpecialOffer.setVisibility(View.GONE);
                } else {
                    tvHour.setText(String.valueOf(TimeUnit.MILLISECONDS.toHours(time)));
                    tvMinute.setText(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))));
                    tvSecond.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))));

                }
            }
        }, delay);
    }

    private long convertInMilliSecond(String time) {
        String[] tokens = time.split(":");
        int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
        int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
        int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
        return secondsToMs + minutesToMs + hoursToMs;
    }

    private String convertInTimeFormat(long millis) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getRecentData();
        showCart();
        if (Constant.IS_CURRENCY_SET) {
            getHomeData();
            databaseHelper.clearRecentItem();
            databaseHelper.clearCart();
            Constant.IS_CURRENCY_SET = false;
        }
        if (recentViewAdapter != null) {
            recentViewAdapter.notifyDataSetChanged();
        }
        if (topRatedProductAdapter != null) {
            topRatedProductAdapter.notifyDataSetChanged();
        }
        if (selectedItemAdapter != null) {
            selectedItemAdapter.notifyDataSetChanged();
        }
        if (selectProductAdapter != null) {
            selectProductAdapter.notifyDataSetChanged();
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (recentlyAddedAdapter != null) {
            recentlyAddedAdapter.notifyDataSetChanged();
        }
    }

    public void AddNewCarousel() {

        homeBinding.llMenus.removeAllViews();
        if (homeRider.productsViewOrders != null && homeRider.productsViewOrders.size() > 0) {
            for (int i = 0; i < homeRider.productsViewOrders.size(); i++) {
                if (homeRider.productsViewOrders.get(i).name.equals(RequestParamUtils.recentProducts)) {
                    new Handler().post(this::AddRecentProducts);
                }

                if (homeRider.productsViewOrders.get(i).name.equals(RequestParamUtils.specialDealProducts)) {
                    new Handler().post(this::AddSpecialDealProducts);
                }
                if (homeRider.productsViewOrders.get(i).name.equals(RequestParamUtils.featureProducts)) {
                    new Handler().post(this::AddFeatureProducts);
                }
                if (homeRider.productsViewOrders.get(i).name.equals(RequestParamUtils.popularProducts)) {
                    new Handler().post(this::AddPopularProducts);
                }
                if (homeRider.productsViewOrders.get(i).name.equals(RequestParamUtils.TOPRATEDPRODUCT)) {
                    new Handler().post(this::AddTopRatedProducts);
                }
            }
        }
    }

    public void AddRecentProducts() {
        homeBinding.llmenusOne.removeAllViews();

        if (homeRider.productsCarousel.recentProducts != null && homeRider.productsCarousel.recentProducts.status.equals("enable") && homeRider.productsCarousel.recentProducts.products.size() > 0) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dynamic_mostpopuler, null);

            TextView tvViewAll = view.findViewById(R.id.tvViewAll);
            RecyclerView rvProducts = view.findViewById(R.id.rvProducts);

            recentlyAddedAdapter = new RecentlyAddedAdapter(HomeActivity.this);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

            rvProducts.setLayoutManager(mLayoutManager);
            rvProducts.setNestedScrollingEnabled(false);
            rvProducts.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize
            rvProducts.setAdapter(recentlyAddedAdapter);
            recentlyAddedAdapter.addAll(homeRider.productsCarousel.recentProducts.products);

            String product_name = getResources().getString(R.string.recently_view_product);
            if (!homeRider.productsCarousel.recentProducts.title.isEmpty()) {
                product_name = homeRider.productsCarousel.recentProducts.title;
            }

            if (!Config.IS_RTL) {
                rvProducts.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize

            }
            setViewAllText(product_name, view);

            tvViewAll.setText(getResources().getString(R.string.view_all));

            tvViewAll.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.POSITION, 0);
                startActivity(intent);
            });
            homeBinding.llmenusOne.addView(view);
        }
    }

    public void AddFeatureProducts() {
        homeBinding.llmenusTwo.removeAllViews();
        if (homeRider.productsCarousel.featureProducts != null && homeRider.productsCarousel.featureProducts.status.equals("enable") && homeRider.productsCarousel.featureProducts.products.size() > 0) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dynamic_mostpopuler, null);
            TextView tvViewAll = view.findViewById(R.id.tvViewAll);
            RecyclerView rvProducts = view.findViewById(R.id.rvProducts);

            selectProductAdapter = new SelectProductAdapter(HomeActivity.this);
            selectProductAdapter.addAll(homeRider.productsCarousel.featureProducts.products);

            rvProducts.setHasFixedSize(true);
            rvProducts.setNestedScrollingEnabled(false);

            GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            rvProducts.setLayoutManager(mLayoutManager);
            rvProducts.setAdapter(selectProductAdapter);
            rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));

            String product_name = getResources().getString(R.string.featureProducts);
            if (!homeRider.productsCarousel.featureProducts.title.isEmpty()) {
                product_name = homeRider.productsCarousel.featureProducts.title;
            }

            setViewAllText(product_name, view);

            tvViewAll.setText(getResources().getString(R.string.view_all));
            tvViewAll.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.FEATURE, true);
                startActivity(intent);
            });
            homeBinding.llmenusTwo.addView(view);
        }
    }

    public void AddTopRatedProducts() {
        if (homeRider.productsCarousel.topRatedProducts != null && homeRider.productsCarousel.topRatedProducts.status.equals("enable") && homeRider.productsCarousel.topRatedProducts.products.size() > 0) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dynamic_mostpopuler, null);
            TextView tvViewAll = view.findViewById(R.id.tvViewAll);
            RecyclerView rvProducts = view.findViewById(R.id.rvProducts);

            topRatedProductAdapter = new TopRatedProductAdapter(HomeActivity.this);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rvProducts.setLayoutManager(mLayoutManager);
            ViewCompat.setNestedScrollingEnabled(rvProducts, false);
            rvProducts.setNestedScrollingEnabled(false);
            rvProducts.setHasFixedSize(true);
            rvProducts.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            rvProducts.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize
            rvProducts.setAdapter(topRatedProductAdapter);
            topRatedProductAdapter.addAll(homeRider.productsCarousel.topRatedProducts.products);

            String product_name = getResources().getString(R.string.featureProducts);
            if (!homeRider.productsCarousel.topRatedProducts.title.isEmpty()) {
                product_name = homeRider.productsCarousel.topRatedProducts.title;
            }

            setViewAllText(product_name, view);

            tvViewAll.setText(getResources().getString(R.string.view_all));
            tvViewAll.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.POSITION, 1);
                intent.putExtra(RequestParamUtils.ORDER_BY, "rating");
                startActivity(intent);
            });
            homeBinding.llMenus.addView(view);
        }
    }

    private void AddPopularProducts() {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dynamic_mostpopuler, null);
        TextView tvViewAll = view.findViewById(R.id.tvViewAll);
        RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
        rvProducts.setHasFixedSize(true);
        rvProducts.setNestedScrollingEnabled(false);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(mLayoutManager);
        tvViewAll.setText(getResources().getString(R.string.view_all));
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        tvViewAll.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        if (homeRider.productsCarousel != null) {
            if (homeRider.productsCarousel.popularProducts != null &&
                    homeRider.productsCarousel.popularProducts.status != null &&
                    homeRider.productsCarousel.popularProducts.status.equals("enable") &&
                    homeRider.productsCarousel.popularProducts.products.size() > 0) {

                mAdapter = new DynamicItemAdapter(HomeActivity.this);
                rvProducts.setAdapter(mAdapter);
                mAdapter.addAll(homeRider.productsCarousel.popularProducts.products);

                String product_name = getResources().getString(R.string.most_popular_poroducts);
                if (!homeRider.productsCarousel.popularProducts.title.isEmpty()) {
                    product_name = homeRider.productsCarousel.popularProducts.title;
                }

                setViewAllText(product_name, view);

                tvViewAll.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                    intent.putExtra(RequestParamUtils.ORDER_BY, RequestParamUtils.popularity);
                    intent.putExtra(RequestParamUtils.POSITION, 2);
                    intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
                    startActivity(intent);
                });
                homeBinding.llMenus.addView(view);
            }
        } else {
            if (homeRider.popularProducts != null && homeRider.popularProducts.size() > 0) {
                mAdapter = new DynamicItemAdapter(HomeActivity.this);
                rvProducts.setAdapter(mAdapter);
                mAdapter.addAll(homeRider.popularProducts);
                String product_name = getResources().getString(R.string.featureProducts);
                setViewAllText(product_name, view);

                tvViewAll.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                    intent.putExtra(RequestParamUtils.ORDER_BY, RequestParamUtils.popularity);
                    intent.putExtra(RequestParamUtils.POSITION, 2);
                    intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
                    startActivity(intent);
                });
                homeBinding.llMenus.addView(view);
            }
        }
    }

    private void AddCustomSection() {
        Log.e(TAG, "AddCustomSection: ");
        if (homeRider.product_banners_cat_value != null && homeRider.product_banners_cat_value.equals("enable")) {
            if (homeRider.product_banners_title != null && homeRider.product_banners_title.length() > 0) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dynamic_mostpopuler, null);
                TextView tvViewAll = view.findViewById(R.id.tvViewAll);
                RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
                rvProducts.setHasFixedSize(true);
                rvProducts.setNestedScrollingEnabled(false);
                GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
                rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
                rvProducts.setLayoutManager(mLayoutManager);
                tvViewAll.setVisibility(View.GONE);

                if (homeRider.custom_section != null && homeRider.custom_section.size() > 0) {
                    selectedItemAdapter = new SelectedItemAdapter(HomeActivity.this);
                    rvProducts.setAdapter(selectedItemAdapter);
                    selectedItemAdapter.addAll(homeRider.custom_section);
                    selectedItemAdapter.notifyDataSetChanged();
                    Log.e(TAG, "AddCustomSection:if " + new Gson().toJson(homeRider.custom_section));
                    getResources().getString(R.string.selected_poroducts);
                    String product_name;
                    if (!homeRider.product_banners_title.isEmpty()) {
                        product_name = homeRider.product_banners_title;
                    } else {
                        product_name = getResources().getString(R.string.selected_poroducts);
                    }
                    setViewAllText(product_name, view);
                }
                homeBinding.llMenus.setVisibility(View.VISIBLE);
                Log.e(TAG, "AddCustomSection:addview ");
                homeBinding.llMenus.addView(view);
            }
        }
    }

    private void setViewAllText(String product_name, View view) {
        TextView tvProductName_one = view.findViewById(R.id.tvProductName_one);
        TextView tvProductName_two = view.findViewById(R.id.tvProductName_two);
        if (product_name.contains(" ")) {
            String[] array = product_name.split(" ");
            if (array.length > 0) {

                String secondName = array[1];
                for (int i = 2; i < array.length; i++) {
                    secondName = String.format("%s %s", secondName, array[i]);
                }

                tvProductName_one.setText(array[0].toUpperCase());
                tvProductName_two.setText(secondName.toUpperCase());
            } else {
                tvProductName_one.setText(product_name);
            }
        } else {
            tvProductName_one.setText(product_name);
        }
        tvProductName_one.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvProductName_two.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    private void AddSpecialDealProducts() {
        final SpecialOfferAdapter mAdapter;
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dynamic_spacialoffer, null);
        LinearLayout llSpecialOffer = view.findViewById(R.id.llSpecialOffer);
        ImageView ivTimer = view.findViewById(R.id.ivTimer);
        TextView tvSaleText = view.findViewById(R.id.tvSaleText);
        TextView tvHour = view.findViewById(R.id.tvHour);
        TextView tvMinute = view.findViewById(R.id.tvMinute);
        TextView tvSecond = view.findViewById(R.id.tvSecond);
        TextView colonOne = view.findViewById(R.id.colonOne);
        TextView colonTwo = view.findViewById(R.id.colonTwo);

        TextView tvViewAllSpecialDeal = view.findViewById(R.id.tvViewAllSpecialDeal);
        tvViewAllSpecialDeal.setText(getResources().getString(R.string.view_all));
        tvSaleText.setText(getResources().getString(R.string.end_of_the_sale));
        Drawable unwrappedDrawable = tvHour.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        unwrappedDrawable = tvMinute.getBackground();
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        unwrappedDrawable = tvSecond.getBackground();
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        tvSaleText.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        colonOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        colonTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvViewAllSpecialDeal.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        RecyclerView rvSpecialOffer = view.findViewById(R.id.rvSpecialOffer);

        pulseAnimation(ivTimer);


        mAdapter = new SpecialOfferAdapter(HomeActivity.this, HomeActivity.this);
        rvSpecialOffer.setHasFixedSize(true);
        rvSpecialOffer.setNestedScrollingEnabled(false);
//        GridLayoutManager mLayoutManager = new GridLayoutManager(HomeActivity.this, 2, GridLayoutManager.HORIZONTAL, false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        rvSpecialOffer.setLayoutManager(mLayoutManager);
        rvSpecialOffer.setAdapter(mAdapter);
        homeBinding.rvRecentOffer.addItemDecoration(new EqualSpacingItemDecoration(dpToPx(10), EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize

//        rvSpecialOffer.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));

        ivTimer.setImageResource(R.drawable.ic_watch);


        ivTimer.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        if (homeRider.productsCarousel != null) {
            if (homeRider.productsCarousel.specialDealProducts.products != null && homeRider.productsCarousel.specialDealProducts.status.equals("enable") && homeRider.productsCarousel.specialDealProducts.products.size() > 0) {
                mAdapter.addAll(homeRider.productsCarousel.specialDealProducts.products);
                if (homeRider.productsCarousel.specialDealProducts.title.length() > 0) {
                    setViewAllText(homeRider.productsCarousel.specialDealProducts.title, view);
                } else {
                    setViewAllText(getResources().getString(R.string.special_offer), view);
                }
                if (homeRider.productsCarousel.specialDealProducts.products.size() > 0) {
                    tvHour.setText(homeRider.productsCarousel.specialDealProducts.products.get(0).dealLife.hours);
                    tvMinute.setText(homeRider.productsCarousel.specialDealProducts.products.get(0).dealLife.minutes);
                    tvSecond.setText(homeRider.productsCarousel.specialDealProducts.products.get(0).dealLife.seconds);
                }
                setTimer(tvHour, tvMinute, tvSecond, llSpecialOffer);
                tvViewAllSpecialDeal.setOnClickListener(v -> {
                    StringBuilder productId = new StringBuilder();
                    for (int i = 0; i < mAdapter.getList().size(); i++) {
                        if (productId.toString().equals("")) {
                            productId = new StringBuilder(mAdapter.getList().get(i).id);
                        } else {
                            productId.append(",").append(mAdapter.getList().get(i).id);
                        }
                    }
                    Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                    intent.putExtra(RequestParamUtils.DEAL_OF_DAY, productId.toString());
                    startActivity(intent);
                });
                homeBinding.llMenus.addView(view);
            }
        } else {
            if (homeRider.scheduledSaleProducts.products != null && homeRider.scheduledSaleProducts.products.size() > 0) {
                mAdapter.addAll(homeRider.scheduledSaleProducts.products);
                setViewAllText(getResources().getString(R.string.special_offer), view);
                if (homeRider.scheduledSaleProducts.products.size() > 0) {
                    tvHour.setText(homeRider.scheduledSaleProducts.products.get(0).dealLife.hours);
                    tvMinute.setText(homeRider.scheduledSaleProducts.products.get(0).dealLife.minutes);
                    tvSecond.setText(homeRider.scheduledSaleProducts.products.get(0).dealLife.seconds);
                }
                setTimer(tvHour, tvMinute, tvSecond, llSpecialOffer);

                tvViewAllSpecialDeal.setOnClickListener(v -> {
                    StringBuilder productId = new StringBuilder();
                    for (int i = 0; i < mAdapter.getList().size(); i++) {
                        if (productId.toString().equals("")) {
                            productId = new StringBuilder(mAdapter.getList().get(i).id);
                        } else {
                            productId.append(",").append(mAdapter.getList().get(i).id);
                        }
                    }
                    Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
                    intent.putExtra(RequestParamUtils.DEAL_OF_DAY, productId.toString());
                    startActivity(intent);
                });
                homeBinding.llMenus.addView(view);
            }
        }
    }

    public void setCurrency(String response) {
        if (Constant.IS_CURRENCY_SWITCHER_ACTIVE) {
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONObject currency_switcher = jsonObj.getJSONObject(RequestParamUtils.currencySwitcher);
                Constant.CurrencyList = new ArrayList<>();
                JSONArray nameArray = currency_switcher.names();  //<<< get all keys in JSONArray
                if (nameArray != null) {
                    for (int i = 0; i < nameArray.length(); i++) {
                        JSONObject c = currency_switcher.getJSONObject(nameArray.get(i).toString());
                        String name = c.getString(RequestParamUtils.name);
                        String symbol = c.getString(RequestParamUtils.symbol);
                        JSONObject obj = new JSONObject();
                        obj.put(RequestParamUtils.NAME, name);
                        obj.put(RequestParamUtils.SYMBOL, symbol);
                        // adding contact to contact list
                        Constant.CurrencyList.add(String.valueOf(obj));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Snackbar.make(homeBinding.llMain, getResources().getString(R.string.exitformapp), Snackbar.LENGTH_LONG).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}



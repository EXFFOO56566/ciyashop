package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.Ciyashop;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.HomeTopCategoryAdapter;
import com.example.ciyashop.adapter.InfiniteScrollAdapter;
import com.example.ciyashop.adapter.NavigationDrawerAdapter;
import com.example.ciyashop.customview.CustomLinearLayoutManager;
import com.example.ciyashop.customview.GridSpacingItemDecoration;
import com.example.ciyashop.databinding.ActivityInfiniteScrollBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
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

public class InfiniteScrollActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityInfiniteScrollBinding binding;

    //Todo: Global Parameter
    int CurrentPage = 1;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    ArrayList<CategoryList> viewProductList = new ArrayList<>();

    Home homeRider;
    List<CategoryList> categoryLists = new ArrayList<>();
    Boolean setNoItemFound = false;

    Boolean isSaleCallOver = false;
    private InfiniteScrollAdapter infiniteScrollAdapter;
    private HomeTopCategoryAdapter homeTopCategoryAdapter;
    private boolean loading = true;
    private TextView[] dots;
    private View listHeaderView;
    private boolean isHead = false;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private TextView tvName;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DatabaseHelper databaseHelper;
    private final boolean isAutoScroll = false;
    private final boolean isSpecialDeal = false;
    private long mBackPressed;
    private Handler handler;
    private int page = 1;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInfiniteScrollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Config.IS_RTL = getPreferences().getBoolean(Constant.RTL, false);
        setHomecolorTheme(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR));
        setScreenLayoutDirection();
        binding.ivBack.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_drawer, null));

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
        getHomeData();
        initDrawer();
        settvImage();
        showNotification();
        showCart();
        swipeView();
        setCategoryList();
        setToolbarTheme();
        settvImage();
        setHomeCategoryData();
    }

    public void setHomeCategoryData() {
        homeTopCategoryAdapter = new HomeTopCategoryAdapter(this, this);
        CustomLinearLayoutManager mLayoutManager = new CustomLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvTopCategory.setLayoutManager(mLayoutManager);
        binding.rvTopCategory.setAdapter(homeTopCategoryAdapter);
        binding.rvTopCategory.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(binding.rvTopCategory, false);
        binding.rvTopCategory.setHasFixedSize(true);
        binding.rvTopCategory.setItemViewCacheSize(20);
        binding.rvTopCategory.setDrawingCacheEnabled(true);
        binding.rvTopCategory.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getHomeData() {
        if (Utils.isInternetConnected(this)) {
            //showProgress("");
            showProgress("");
            try {
                PostApi postApi = new PostApi(this, RequestParamUtils.getHomeData, this, getlanuage());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.appVersion, URLS.version);
                postApi.callPostApi(new URLS().HOME_SCROLLING + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Home", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

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
        binding.ivBack.setOnClickListener(view -> {
            if (Config.IS_RTL) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(InfiniteScrollActivity.this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        binding.drawerListView.setOnItemClickListener((parent, view, position, id) -> selectItemFragment(position - 1));
        setBottomBar("home", binding.svHome);
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
            Intent notificationIntent = new Intent(InfiniteScrollActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        } else if (name.equals(getResources().getString(R.string.my_reward))) {
            Intent rewardIntent = new Intent(InfiniteScrollActivity.this, RewardsActivity.class);
            startActivity(rewardIntent);
        } else if (name.equals(getResources().getString(R.string.my_cart))) {
            Intent cartIntent = new Intent(InfiniteScrollActivity.this, CartActivity.class);
            startActivity(cartIntent);
        } else if (name.equals(getResources().getString(R.string.my_wish_list))) {
            Intent wishListIntent = new Intent(InfiniteScrollActivity.this, WishListActivity.class);
            startActivity(wishListIntent);
        } else if (name.equals(getResources().getString(R.string.my_account))) {
            Intent accountIntent = new Intent(InfiniteScrollActivity.this, AccountActivity.class);
            startActivity(accountIntent);
        } else if (name.equals(getResources().getString(R.string.my_orders))) {
            Intent myOrderIntent = new Intent(InfiniteScrollActivity.this, MyOrderActivity.class);
            startActivity(myOrderIntent);
        } else if (name.equals(getResources().getString(R.string.blog))) {
            Intent myOrderIntent = new Intent(InfiniteScrollActivity.this, BlogActivity.class);
            startActivity(myOrderIntent);
        } else if (name.equals(getResources().getString(R.string.find_store))) {
            Intent myOrderIntent = new Intent(InfiniteScrollActivity.this, StoreFinderActivity.class);
            startActivity(myOrderIntent);
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        dismissProgress();
        switch (methodName) {
            case RequestParamUtils.getHomeData:
                if (response != null && response.length() > 0) {
                    binding.swipeContainer.setRefreshing(false);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        //Convert json response into gson and made model class
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        homeRider = gson.fromJson(
                                jsonObject.toString(), new TypeToken<Home>() {
                                }.getType());
                        Constant.IS_ADD_TO_CART_ACTIVE = jsonObject.has("pgs_woo_api_add_to_cart_option") && jsonObject.getString("pgs_woo_api_add_to_cart_option").equals("enable");
                        if (jsonObject.has("pgs_woo_api_catalog_mode_option") && jsonObject.getString("pgs_woo_api_catalog_mode_option").equals("enable")) {
                            Config.IS_CATALOG_MODE_OPTION = true;
                            showCart();
                        } else {
                            Config.IS_CATALOG_MODE_OPTION = false;
                        }

                        if (jsonObject.has("pgs_woo_api_web_view_pages")) {
                            Constant.WEBVIEWPAGES = new ArrayList<>();
                            if (homeRider.webViewPages != null && homeRider.webViewPages.size() > 0) {
                                Constant.WEBVIEWPAGES.addAll(homeRider.webViewPages);
                            }
                        }
                        checkReview(jsonObject);
                        runOnUiThread(() -> {
                            binding.llMain.setVisibility(View.VISIBLE);
                            if (homeRider != null) {
                                if (homeRider.isAppValidation != null) {
                                    new Ciyashop(InfiniteScrollActivity.this).setFlag(homeRider.isAppValidation, false);
                                }
                                //set all constant value from the response
                                setConstantValue();

                                //set theme color from the response
                                setThemeIconColor();

                                //set  all color from the response into preferences
                                setColorPreferences(homeRider.appColor.primaryColor, homeRider.appColor.secondaryColor, homeRider.appColor.headerColor);

                                //set color into toolbar of home activity
                                setHomecolorTheme(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR));

                                //set lungage into local
                                setLocale(homeRider.siteLanguage);

                                //CheckOut URLs get
                                if (homeRider.checkoutRedirectUrl != null && homeRider.checkoutRedirectUrl.size() > 0) {
                                    setCheckoutURL(homeRider.checkoutRedirectUrl);
                                }

                                //set bottomBar
                                setBottomBar("home", binding.svHome);

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
                            SharedPreferences.Editor editor = getPreferences().edit();
                            editor.putString(Constant.APPLOGO, homeRider.appLogo);
                            editor.putString(Constant.APPLOGO_LIGHT, homeRider.appLogoLight);
                            editor.apply();
                            settvImage();
                        });
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                    }
                    page = 1;
                    setNoItemFound = false;
                    infiniteScrollAdapter.clearList();
                    getCategoryListData(true);
                }
                break;
            case RequestParamUtils.getCategoryListData:
                if (response != null && response.length() > 0) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        categoryLists = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            CategoryList categoryListRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<CategoryList>() {
                                    }.getType());
                            categoryLists.add(categoryListRider);
                        }
                        infiniteScrollAdapter.addAll(categoryLists);
                        dismissProgress();
                        //  dismissProgress();
                        loading = true;
                    } catch (Exception e) {
                        //dismissProgress();
                        dismissProgress();
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getString("message").equals(getString(R.string.no_product_found))) {
                                setNoItemFound = true;
                            }
                        } catch (JSONException e1) {
                            Log.e("noProductJSONException", e1.getMessage());
                        }
                    }
                }
                binding.llProgress.setVisibility(View.GONE);
                //dismissProgress();
                dismissProgress();
                break;
            case RequestParamUtils.addWishList:
            case RequestParamUtils.removeWishList:
                dismissProgress();
                break;
        }
    }

    private void setCheckoutURL(List<String> checkoutRedirectUrl) {
        Constant.CheckoutURL.clear();
        Constant.CheckoutURL = new ArrayList<>();
        Constant.CheckoutURL.addAll(checkoutRedirectUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setThemeIconColor() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor(getPreferences().getString(Constant.PRIMARY_COLOR, Constant.PRIMARY_COLOR)));
        gradientDrawable.setCornerRadius(5);
    }

    public void swipeView() {
        binding.swipeContainer.setOnRefreshListener(() -> {
            getHomeData();
            if (infiniteScrollAdapter != null && infiniteScrollAdapter.handler != null) {
                infiniteScrollAdapter.handler.removeCallbacksAndMessages(null);
                infiniteScrollAdapter.handler = null;
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.orange,
                R.color.red,
                R.color.blue
        );
    }

    public void setConstantValue() {
        if (homeRider.pgsAppContactInfo != null) {
            if (homeRider.pgsAppContactInfo.addressLine1 != null) {
                Constant.ADDRESS_LINE1 = homeRider.pgsAppContactInfo.addressLine1;
            }
            if (homeRider.pgsAppContactInfo.addressLine2 != null) {
                Constant.ADDRESS_LINE2 = homeRider.pgsAppContactInfo.addressLine2;
            }
            if (homeRider.pgsAppContactInfo.email != null) {
                Constant.EMAIL = homeRider.pgsAppContactInfo.email;
            }
            if (homeRider.pgsAppContactInfo.phone != null) {
                Constant.PHONE = homeRider.pgsAppContactInfo.phone;
            }
            if (homeRider.pgsAppContactInfo.whatsappNo != null) {
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
        if (homeRider.isOrderTrackingActive != null) {
            Constant.IS_ORDER_TRACKING_ACTIVE = homeRider.isOrderTrackingActive;
        }
        if (homeRider.isRewardPointsActive != null) {
            Constant.IS_REWARD_POINT_ACTIVE = homeRider.isRewardPointsActive;
        }
        if (homeRider.isWishlistActive != null) {
            Constant.IS_WISH_LIST_ACTIVE = homeRider.isWishlistActive;
        }
        if (homeRider.isWishlistActive != null) {
            Constant.IS_YITH_FEATURED_VIDEO_ACTIVE = homeRider.isYithFeaturedVideoActive;
        }
        if (homeRider.pgsAppSocialLinks != null) {
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
//                Config.IS_RTL =true;
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
        String colorSubString = (primaryColor.substring(primaryColor.lastIndexOf("#") + 1));
        SharedPreferences.Editor editor = getPreferences().edit();

        if (!primaryColor.equals("")) {
            editor.putString(Constant.APP_COLOR, primaryColor);
            editor.putString(Constant.APP_TRANSPARENT, "#aa" + colorSubString);
            editor.putString(Constant.APP_TRANSPARENT_VERY_LIGHT, "#44" + colorSubString);
        }
        if (!secondaryColor.equals("")) {
            editor.putString(Constant.SECOND_COLOR, secondaryColor);
        }
        if (!HeaderColor.equals("")) {
            editor.putString(Constant.HEADER_COLOR, HeaderColor);
        }
        editor.apply();
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
                binding.llTopCategory.setVisibility(View.VISIBLE);


            } else {
                binding.llTopCategory.setVisibility(View.GONE);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        showCart();
        if (Constant.IS_CURRENCY_SET) {
            getHomeData();
            databaseHelper.clearRecentItem();
            databaseHelper.clearCart();
            Constant.IS_CURRENCY_SET = false;
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

    public void setLocale(String lang) {
        if (!getPreferences().getString(RequestParamUtils.LANGUAGE, "").equals("")) {
            lang = getPreferences().getString(RequestParamUtils.LANGUAGE, "");
        }
        String languageToLoad; // your language
        if (lang.contains("-")) {
            String[] array = lang.split("-");
            if (array.length > 0) {
                languageToLoad = array[0];
            } else {
                languageToLoad = lang;
            }
        } else {
            languageToLoad = lang;
        }
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        if (!getPreferences().getString(RequestParamUtils.DEFAULTLANGUAGE, "").equals("") && !getPreferences().getString(RequestParamUtils.DEFAULTLANGUAGE, "").equals(languageToLoad)) {
            recreate();
            getPreferences().edit().putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false).apply();
        }
        if (getPreferences().getString(RequestParamUtils.LANGUAGE, "").isEmpty()) {
            if (!getPreferences().getBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false)) {
                getPreferences().edit().putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, true).apply();
                (InfiniteScrollActivity.this).getPreferences().edit().putString(RequestParamUtils.DEFAULTLANGUAGE, languageToLoad).apply();
                recreate();
            }
        }
        setScreenLayoutDirection();
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Snackbar.make(binding.llMain, getResources().getString(R.string.exitformapp), Snackbar.LENGTH_LONG).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    public void getCategoryListData(boolean isDialogShow) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                //showProgress("");
                showProgress("");
            }
            PostApi postApi = new PostApi(InfiniteScrollActivity.this, RequestParamUtils.getCategoryListData, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.PAGE, page);
                jsonObject.put(RequestParamUtils.PERPAGE, 15);
                jsonObject.put(RequestParamUtils.LODADED, infiniteScrollAdapter.getIds());
                postApi.callPostApi(new URLS().RANDOMLIST + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void setCategoryList() {
        infiniteScrollAdapter = new InfiniteScrollAdapter(this, this);
        final LinearLayoutManager mLayoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        binding.rvViewAllProductList.setLayoutManager(mLayoutManager);
        binding.rvViewAllProductList.setAdapter(infiniteScrollAdapter);
        binding.rvViewAllProductList.setNestedScrollingEnabled(false);
        binding.rvViewAllProductList.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        //pagination on get category data
        binding.svHome.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    //code to fetch more data for endless scrolling
                    if (loading) {
                        if (!setNoItemFound) {
                            loading = false;
                            page = page + 1;
                            Log.e("End ", "Last Item Wow  and page no:- " + page);
                            binding.llProgress.setVisibility(View.VISIBLE);
                            binding.progressWheel.getIndeterminateDrawable().setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                            getCategoryListData(false);
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });
    }

    public void setToolbarTheme() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        String userid = getPreferences().getString(RequestParamUtils.ID, "");
        if (!userid.equals("")) {
            if (value.equals(RequestParamUtils.delete)) {
                removeWishList(true, userid, position + "");
            } else if (value.equals(RequestParamUtils.insert)) {
                addWishList(true, userid, position + "");
            }
        }
    }

    public void addWishList(boolean isDialogShow, String userid, String productId) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }
            PostApi postApi = new PostApi(InfiniteScrollActivity.this, RequestParamUtils.addWishList, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.USER_ID, userid);
                jsonObject.put(RequestParamUtils.PRODUCT_ID, productId);
                postApi.callPostApi(new URLS().ADD_TO_WISHLIST + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void removeWishList(boolean isDialogShow, String userid, String productId) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }

            PostApi postApi = new PostApi(InfiniteScrollActivity.this, RequestParamUtils.removeWishList, this, getlanuage());
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
}
package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.GroupProductAdapter;
import com.example.ciyashop.adapter.ProductColorAdapter;
import com.example.ciyashop.adapter.ProductImageViewPagerAdapter;
import com.example.ciyashop.adapter.ProductVariationAdapter;
import com.example.ciyashop.adapter.RelatedProductAdapter;
import com.example.ciyashop.adapter.ReviewAdapter;
import com.example.ciyashop.customview.EqualSpacingItemDecoration;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.databinding.ActivityProductDetailBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.CheckIsVariationAvailable;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.ProductReview;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.model.WishList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductDetailActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private static final String TAG = ProductDetailActivity.class.getSimpleName();
    public static HashMap<Integer, String> combination = new HashMap<>();
    private static TextViewRegular tvPrice;
    private static TextViewRegular tvPrice1;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    AlertDialog alertDialog;
    List<CategoryList.Image> imageList = new ArrayList<>();
    List<CategoryList> categoryLists = new ArrayList<>();

    private boolean isDialogOpen = false;
    private boolean isDeepLinking = false;
    private TextView[] dots;
    private int[] layouts;
    private ProductImageViewPagerAdapter productImageViewPagerAdapter;
    private int currentPosition;
    private ProductColorAdapter productColorAdapter;
    private RelatedProductAdapter relatedProductAdapter;
    private ReviewAdapter reviewAdapter;
    private GroupProductAdapter groupProductAdapter;
    private ProductVariationAdapter productVariationAdapter;
    private final CategoryList categoryList = Constant.CATEGORYDETAIL;
    private List<Variation> variationList;
    private final int page = 1;
    private DatabaseHelper databaseHelper;
    private CustomToast toast;
    private float fiveRate, fourRate, threeRate, twoRate, oneRate;
    private float avgRatting;
    private int pos;
    private int defaultVariationId;
    private int VariationPage = 1;
    private boolean isFirstLoad = true;
    private RecyclerView rvProductVariation;

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    private ActivityProductDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setMarginToLlMain();
        setClickEvent();
        getIntentData();
        binding.ivWishList.setActivetint(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        binding.ivWishList.setColors(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)), Color.parseColor(getPreferences().getString(Constant.APP_TRANSPARENT, Constant.SECOND_COLOR)));
        setScreenLayoutDirection();
        indexNote(Constant.CATEGORYDETAIL);
        List<CategoryList.Attribute> attributes = new ArrayList<>();
        if (Constant.CATEGORYDETAIL != null && Constant.CATEGORYDETAIL.attributes != null && Constant.CATEGORYDETAIL.attributes.size() > 0) {
            for (int i = 0; i < Constant.CATEGORYDETAIL.attributes.size(); i++) {
                if (Constant.CATEGORYDETAIL.attributes.get(i).variation) {
                    attributes.add(Constant.CATEGORYDETAIL.attributes.get(i));
                }
            }
        }
        categoryList.attributes = attributes;
        Constant.CATEGORYDETAIL = categoryList;
        tvPrice = findViewById(R.id.tvPrice);
        tvPrice1 = findViewById(R.id.tvPrice1);
        databaseHelper = new DatabaseHelper(this);
        String product = new Gson().toJson(categoryList);
        databaseHelper.addTorecentView(product, categoryList.id + "");
        toast = new CustomToast(this);
        if (Constant.IS_WISH_LIST_ACTIVE) {
            binding.ivWishList.setVisibility(View.VISIBLE);
            binding.ivWishList.setChecked(databaseHelper.getWishlistProduct(categoryList.id + ""));
        } else {
            binding.ivWishList.setVisibility(View.INVISIBLE);
            binding.ivWishList.setEnabled(false);
        }
        setData();
        setColorTheme();
        getRelatedProduct();
        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.fromdeeplink)) {
            isDeepLinking = intent.getBooleanExtra(RequestParamUtils.fromdeeplink, true);
        } else {
            isDeepLinking = false;
        }
        binding.nsScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    if (!categoryList.relatedIds.isEmpty()) {
                        if (isFirstLoad) {
                            //getRelatedProduct();
                            isFirstLoad = false;
                            binding.llRelatedItem.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.llRelatedItem.setVisibility(View.GONE);
                    }
                }
            }
        });
        if (Config.WOO_API_DELIVER_PINCODE) {
            binding.etPincode.setHint(Constant.settingOptions.pincodePlaceholderTxt);
            binding.llPincode.setVisibility(View.VISIBLE);
            binding.tvDeliverable.setVisibility(View.VISIBLE);
        } else {
            binding.llPincode.setVisibility(View.GONE);
            binding.tvDeliverable.setVisibility(View.GONE);
        }
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initCollapsingToolbar();
    }

    private void setMarginToLlMain() {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top; // This is the height of the status bar
                binding.crMain.setFitsSystemWindows(true);
                CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, -statusBarHeight, 0, 0);
                layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                binding.llMain.setLayoutParams(layoutParams);
            }
        });
    }

    private boolean hasOnScreenSystemBar() {
        Display display = getWindowManager().getDefaultDisplay();
        int rawDisplayHeight = 0;
        try {
            Method getRawHeight = Display.class.getMethod("getRawHeight");
            rawDisplayHeight = (Integer) getRawHeight.invoke(display);
        } catch (Exception ex) {
            Log.e("TAG", "Exception: " + ex.getMessage());
        }
        /*int UIRequestedHeight = display.getHeight();*/
        int UIRequestedHeight = getResources().getDisplayMetrics().heightPixels;
        return rawDisplayHeight - UIRequestedHeight > 0;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initCollapsingToolbar() {
        binding.collapsingToolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.PRIMARY_COLOR)));
        binding.collapsingToolbar.setTitle(binding.tvProductName.getText().toString());
        binding.collapsingToolbar.setContentScrimColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparent_white));
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.collapsingToolbar.setStatusBarScrimColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));

        Drawable drawable = binding.toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
        }
        AppBarLayout appBarLayout = findViewById(R.id.appbar);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            Drawable drawable12 = binding.toolbar.getNavigationIcon();
            drawable12.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
            mCurrentState = State.IDLE;
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    Drawable drawable1 = binding.toolbar.getNavigationIcon();
                    drawable1.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);

                    binding.toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
            } else if (Math.abs(i) >= appBarLayout1.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    Drawable drawable1 = binding.toolbar.getNavigationIcon();
                    drawable1.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);

                    binding.toolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    }
                    drawable12 = binding.toolbar.getNavigationIcon();
                    drawable12.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                } else {
                    drawable12 = binding.toolbar.getNavigationIcon();
                    drawable12.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
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
            }
        });
    }

    public void variationPopupOrInPage() {
        if (Config.IS_VARIATION_POPUP_SHOW) {
            showDialog();
            if (variationList.size() == 0) {
                binding.llProductVariation.setVisibility(View.GONE);
                binding.llColor.setVisibility(View.GONE);
            } else {
                binding.llProductVariation.setVisibility(View.GONE);
                binding.llColor.setVisibility(View.VISIBLE);
            }
            productColorAdapter.addAllVariationList(variationList, productVariationAdapter);
            productColorAdapter.addAll(categoryList.attributes.get(0).options);
        } else {
            if (variationList.size() == 0) {
                binding.llProductVariation.setVisibility(View.GONE);
                binding.llColor.setVisibility(View.GONE);
            } else {
                binding.llColor.setVisibility(View.GONE);
                binding.llProductVariation.setVisibility(View.VISIBLE);
            }
            setVariation(binding.rvVariation);
            productVariationAdapter.notifyDataSetChanged();
            changePrice();
        }
    }

    //TODO:For deepLinking
    public void getIntentData() {
        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.fromdeeplink)) {
            isDeepLinking = intent.getBooleanExtra(RequestParamUtils.fromdeeplink, true);
        } else {
            isDeepLinking = false;
        }

//        if(Constant.IS_RTL) {
//            tvAvailibilitytext.setText(":" + getResources().getString(R.string.availability));
//        }else {
//            tvAvailibilitytext.setText( getResources().getString(R.string.availability)+":");
//        }
    }

    private void setColorTheme() {
        binding.tvMoreQuickOverview.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvMoreDetail.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvFive.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvFour.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvThree.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvSellerInfoTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
//        binding.tvBuyNow.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        String htmlPrice;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlPrice = String.valueOf(Html.fromHtml(categoryList.priceHtml + "", Html.FROM_HTML_MODE_COMPACT));
        } else {
            htmlPrice = (Html.fromHtml(categoryList.priceHtml) + "");
        }
        if (Config.IS_CATALOG_MODE_OPTION) {
            binding.flBuyNow.setVisibility(View.GONE);
            binding.flAddToCart.setVisibility(View.GONE);
        } else if (htmlPrice.equals("") && categoryList.price.equals("")) {
            binding.flBuyNow.setVisibility(View.GONE);
            binding.flAddToCart.setVisibility(View.GONE);
        } else {
            binding.flBuyNow.setVisibility(View.VISIBLE);
            binding.flAddToCart.setVisibility(View.VISIBLE);
        }

//        GradientDrawable gd = (GradientDrawable) llratting.getBackground();
//        gd.setStroke(5, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivDetailMore.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivQuickOverViewMore.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvSellerMore.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivMoreSeller.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvReward.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvInfo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvCheck.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvDeliveryOptions.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvQuickOverViewTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvProductDetailTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvRatingTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvProductNameOne.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvProductNameTwo.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        // tvContactSeller.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.tvContactSeller.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        unwrappedDrawable = binding.tvReward.getBackground();
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.APP_TRANSPARENT_VERY_LIGHT, Constant.SECONDARY_COLOR))));

        Drawable background = binding.tvRateReview.getBackground();
        Drawable drawables = DrawableCompat.wrap(background);
        DrawableCompat.setTint(drawables, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));


        Drawable background1 = binding.flBuyNow.getBackground();
        Drawable drawables1 = DrawableCompat.wrap(background1);
        DrawableCompat.setTint(drawables1, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));


        Drawable unwrappedDrawables = binding.tvViewStore.getBackground();
        Drawable wrappedDrawables = DrawableCompat.wrap(unwrappedDrawables);
        DrawableCompat.setTint(wrappedDrawables, getResources().getColor(R.color.black));
//        tvViewStore.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void verificationReview(String userId) {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            try {
                PostApi postApi = new PostApi(this, RequestParamUtils.VERIFY_REVIEW, this, getlanuage());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.USER_ID, userId);
                jsonObject.put(RequestParamUtils.PRODUCT_ID, categoryList.id);
//                Log.e("?lang=fr: ", getPreferences().getString(RequestParamUtils.DefaultLanguage, "") );
                postApi.callPostApi(new URLS().VERIFY_REVIEW + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("verificationReview", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    private void setData() {
//        GlideDrawableImageViewTarget ivSmily = new GlideDrawableImageViewTarget(ivProgress);
        Glide.with(this).load(R.raw.loader).into(binding.ivProgress);
        if (categoryList.additionInfoHtml != null && !categoryList.additionInfoHtml.equals("")) {
            binding.wvInfo.loadData(categoryList.additionInfoHtml, "text/html; charset=UTF-8", null);
        } else {
            binding.llInfo.setVisibility(View.GONE);
        }

        if (categoryList.rewardMessage != null && !categoryList.rewardMessage.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvReward.setText(Html.fromHtml(categoryList.rewardMessage, Html.FROM_HTML_MODE_COMPACT));
            } else {
                binding.tvReward.setText(Html.fromHtml(categoryList.rewardMessage));
            }
            binding.tvReward.setVisibility(View.VISIBLE);
        } else {
            binding.tvReward.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            tvProductName.setText(categoryList.name + "");
            binding.tvProductName.setText(Html.fromHtml(categoryList.name + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
//            tvProductName.setText(categoryList.name + "");
            binding.tvProductName.setText(Html.fromHtml(categoryList.name + ""));
        }

//        if (price == null) {
//            price = categoryList.priceHtml;
//        }

//        Utils.setPrice(categoryList.priceHtml,tvPrice,tvPrice1,this);

        setPrice(categoryList.priceHtml);

//        if (categoryList != null && categoryList.averageRating != null && categoryList.averageRating.equals("")) {
//            tvRatting.setText("0");
//        } else {
//            if (categoryList.averageRating != null && !categoryList.averageRating.equals("0")) {
//                tvRatting.setText(Constant.setDecimalOne(Double.parseDouble(categoryList.averageRating)));
//            } else {
//                tvRatting.setText("0");
//            }
//        }

        if (categoryList.inStock) {
            binding.tvAvailibility.setText(R.string.in_stock);
            binding.tvAvailibility.setTextColor(getResources().getColor(R.color.green));
            binding.llAddToCart.setVisibility(View.VISIBLE);
            binding.llOutOfStock.setVisibility(View.GONE);
        } else {
            binding.tvAvailibility.setText(R.string.out_of_stock);
            binding.tvAvailibility.setTextColor(Color.RED);
            binding.tvBuyNow.setAlpha((float) 0.6);
            binding.tvBuyNow.setClickable(false);
            binding.tvCart.setAlpha((float) 0.6);
            binding.tvCart.setClickable(false);
            binding.llAddToCart.setVisibility(View.GONE);
            binding.llOutOfStock.setVisibility(View.VISIBLE);
        }

        setSellerInformation();
        setProductDescription();
        imageList = categoryList.images;

        if (categoryList.attributes.size() > 0) {
            setColorData();
            String text = categoryList.attributes.get(0).name.substring(0, 1).toUpperCase() + categoryList.attributes.get(0).name.substring(1).toLowerCase();
            String strColor = categoryList.attributes.get(0).options.size() + " " + text;
            binding.tvColor.setText(strColor);
        }

        if (!categoryList.shortDescription.equals("")) {
            binding.llQuickOverView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvDescription.setText(Html.fromHtml(categoryList.shortDescription, Html.FROM_HTML_MODE_COMPACT));
            } else {
                binding.tvDescription.setText(Html.fromHtml(categoryList.shortDescription));
            }
        } else {
            binding.llQuickOverView.setVisibility(View.GONE);
        }
//        showBackButton();
//        showCart();
//        settvImage();
        setReviewData();
        setListRecycleView();
//      hideSearchNotification();
        switch (categoryList.type) {
            case RequestParamUtils.variable:
                getVariation();
                break;
            case RequestParamUtils.simple:
                if (categoryList.featuredVideo != null && Constant.IS_YITH_FEATURED_VIDEO_ACTIVE
                        && categoryList.featuredVideo.imageUrl != null && categoryList.featuredVideo.videoId != null) {
                    CategoryList.Image images = new CategoryList().getImageInstance();
                    images.src = categoryList.featuredVideo.imageUrl;
                    images.url = categoryList.featuredVideo.url;
                    images.type = "Video";
                    categoryList.images.add(0, images);
                }

                if (databaseHelper.getProductFromCartById(categoryList.id + "") != null) {
                    binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                } else {
                    binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
                }
                getReview();
                break;
            case RequestParamUtils.grouped:
                if (categoryList.featuredVideo != null && categoryList.featuredVideo.url != null &&
                        categoryList.featuredVideo.imageUrl != null && Constant.IS_YITH_FEATURED_VIDEO_ACTIVE) {
                    CategoryList.Image images = new CategoryList().getImageInstance();
                    images.src = categoryList.featuredVideo.imageUrl;
                    images.url = categoryList.featuredVideo.url;
                    images.type = "Video";
                    categoryList.images.add(0, images);
                }

                if (databaseHelper.getProductFromCartById(categoryList.id + "") != null) {
                    binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                } else {
                    binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
                }
                StringBuilder groupId = new StringBuilder();
                for (int i = 0; i < categoryList.groupedProducts.size(); i++) {
                    if (groupId.toString().equals("")) {
                        groupId.append(categoryList.groupedProducts.get(i));
                    } else {
                        groupId.append(",").append(categoryList.groupedProducts.get(i));
                    }
                }
                getGroupProducts(groupId.toString());
                setRvGroupProduct();
                break;
            case RequestParamUtils.external:
                finish();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(categoryList.externalUrl));
                startActivity(browserIntent);
                break;
        }
        setVpProductImages();

        if (getPreferences().getBoolean(RequestParamUtils.Enable_Review, false)) {
            binding.tvRateReview.setVisibility(View.VISIBLE);
        } else {
            binding.tvRateReview.setVisibility(View.GONE);
        }
    }

    public void setPrice(String price) {
        if (price.isEmpty()) {
            price = categoryList.priceHtml;
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvPrice.setText(Html.fromHtml(price, Html.FROM_HTML_MODE_COMPACT));
        } else {
            String strPrice = Html.fromHtml(price + " ") + "";
            tvPrice.setText(strPrice);
        }
        tvPrice.setTextSize(15);
        setPrice(tvPrice, tvPrice1, price);

        if (!categoryList.type.equals("variable")) {
            showDiscount(binding.tvDiscount, categoryList.salePrice, categoryList.regularPrice);
        } else {
            showDiscount(binding.tvDiscount, CheckIsVariationAvailable.salePrice + "", CheckIsVariationAvailable.regularPrice + "");
        }
    }

    private void setProductDescription() {
        if (categoryList.description != null && !categoryList.description.equals("")) {
            binding.llProductDescription.setVisibility(View.VISIBLE);
            binding.tvProductDescription.setHtml(categoryList.description,
                    new HtmlHttpImageGetter(binding.tvProductDescription));
        }
    }

    private void setSellerInformation() {
        if (categoryList.sellerInfo != null && categoryList.sellerInfo.isSeller) {
            binding.llIsSeller.setVisibility(View.VISIBLE);
            if (categoryList.sellerInfo.contactSeller) {
                binding.tvContactSeller.setClickable(true);
                binding.tvContactSeller.setVisibility(View.VISIBLE);
            } else {
                binding.tvContactSeller.setClickable(false);
                binding.tvContactSeller.setVisibility(View.INVISIBLE);
            }
        } else {
            binding.llIsSeller.setVisibility(View.GONE);
        }
        if (categoryList.sellerInfo != null) {
            if (categoryList.sellerInfo.soldBy) {
                binding.llSoldBy.setVisibility(View.VISIBLE);
                binding.tvSellerName.setText(categoryList.sellerInfo.storeName == null ? "" : categoryList.sellerInfo.storeName);
            } else {
                binding.llSoldBy.setVisibility(View.GONE);
            }

            if (categoryList.sellerInfo.storeTnc == null || categoryList.sellerInfo.storeTnc.equals("")) {
                binding.tvSellerMore.setVisibility(View.GONE);
                binding.tvSellerContent.setVisibility(View.GONE);
                binding.ivMoreSeller.setVisibility(View.GONE);
            } else {
                binding.tvSellerMore.setVisibility(View.VISIBLE);
                binding.tvSellerContent.setVisibility(View.VISIBLE);
                binding.ivMoreSeller.setVisibility(View.VISIBLE);
                binding.tvSellerContent.setHtml(categoryList.sellerInfo.storeTnc,
                        new HtmlHttpImageGetter(binding.tvSellerContent));
            }
        }
    }

    public void getVariation() {
        if (VariationPage == 1) {
            variationList = new ArrayList<>();
        }
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            GetApi getApi = new GetApi(this, RequestParamUtils.getVariation, this, getlanuage());

            new URLS();
            String strURl = URLS.WOO_MAIN_URL + new URLS().WOO_PRODUCT_URL + "/" + categoryList.id + "/" + new URLS().WOO_VARIATION;

            if (getPreferences().getString(RequestParamUtils.CurrencyText, "").equals("")) {
                strURl = strURl + "?page=" + VariationPage;
            } else {
                strURl = strURl + getPreferences().getString(RequestParamUtils.CurrencyText, "") + "&page=" + VariationPage;
            }
            getApi.callGetApi(strURl);
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void getReview() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            GetApi getApi = new GetApi(this, RequestParamUtils.getReview, this, getlanuage());
            getApi.setisDialogShow(false);
            new URLS();
            getApi.callGetApi(URLS.WOO_MAIN_URL + new URLS().WOO_PRODUCT_URL + "/" + categoryList.id + "/" + new URLS().WOO_REVIEWS);
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void getRelatedProduct() {
        if (!categoryList.relatedIds.isEmpty()) {
            if (Utils.isInternetConnected(this)) {
                PostApi postApi = new PostApi(ProductDetailActivity.this, RequestParamUtils.relatedProduct, this, getlanuage());
                Log.e("RelatedProduct ", "get RelatedProduct");
                try {
                    JSONObject jsonObject = new JSONObject();
                    StringBuilder relatedId = new StringBuilder();
                    for (int i = 0; i < categoryList.relatedIds.size(); i++) {
                        if (relatedId.toString().equals("")) {
                            relatedId.append(categoryList.relatedIds.get(i));
                        } else {
                            relatedId.append(",").append(categoryList.relatedIds.get(i));
                        }
                    }
                    jsonObject.put(RequestParamUtils.INCLUDE, relatedId.toString());
                    jsonObject.put(RequestParamUtils.PAGE, page);
                    postApi.callPostApi(new URLS().PRODUCT_URL + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
                } catch (Exception e) {
                    Log.e("Json Exception", e.getMessage());
                }
            } else {
                Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
            }
        } else {
            binding.llRelatedItem.setVisibility(View.GONE);
        }
    }

    public void getGroupProducts(String groupId) {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(ProductDetailActivity.this, RequestParamUtils.getGroupProducts, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.INCLUDE, groupId);
                jsonObject.put(RequestParamUtils.PAGE, page);
                postApi.callPostApi(new URLS().PRODUCT_URL, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void setListRecycleView() {
        relatedProductAdapter = new RelatedProductAdapter(this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvRelatedProduct.setLayoutManager(mLayoutManager);
        binding.rvRelatedProduct.setAdapter(relatedProductAdapter);
        ViewCompat.setNestedScrollingEnabled(binding.rvRelatedProduct, false);
        binding.rvRelatedProduct.setHasFixedSize(true);
        relatedProductAdapter.notifyDataSetChanged();
        binding.rvRelatedProduct.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        binding.rvRelatedProduct.addItemDecoration(new EqualSpacingItemDecoration(10, EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize
    }

    @Override
    public void onResponse(String response, String methodName) {
        dismissProgress();
        switch (methodName) {
            case RequestParamUtils.getVariation:
                JSONArray jsonArray = null;
                if (response != null && response.length() > 0) {
                    try {
                        jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            Variation variationRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<Variation>() {
                                    }.getType());
                            variationList.add(variationRider);
                        }
                        if (jsonArray.length() == 10) {
                            //more product available
                            VariationPage++;
                            getVariation();
                        } else {
                            variationPopupOrInPage();
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                    }
                    if (jsonArray == null || jsonArray.length() != 10) {
                        getReview();
                        getDefaultVariationId();
                    }
                }
                break;
            case RequestParamUtils.getGroupProducts:
                if (response != null && response.length() > 0) {
                    try {
                        jsonArray = new JSONArray(response);
                        List<CategoryList> categoryLists = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            CategoryList categoryListRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<CategoryList>() {
                                    }.getType());
                            categoryLists.add(categoryListRider);
//                        if (categoryListRider.type.equals("simple")) {
//                            isGroupProductAddToCart(categoryListRider.id + "");
//                        }
                        }
                        isGroupProductAddToCart();
                        groupProductAdapter.addAll(categoryLists);
                        if (categoryLists.size() > 0) {
                            binding.rvGroupProduct.setVisibility(View.VISIBLE);
                        } else {
                            binding.rvGroupProduct.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                    }
                }
                getReview();
                break;
            case RequestParamUtils.getReview:
                if (response != null && response.length() > 0) {
                    try {
                        jsonArray = new JSONArray(response);
                        List<ProductReview> reviewList = new ArrayList<>();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String jsonResponse = jsonArray.get(i).toString();
                                ProductReview productReviewRider = new Gson().fromJson(
                                        jsonResponse, new TypeToken<ProductReview>() {
                                        }.getType());
                                reviewList.add(productReviewRider);

                                if (productReviewRider.rating == 5) {
                                    fiveRate = fiveRate + 1;
                                } else if (productReviewRider.rating == 4) {
                                    fourRate = fourRate + 1;
                                } else if (productReviewRider.rating == 3) {
                                    threeRate = threeRate + 1;
                                } else if (productReviewRider.rating == 2) {
                                    twoRate = twoRate + 1;
                                } else if (productReviewRider.rating == 1) {
                                    oneRate = oneRate + 1;
                                }
                            }
                            setRate(reviewList.size());
                        }
                        if (reviewList.size() > 3) {
                            List<ProductReview> reviewLists = new ArrayList<>();
                            for (int i = 0; i < 3; i++) {
                                reviewLists.add(reviewList.get(i));
                            }
                            reviewAdapter.addAll(reviewLists);
                        } else {
                            reviewAdapter.addAll(reviewList);
                        }

                        if (reviewList.size() < 3) {
                            binding.tvCheckAllReview.setVisibility(View.GONE);
                            binding.ivReview.setVisibility(View.GONE);
                        } else {
                            binding.tvCheckAllReview.setVisibility(View.VISIBLE);
                            binding.ivReview.setVisibility(View.VISIBLE);
                        }
                        dismissProgress();
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                    }
                } else {
                    binding.tvCheckAllReview.setVisibility(View.GONE);
                    binding.ivReview.setVisibility(View.GONE);
                }
                break;
            case RequestParamUtils.relatedProduct:
                if (response != null && response.length() > 0) {
                    categoryLists.clear();
                    try {
                        jsonArray = new JSONArray(response);
                        categoryLists = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            CategoryList categoryListRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<CategoryList>() {
                                    }.getType());
                            categoryLists.add(categoryListRider);
                        }
                        relatedProductAdapter.addAll(categoryLists);
                        dismissProgress();
                        binding.llRelatedItem.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.llRelatedItem.setVisibility(View.GONE);
                }

                binding.ivProgress.setVisibility(View.GONE);
                break;
            case RequestParamUtils.removeWishList:
            case RequestParamUtils.addWishList:
                dismissProgress();
                break;
            case RequestParamUtils.addToAbandondCart:
                Log.e("Response is ", response);
                break;
            case RequestParamUtils.VERIFY_REVIEW:
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
                        boolean isOwner = jsonObject.getBoolean("owner");
                        if (isOwner) {
                            Intent i = new Intent(this, RateAndReviewActivity.class);
                            i.putExtra(RequestParamUtils.PRODUCT_NAME, binding.tvProductName.getText().toString());
                            i.putExtra(RequestParamUtils.PRODUCT_ID, String.valueOf(categoryList.id));
                            if (categoryList.images.size() > 0) {
                                i.putExtra(RequestParamUtils.IMAGEURL, categoryList.images.get(0).src);
                            }
                            startActivity(i);
                        } else {
                            String message = getResources().getString(R.string.review_verify);
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String message = getResources().getString(R.string.review_verify);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("Exception =", e.getMessage());
                }
                break;
            case RequestParamUtils.PincodeView:
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    binding.tvDeliverable.setVisibility(View.VISIBLE);
                    String strDeliverable;
                    if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
                        strDeliverable = Constant.settingOptions.availableatText + " " + binding.etPincode.getText().toString();
                    } else {
                        strDeliverable = Constant.settingOptions.codNotAvailableMsg + " " + binding.etPincode.getText().toString();
                    }
                    binding.tvDeliverable.setText(strDeliverable);
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e.getMessage());
                }
                break;
            case RequestParamUtils.IsUserExists:

            if (response != null && response.length() > 0) {
                    try {
//                        {
//                            "is_user_exists": "true"
//                        }
//
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("is_user_exists");
                        Log.e("TAG", "onResponse:+++++ " + "" + status);
                        if (!status.equals("true")) {
                            setLogoutFornonExistUserDialog();
                            //   Toast.makeText(this, "No Exist", Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                    }
                }

            break;
        }
    }

    private void isGroupProductAddToCart() {
        for (int i = 0; i < categoryList.groupedProducts.size(); i++) {
            if (databaseHelper.getProductFromCartById(categoryList.groupedProducts.get(i) + "") != null) {
                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
            } else {
                binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
                break;
            }
        }
//        if (databaseHelper.getProductFromCartById(id + "") != null) {
//            tvCart.setText(getResources().getString(R.string.go_to_cart));
//            return true;
//        }
//        return false;
    }

    private void setRate(int totalReview) {
        binding.tvAverageRatting.setText(Constant.setDecimalTwo(Double.valueOf(categoryList.averageRating)));
        binding.ratingBar.setRating(Float.parseFloat(categoryList.averageRating));
        binding.rattingFive.setProgress((fiveRate / totalReview) * 100);
        binding.rattingFour.setProgress((fourRate / totalReview) * 100);
        binding.rattingThree.setProgress((threeRate / totalReview) * 100);
        binding.rattingTwo.setProgress((twoRate / totalReview) * 100);
        binding.rattingOne.setProgress((oneRate / totalReview) * 100);
    }

    private void setVpProductImages() {
        for (int i = 0; i < imageList.size(); i++) {
            if (imageList.get(i).src != null && imageList.get(i).src.contains(RequestParamUtils.placeholder)) {
                if (imageList.get(i).type == null || !imageList.get(i).type.equals("Video")) {
                    if (imageList.size() > 1) {
                        imageList.remove(imageList.get(i));
                    }
                }
            }
        }

        addBottomDots(0, imageList.size());
        if (productImageViewPagerAdapter == null) {
            productImageViewPagerAdapter = new ProductImageViewPagerAdapter(this, categoryList.id);
            binding.vpProductImages.setAdapter(productImageViewPagerAdapter);
            productImageViewPagerAdapter.addAll(imageList);

            binding.vpProductImages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    addBottomDots(position, imageList.size());
                    currentPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            binding.vpProductImages.setCurrentItem(0);
            productImageViewPagerAdapter.addAll(imageList);
        }
    }

    private void addBottomDots(int currentPage, int length) {
        binding.layoutDots.removeAllViews();
        dots = new TextView[length];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.gray));
            binding.layoutDots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void setRvGroupProduct() {
        groupProductAdapter = new GroupProductAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvGroupProduct.setLayoutManager(mLayoutManager);
        binding.rvGroupProduct.setAdapter(groupProductAdapter);
        binding.rvGroupProduct.setNestedScrollingEnabled(false);
    }

    public void setColorData() {
        productColorAdapter = new ProductColorAdapter(ProductDetailActivity.this, ProductDetailActivity.this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvColor.setLayoutManager(mLayoutManager);
        binding.rvColor.setAdapter(productColorAdapter);
        binding.rvColor.setNestedScrollingEnabled(false);
        productColorAdapter.addAll(categoryList.attributes.get(0).options);
        productColorAdapter.setType(categoryList.type);
        productColorAdapter.getDialogList(categoryList.attributes);
    }

    public void setReviewData() {
        reviewAdapter = new ReviewAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvReview.setLayoutManager(mLayoutManager);
        binding.rvReview.setAdapter(reviewAdapter);
        binding.rvReview.setNestedScrollingEnabled(false);
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        if (outerPos != 11459060) {
            changePrice();
        } else if (outerPos == 11459060) {

            boolean isAllProductAdded = false;
            for (int i = 0; i < groupProductAdapter.getList().size(); i++) {
                if (databaseHelper.getProductFromCartById(groupProductAdapter.getList().get(i).id + "") != null) {
                    isAllProductAdded = true;
                } else {
                    isAllProductAdded = false;
                    break;
                }
            }
            if (isAllProductAdded) {
                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
            } else {
                binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
            }
            if (groupProductAdapter != null) {
                groupProductAdapter.notifyDataSetChanged();
            }
        }
//        changePrice();
    }


    public Cart getCartVariationProduct() {
        Log.e("getCartVariation", "called");
        String appThumbnail = categoryList.appthumbnail;
        boolean isManageStock = categoryList.manageStock;
        String htmlPrice = categoryList.priceHtml;
        List<String> list = new ArrayList<>();
        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < combination.size(); i++) {
                String value = combination.get(i);
                String[] valuearray = new String[0];
                if (value != null && value.contains("->")) {
                    valuearray = value.split("->");
                }
                if (valuearray.length > 0) {
                    object.put(valuearray[0], valuearray[1]);
                }
                list.add(combination.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Cart cart = new Cart();
        cart.setQuantity(1);
        cart.setVariation(object.toString());
        setVariationPriceAndOtherDetailToList(isManageStock);
        cart.setVariationid(new CheckIsVariationAvailable().getVariationId(variationList, list));
        cart.setProductid(categoryList.id + "");
        cart.setBuyNow(0);
        cart.setManageStock(categoryList.manageStock);
        cart.setStockQuantity(CheckIsVariationAvailable.stockQuantity);
        setImagesByVariation(cart.getVariationid());

        cart.setProduct(new Gson().toJson(categoryList));
        setOriginalPrice(htmlPrice, appThumbnail, isManageStock);
        return cart;

    }

    public void getDefaultVariationId() {
        Log.e("default variation id ", "called");
        List<String> list = new ArrayList<>();
        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < combination.size(); i++) {
                String value = combination.get(i);
                String[] valuearray = new String[0];
                if (value != null && value.contains("->")) {
                    valuearray = value.split("->");
                }
                if (valuearray.length > 0) {
                    object.put(valuearray[0], valuearray[1]);
                }
                list.add(combination.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        defaultVariationId = new CheckIsVariationAvailable().getVariationId(variationList, list);
        setImagesByVariation(defaultVariationId);
    }

    public void setImagesByVariation(int variationId) {
        if (variationId != defaultVariationId) {
            if (CheckIsVariationAvailable.imageSrc != null && !CheckIsVariationAvailable.imageSrc.contains(RequestParamUtils.placeholder)) {
                imageList = new ArrayList<>();
                addSelectedImage();
                productImageViewPagerAdapter = new ProductImageViewPagerAdapter(this, categoryList.id);
                binding.vpProductImages.setAdapter(productImageViewPagerAdapter);
                addBottomDots(0, imageList.size());
                productImageViewPagerAdapter.addAll(imageList);
            } else {
                imageList = new ArrayList<>();
                imageList.addAll(categoryList.images);
                setVpProductImages();
            }
        } else {
            if (CheckIsVariationAvailable.imageSrc != null && !CheckIsVariationAvailable.imageSrc.contains(RequestParamUtils.placeholder)) {
                imageList = new ArrayList<>();
                addSelectedImage();
                imageList.addAll(categoryList.images);
                setVpProductImages();
            }
        }
    }

    public void addSelectedImage() {
        CategoryList.Image image = new CategoryList().getImageInstance();
        if (categoryList.featuredVideo != null && categoryList.featuredVideo.imageUrl != null &&
                Constant.IS_YITH_FEATURED_VIDEO_ACTIVE) {
            image.src = categoryList.featuredVideo.imageUrl;
            image.url = categoryList.featuredVideo.url;
            image.type = "Video";
            imageList.add(image);
        }
        image = new CategoryList().getImageInstance();
        image.src = CheckIsVariationAvailable.imageSrc;
        imageList.add(image);
    }

    public void sellerRedirection() {
        Intent intent = new Intent(this, SellerInfoActivity.class);
        intent.putExtra(RequestParamUtils.ID, categoryList.sellerInfo.sellerId);
        startActivity(intent);
    }

    public void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_variation, null);
        dialogBuilder.setView(dialogView);

        rvProductVariation = dialogView.findViewById(R.id.rvProductVariation);
        TextViewRegular tvDone = dialogView.findViewById(R.id.tvDone);
        TextViewRegular tvCancel = dialogView.findViewById(R.id.tvCancel);
        setVariation(rvProductVariation);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        tvCancel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvDone.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvCancel.setOnClickListener(view -> alertDialog.dismiss());
        tvDone.setOnClickListener(view -> {
            if (alertDialog != null) {
                alertDialog.show();
            }
            if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, categoryList.attributes)) {
                toast.showToast(getString(R.string.combition_doesnt_exist));
            } else {
                toast.cancelToast();
                alertDialog.dismiss();
                if (databaseHelper.getVariationProductFromCart(getCartVariationProduct())) {
                    binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                } else {
                    binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
                }
                changePrice();
            }
        });
        changePrice();
    }

    public void setVariation(RecyclerView rvVariation) {
        productVariationAdapter = new ProductVariationAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvVariation.setLayoutManager(mLayoutManager);
        rvVariation.setAdapter(productVariationAdapter);
        rvVariation.setNestedScrollingEnabled(false);
        productVariationAdapter.addAll(categoryList.attributes);
        productVariationAdapter.addAllVariationList(variationList);
    }

    public void addToCart(Cart cart, String id) {
        //   showCart();
        cart.setBuyNow(0);
        databaseHelper.addToCart(cart);
        Intent intent = new Intent(this, CartActivity.class);
        intent.putExtra(RequestParamUtils.ID, categoryList.id + "");
        intent.putExtra(RequestParamUtils.buynow, 0);
        startActivity(intent);
    }

    public void changePrice() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < combination.size(); i++) {
            list.add(combination.get(i));
        }
        new CheckIsVariationAvailable().getVariationId(variationList, list);
        if (CheckIsVariationAvailable.priceHtml != null) {
            setPrice(CheckIsVariationAvailable.priceHtml);
        }
        if (categoryList.inStock) {
            if (categoryList.type.equals(RequestParamUtils.variable)) {
                if (categoryList.manageStock) {
                    if (CheckIsVariationAvailable.inStock && CheckIsVariationAvailable.stockQuantity != 0) {
                        binding.tvAvailibility.setText(R.string.in_stock);
                        binding.tvAvailibility.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
                        binding.tvBuyNow.setClickable(true);
                        binding.tvBuyNow.setAlpha((float) 1.0);
                        binding.tvCart.setClickable(true);
                        binding.tvCart.setAlpha((float) 1.0);
                    } else {
                        binding.tvAvailibility.setText(R.string.out_of_stock);
                        binding.tvAvailibility.setTextColor(Color.RED);
                        binding.tvBuyNow.setAlpha((float) 0.6);
                        binding.tvBuyNow.setClickable(false);
                        binding.tvCart.setAlpha((float) 0.6);
                        binding.tvCart.setClickable(false);
                    }
                } else {
                    if (CheckIsVariationAvailable.inStock) {
                        binding.tvAvailibility.setText(R.string.in_stock);
                        binding.tvAvailibility.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
                        binding.tvBuyNow.setClickable(true);
                        binding.tvBuyNow.setAlpha((float) 1.0);
                        binding.tvCart.setClickable(true);
                        binding.tvCart.setAlpha((float) 1.0);
                    } else {
                        binding.tvAvailibility.setText(R.string.out_of_stock);
                        binding.tvAvailibility.setTextColor(Color.RED);
                        binding.tvBuyNow.setAlpha((float) 0.6);
                        binding.tvBuyNow.setClickable(false);
                        binding.tvCart.setAlpha((float) 0.6);
                        binding.tvCart.setClickable(false);
                    }
                }
            } else {
                binding.tvAvailibility.setText(R.string.in_stock);
                binding.tvAvailibility.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
            }
        } else {
            binding.tvAvailibility.setText(R.string.out_of_stock);
            binding.tvAvailibility.setTextColor(Color.RED);
            binding.tvBuyNow.setAlpha((float) 0.6);
            binding.tvBuyNow.setClickable(false);
            binding.tvCart.setAlpha((float) 0.6);
            binding.tvCart.setClickable(false);
        }

        if (databaseHelper.getVariationProductFromCart(getCartVariationProduct())) {
            binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
        } else {
            binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
        }
    }

    public void removeWishList(boolean isDialogShow, String userid, String productId) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }

            PostApi postApi = new PostApi(ProductDetailActivity.this, RequestParamUtils.removeWishList, this, getlanuage());
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
    protected void onRestart() {
        super.onRestart();
//        showCart();

        if (!categoryList.type.equals("grouped")) {
            if (databaseHelper.getProductFromCartById(categoryList.id + "") != null) {
                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
            } else {
                binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
            }
        } else {
            boolean isAllProductAdded = false;
            for (int i = 0; i < groupProductAdapter.getList().size(); i++) {
                if (databaseHelper.getProductFromCartById(groupProductAdapter.getList().get(i).id + "") != null) {
                    isAllProductAdded = true;
                } else {
                    isAllProductAdded = false;
                    break;
                }
            }

            if (isAllProductAdded) {
                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
            } else {
                binding.tvCart.setText(getResources().getString(R.string.add_to_Cart));
            }
            if (groupProductAdapter != null) {
                groupProductAdapter.notifyDataSetChanged();
            }
        }
        Constant.CATEGORYDETAIL = categoryList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed();
    }

    public void backPressed() {
        if (isDeepLinking) {
            Intent intent = new Intent(ProductDetailActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        finish();
    }

    public void setVariationPriceAndOtherDetailToList(boolean isManage) {
        if (CheckIsVariationAvailable.priceHtml != null) {
            categoryList.priceHtml = CheckIsVariationAvailable.priceHtml;
            categoryList.price = CheckIsVariationAvailable.price + "";
            categoryList.taxPrice = CheckIsVariationAvailable.taxPrice + "";
        }
        if (CheckIsVariationAvailable.imageSrc != null && !CheckIsVariationAvailable.imageSrc.contains(RequestParamUtils.placeholder)) {
            categoryList.appthumbnail = CheckIsVariationAvailable.imageSrc;
        }
        if (!isManage) {
            categoryList.manageStock = CheckIsVariationAvailable.isManageStock;
        }
    }

    public void setOriginalPrice(String priceHtml, String appThumbnail, boolean isManage) {
        categoryList.priceHtml = priceHtml;
        categoryList.appthumbnail = appThumbnail;
        categoryList.manageStock = isManage;
//        categoryList.price = CheckIsVariationAvailable.price + "";
//        categoryList.taxPrice = CheckIsVariationAvailable.taxPrice + "";
    }

    public void setClickEvent() {
        binding.tvRateReview.setOnClickListener(v -> {
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            if (binding.tvRateReview.getVisibility() == View.VISIBLE) {
                if (getPreferences().getBoolean(RequestParamUtils.Review_Varification, false)) {
                    if (customerId.equals("")) {
                        Intent intent = new Intent(ProductDetailActivity.this, LogInActivity.class);
                        startActivity(intent);
                    } else {
                        verificationReview(customerId);
                    }
                } else {
                    Intent i = new Intent(this, RateAndReviewActivity.class);
                    i.putExtra(RequestParamUtils.PRODUCT_NAME, binding.tvProductName.getText().toString());
                    i.putExtra(RequestParamUtils.PRODUCT_ID, String.valueOf(categoryList.id));
                    if (categoryList.images.size() > 0) {
                        i.putExtra(RequestParamUtils.IMAGEURL, categoryList.images.get(0).src);
                    }
                    startActivity(i);
                }
            }
        });

        binding.tvCheck.setOnClickListener(v -> {
            String pinCode = binding.etPincode.getText().toString();

            if (pinCode.isEmpty()) {
                Toast.makeText(this, Constant.settingOptions.errorMsgBlank, Toast.LENGTH_LONG).show();
            } else {
                if (Utils.isInternetConnected(this)) {
                    showProgress("");
                    try {
                        PostApi postApi = new PostApi(this, RequestParamUtils.PincodeView, this, getlanuage());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(RequestParamUtils.PRODUCT_ID, categoryList.id);
                        jsonObject.put(RequestParamUtils.PincodeCheck, pinCode);
                        postApi.callPostApi(new URLS().DELIVER_PINCODE, jsonObject.toString());
                    } catch (Exception e) {
                        Log.e("verificationReview", e.getMessage());
                    }
                } else {
                    Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.tvCheckAllReview.setOnClickListener(v -> {
            Intent i = new Intent(this, CheckAllActivity.class);
            startActivity(i);
        });

        binding.tvMoreQuickOverview.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductQuickDetailActivity.class);
            intent.putExtra(RequestParamUtils.title, getString(R.string.quick_overviews));
            intent.putExtra(RequestParamUtils.name, categoryList.name + "");
            intent.putExtra(RequestParamUtils.description, categoryList.shortDescription + "");
            if (categoryList.images.size() > 0) {
                intent.putExtra(RequestParamUtils.image, categoryList.images.get(0).src);
            } else {
                intent.putExtra(RequestParamUtils.image, "");
            }
            startActivity(intent);
        });

        binding.tvMoreDetail.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductQuickDetailActivity.class);
            intent.putExtra(RequestParamUtils.title, getString(R.string.detail));
            intent.putExtra(RequestParamUtils.name, categoryList.name + "");
            intent.putExtra(RequestParamUtils.description, categoryList.description + "");
            if (categoryList.images.size() > 0) {
                intent.putExtra(RequestParamUtils.image, categoryList.images.get(0).src);
            } else {
                intent.putExtra(RequestParamUtils.image, "");
            }
            startActivity(intent);
        });

        binding.tvSellerMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellerMoreInfoActivity.class);
            intent.putExtra(RequestParamUtils.data, categoryList.sellerInfo.storeTnc);
            intent.putExtra(RequestParamUtils.Dealer, categoryList.sellerInfo.storeName);
            startActivity(intent);
        });

        binding.tvBuyNow.setOnClickListener(v -> {
            switch (categoryList.type) {
                case RequestParamUtils.variable:
                    isDialogOpen = true;
                    if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, categoryList.attributes)) {
                        toast.showToast(getString(R.string.variation_not_available));
                        toast.showRedBg();
                    } else {
                        if (getCartVariationProduct() != null) {
                            Cart cart = getCartVariationProduct();
                            if (!databaseHelper.getVariationProductFromCart(cart)) {
                                databaseHelper.addVariationProductToCart(getCartVariationProduct());
                                //  showCart();
                                toast.showToast(getString(R.string.item_added_to_your_cart));
                                toast.showBlackBg();
                            }
                            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                            intent.putExtra(RequestParamUtils.buynow, 0);
                            startActivity(intent);
                        } else {
                            toast.showToast(getString(R.string.variation_not_available));
                            toast.showRedBg();
                        }
                    }
                    break;
                case RequestParamUtils.simple:
                    Cart cart = new Cart();
                    cart.setQuantity(1);
                    cart.setProduct(new Gson().toJson(categoryList));
                    cart.setVariationid(0);
                    cart.setProductid(categoryList.id + "");
                    cart.setManageStock(categoryList.manageStock);
                    cart.setStockQuantity(categoryList.stockQuantity);
                    addToCart(cart, categoryList.id + "");
                    break;
                case RequestParamUtils.grouped:
                    for (int i = 0; i < groupProductAdapter.getList().size(); i++) {
                        if (groupProductAdapter.getList().get(i).type.equals(RequestParamUtils.simple)) {
                            JSONObject object = new JSONObject();
                            try {
                                for (int j = 0; j < groupProductAdapter.getList().get(i).attributes.size(); j++) {
                                    object.put(groupProductAdapter.getList().get(i).attributes.get(j).name, groupProductAdapter.getList().get(i).attributes.get(j).options.get(0));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cart = new Cart();
                            cart.setQuantity(1);
                            cart.setVariation(object.toString());
                            cart.setProduct(new Gson().toJson(groupProductAdapter.getList().get(i)));
                            cart.setVariationid(0);
                            cart.setProductid(groupProductAdapter.getList().get(i).id + "");
                            addToCart(cart, categoryList.id + "");
                        }
                    }
                    break;
            }
        });

        binding.tvCart.setOnClickListener(v -> {
            switch (categoryList.type) {
                case RequestParamUtils.variable:
                    isDialogOpen = true;
                    if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, categoryList.attributes)) {
                        toast = new CustomToast(this);
                        toast.showToast(getString(R.string.variation_not_available));
                        toast.showRedBg();
                    } else {
                        if (getCartVariationProduct() != null) {
                            Cart cart = getCartVariationProduct();
                            if (databaseHelper.getVariationProductFromCart(cart)) {
                                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                                intent.putExtra(RequestParamUtils.buynow, 0);
                                startActivity(intent);
                            } else {
                                databaseHelper.addVariationProductToCart(getCartVariationProduct());
                                // showCart();
                                toast.showToast(getString(R.string.item_added_to_your_cart));
                                toast.showBlackBg();
                                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                            }
                        } else {
                            toast.showToast(getString(R.string.variation_not_available));
                            toast.showRedBg();
                        }
                    }
                    break;
                case RequestParamUtils.simple:
                    Cart cart = new Cart();
                    cart.setQuantity(1);
                    cart.setProduct(new Gson().toJson(categoryList));
                    cart.setVariationid(0);
                    cart.setProductid(categoryList.id + "");
                    cart.setBuyNow(0);
                    cart.setManageStock(categoryList.manageStock);
                    cart.setStockQuantity(categoryList.stockQuantity);
                    if (databaseHelper.getProductFromCartById(categoryList.id + "") != null) {
                        databaseHelper.addToCart(cart);
                        Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                        intent.putExtra(RequestParamUtils.buynow, 0);
                        startActivity(intent);
                    } else {
                        databaseHelper.addToCart(cart);
                        // showCart();
                        toast.showToast(getString(R.string.item_added_to_your_cart));
                        toast.showBlackBg();
                        binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                    }
                    String value = tvPrice1.getText().toString();
                    if (value.contains(Constant.CURRENCYSYMBOL)) {
                        value = value.replaceAll(Constant.CURRENCYSYMBOL, "");
                    }
                    if (value.contains(Constant.CURRENCYSYMBOL)) {
                        value = value.replace(Constant.CURRENCYSYMBOL, "");
                    }
                    value = value.replaceAll("\\s", "");
                    value = value.replaceAll(",", "");
                    Log.e(TAG, "tvCartClick: " + value);
//                    try {
//                        logAddedToCartEvent(String.valueOf(categoryList.id), categoryList.name, Constant.CURRENCYSYMBOL, Double.parseDouble(value));
//                    } catch (Exception e) {
//                        Log.e("TAG", "Exception: " + e.getMessage());
//                    }
                    break;
                case RequestParamUtils.grouped:
                    for (int i = 0; i < groupProductAdapter.getList().size(); i++) {
                        if (groupProductAdapter.getList().get(i).type.equals(RequestParamUtils.simple)) {
                            JSONObject object = new JSONObject();
                            try {
                                for (int j = 0; j < groupProductAdapter.getList().get(i).attributes.size(); j++) {
                                    object.put(groupProductAdapter.getList().get(i).attributes.get(j).name, groupProductAdapter.getList().get(i).attributes.get(j).options.get(0));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cart = new Cart();
                            cart.setQuantity(1);
                            cart.setVariation(object.toString());
                            cart.setProduct(new Gson().toJson(groupProductAdapter.getList().get(i)));
                            cart.setVariationid(0);
                            cart.setProductid(groupProductAdapter.getList().get(i).id + "");
                            cart.setBuyNow(0);
                            cart.setManageStock(categoryList.manageStock);
                            cart.setStockQuantity(groupProductAdapter.getList().get(i).stockQuantity);
                            if (databaseHelper.getProductFromCartById(groupProductAdapter.getList().get(i).id + "") != null) {
                                databaseHelper.addToCart(cart);
                                if (i == groupProductAdapter.getItemCount() - 1) {
                                    Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                                    intent.putExtra(RequestParamUtils.buynow, 0);
                                    startActivity(intent);
                                }
                            } else {
                                databaseHelper.addToCart(cart);
                                //  showCart();
                                toast.showToast(getString(R.string.item_added_to_your_cart));
                                toast.showBlackBg();
                                binding.tvCart.setText(getResources().getString(R.string.go_to_cart));
                            }

                            value = tvPrice1.getText().toString();
                            if (value.contains(Constant.CURRENCYSYMBOL)) {
                                value = value.replaceAll(Constant.CURRENCYSYMBOL, "");
                            }
                            if (value.contains(Constant.CURRENCYSYMBOL)) {
                                value = value.replace(Constant.CURRENCYSYMBOL, "");
                            }
                            value = value.replaceAll("\\s", "");
                            value = value.replaceAll(",", "");
                            Log.e(TAG, "tvCartClick: " + value);

//                            try {
//                                logAddedToCartEvent(String.valueOf(groupProductAdapter.getList().get(i).id), groupProductAdapter.getList().get(i).name, Constant.CURRENCYSYMBOL, Double.parseDouble(value));
//                            } catch (Exception e) {
//                                Log.e("TAG", "Exception: " + e.getMessage());
//                            }
                        }
                    }
                    break;
            }
        });

        binding.tvContactSeller.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactSellerActivity.class);
            intent.putExtra(RequestParamUtils.ID, categoryList.sellerInfo.sellerId);
            intent.putExtra(RequestParamUtils.Dealer, categoryList.sellerInfo.storeName);
            startActivity(intent);
        });

        binding.tvViewStore.setOnClickListener(v -> sellerRedirection());

        binding.tvSellerName.setOnClickListener(v -> sellerRedirection());

        binding.ivShare.setOnClickListener(v -> {
            showProgress("");

            if (Constant.DeepLinkDomain != null && !Constant.DeepLinkDomain.isEmpty()) {
                String strDescription;
                if (categoryList.shortDescription != null && categoryList.shortDescription.length() > 0) {
                    if (categoryList.shortDescription.length() >= 300) {
                        strDescription = categoryList.shortDescription;
                        strDescription = strDescription.substring(0, 300);
                    } else {
                        strDescription = categoryList.shortDescription;
                    }
                } else {
                    strDescription = "";
                }

                DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(categoryList.permalink + "#" + categoryList.id))
                        .setDomainUriPrefix(Constant.DeepLinkDomain)
                        // Open links with this app on Android
                        .setAndroidParameters(
                                new DynamicLink.AndroidParameters.Builder()
                                        .setMinimumVersion(Constant.PlaystoreMinimumVersion)
                                        .build())
                        // Open links with com.example.ios on iOS
                        .setIosParameters(
                                new DynamicLink.IosParameters.Builder(Constant.DynamicLinkIosParameters)
                                        .setAppStoreId(Constant.AppleAppStoreId)
                                        .setMinimumVersion(Constant.AppleAppVersion)
                                        .build())
                        .setSocialMetaTagParameters(
                                new DynamicLink.SocialMetaTagParameters.Builder()
                                        .setTitle(categoryList.name)
                                        .setDescription(strDescription)
                                        .setImageUrl(Uri.parse(imageList.get(0).src))
                                        .build())
                        .buildDynamicLink();

                Uri dynamicLinkUri = dynamicLink.getUri();

                Log.e(TAG, "ivShareClick: " + dynamicLinkUri);

                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(dynamicLinkUri)
                        .setDomainUriPrefix(Constant.DeepLinkDomain)
                        // Set parameters
                        // ...
                        .buildShortDynamicLink()
                        .addOnCompleteListener(this, task -> {
                            dismissProgress();
                            try {
                                Uri shortLink = task.getResult().getShortLink();
                                Uri flowChartLink = task.getResult().getPreviewLink();
                                Log.e(TAG, "shortLink: " + shortLink.toString());
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                Log.e(TAG, "categoryList Id: " + shortLink + "#" + categoryList.id);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shortLink + "#" + categoryList.id);
                                // shareIntent.putExtra(Intent.EXTRA_TEXT, categoryList.permalink);
                                startActivity(Intent.createChooser(shareIntent, "Share link using"));

                                Log.e(TAG, "shortLink: " + shortLink);
                            } catch (Exception e) {
                                Log.e("Exception is ", e.getMessage());
                                dismissProgress();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, categoryList.permalink);
                                startActivity(Intent.createChooser(shareIntent, "Share link using"));
                            }
                        });
                Log.e("Dynemic link is ", dynamicLink.getUri().toString());
            } else {
                dismissProgress();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, categoryList.permalink);
                startActivity(Intent.createChooser(shareIntent, "Share link using"));
            }
        });

        binding.llDialog.setOnClickListener(v -> {
            if (categoryList.type.equals(RequestParamUtils.variable)) {
                if (alertDialog != null) {
                    alertDialog.show();
                }
                productColorAdapter.notifyDataSetChanged();
//            productColorAdapter.addAllVariationList(variationList);
            } else if (categoryList.type.equals(RequestParamUtils.simple)) {
                alertDialog.show();
            }
        });

        binding.ivWishList.setOnClickListener(v -> {
            if (databaseHelper.getWishlistProduct(categoryList.id + "")) {
                binding.ivWishList.setChecked(false);
                String userid = getPreferences().getString(RequestParamUtils.ID, "");
                if (!userid.equals("")) {
                    removeWishList(true, userid, categoryList.id + "");
                }
                databaseHelper.deleteFromWishList(categoryList.id + "");
            } else {
                binding.ivWishList.setChecked(true);
                binding.ivWishList.playAnimation();
                WishList wishList = new WishList();
                wishList.setProduct(new Gson().toJson(categoryList));
                wishList.setProductid(categoryList.id + "");
                databaseHelper.addToWishList(wishList);
                String userid = getPreferences().getString(RequestParamUtils.ID, "");
                if (!userid.equals("")) {
                    removeWishList(true, userid, categoryList.id + "");
                }

                String value = tvPrice1.getText().toString();
                if (value.contains(Constant.CURRENCYSYMBOL)) {
                    value = value.replaceAll(Constant.CURRENCYSYMBOL, "");
                }
                if (value.contains(Constant.CURRENCYSYMBOL)) {
                    value = value.replace(Constant.CURRENCYSYMBOL, "");
                }
                value = value.replaceAll("\\s", "");
                value = value.replaceAll(",", "");
//                try {
//                    logAddedToWishlistEvent(String.valueOf(categoryList.id), categoryList.name, Constant.CURRENCYSYMBOL, Double.parseDouble(value));
//                } catch (Exception e) {
//                    Log.e("TAG", "Exception: " + e.getMessage());
//                }
            }
        });

    }
}




package com.example.ciyashop.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ciyashop.library.apicall.GetApiWithoutOauth;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.BlogAdapter;
import com.example.ciyashop.customview.GridSpacingItemDecoration;
import com.example.ciyashop.databinding.ActivityBlogBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.BlogPost;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BlogActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private BlogAdapter blogAdapter;
    private final List<BlogPost> blogPostsList = new ArrayList<>();
    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    Boolean setNoItemFound = false;
    private int page = 1;

    private ActivityBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBlogBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        settvImage();
        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        setAdapter();
        setScreenLayoutDirection();
        getBlog(true);
    }

    public void setAdapter() {
        blogAdapter = new BlogAdapter(this, this);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false);
        binding.rvBlog.setLayoutManager(mLayoutManager);
        binding.rvBlog.setAdapter(blogAdapter);
        binding.rvBlog.setNestedScrollingEnabled(false);
        binding.rvBlog.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(20), true));
        binding.rvBlog.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {   //check for scroll down
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            if (!setNoItemFound) {
                                loading = false;
                                page = page + 1;
                                Log.e("End ", "Last Item Wow  and page no:- " + page);
                                binding.llProgress.setVisibility(View.VISIBLE);
                                binding.progressWheel.getIndeterminateDrawable().setColorFilter(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)), PorterDuff.Mode.SRC_ATOP);
                                getBlog(false);
                                //Do pagination.. i.e. fetch new data
                            }
                        }
                    }
                }
            }
        });
    }

    public void getBlog(boolean isDialogShow) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }
            GetApiWithoutOauth getApi = new GetApiWithoutOauth(this, RequestParamUtils.getBlog, this, getlanuage());
            String strURl = new URLS().WOO_BLOG_URL + "?page=" + page;
            getApi.callGetApi(strURl);

        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    @Override
    public void onResponse(String response, String methodName) {
        dismissProgress();
        if (response != null && response.length() > 0) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    //Convert json response into gson and made model class
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    BlogPost blogPostRider = gson.fromJson(
                            object.toString(), new TypeToken<BlogPost>() {
                            }.getType());
                    blogPostsList.add(blogPostRider);
                }
                loading = true;
                blogAdapter.addAll(blogPostsList);
            } catch (JSONException e) {
                Log.e("Json Exception is ", e.getMessage());
            }
        }
        binding.llProgress.setVisibility(View.GONE);
    }
}

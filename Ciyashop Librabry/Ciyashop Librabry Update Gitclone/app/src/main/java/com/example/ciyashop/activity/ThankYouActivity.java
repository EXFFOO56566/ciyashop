package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityThankYouBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONObject;


public class ThankYouActivity extends BaseActivity implements OnResponseListner {

    private DatabaseHelper databaseHelper;
    private ActivityThankYouBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityThankYouBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        databaseHelper = new DatabaseHelper(this);
        setScreenLayoutDirection();
        setThem();
        databaseHelper.clearCart();
        logout();
    }

    public void logout() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.logout, this, getlanuage());
            postApi.callPostApi(new URLS().LOGOUT, "");
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void setThem() {
        TextView tvContinueShopping = findViewById(R.id.tvContinueShopping);
        Drawable unwrappedDrawable = tvContinueShopping.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor((getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)))));

        binding.tvThankYou.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        TextView tvGotoMyorder = findViewById(R.id.tvGotoMyorder);
        Drawable unwrappedDrawable1 = tvGotoMyorder.getBackground();
        Drawable wrappedDrawable1 = DrawableCompat.wrap(unwrappedDrawable1);
        DrawableCompat.setTint(wrappedDrawable1, (Color.parseColor((getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)))));


        binding.ivGo2.setColorFilter(Color.parseColor(getPreferences().getString(Constant.PRIMARY_COLOR, Constant.PRIMARY_COLOR)));
        binding.ivGo.setColorFilter(Color.parseColor(getPreferences().getString(Constant.PRIMARY_COLOR, Constant.PRIMARY_COLOR)));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.logout)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                    } else {
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ThankYouActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void setClickEvent() {
        binding.tvContinueShopping.setOnClickListener(v -> {
            clearCustomer();
            Intent i = new Intent(ThankYouActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });


        binding.tvGotoMyorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearCustomer();
                Intent i = new Intent(ThankYouActivity.this, MyOrderActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });


    }
}
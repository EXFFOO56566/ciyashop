package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.ciyashop.R;
import com.example.ciyashop.adapter.IntroViewPagerAdapter;
import com.example.ciyashop.databinding.ActivityIntroSliderBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

public class IntroSliderActivity extends BaseActivity {

    private ActivityIntroSliderBinding binding;
    private IntroViewPagerAdapter introViewPagerAdapter;
    private int currentPosition = 0;
    private boolean isSkip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroSliderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setScreenLayoutDirection();
        setClickEvent();
        setBannerView();
    }

    //ToDo : set view pager
    private void setBannerView() {
        introViewPagerAdapter = new IntroViewPagerAdapter(this);
        binding.vpIntro.setAdapter(introViewPagerAdapter);
        addBottomDots(0, introViewPagerAdapter.getCount());
        binding.vpIntro.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position, introViewPagerAdapter.getCount());
                currentPosition = position;

                if (currentPosition == 2) {
                    binding.tvNext.setText(getResources().getString(R.string.done));
                    binding.tvNext.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                } else {
                    binding.tvNext.setTextColor(getResources().getColor(R.color.gray));
                    binding.tvNext.setText(getResources().getString(R.string.next));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (Config.IS_RTL) {
            binding.vpIntro.setRotationY(180);
        } else {
            binding.vpIntro.setRotationY(0);
        }
    }

    public void setClickEvent() {
        binding.tvNext.setOnClickListener(v -> {
            if (binding.tvNext.getText().toString().contains(getResources().getString(R.string.done))) {
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, 1);
                editor.apply();
                if (sharedpreferences.getBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, false)) {
                    Intent intent = new Intent(this, LogInActivity.class);
                    intent.putExtra("is_splash", true);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                binding.vpIntro.setCurrentItem(currentPosition + 1);
                if (currentPosition == 2) {
                    binding.tvNext.setText(getResources().getString(R.string.done));
                    binding.tvNext.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                } else {
                    binding.tvNext.setTextColor(getResources().getColor(R.color.gray));
                    binding.tvNext.setText(getResources().getString(R.string.next));
                }
            }
        });

        binding.tvSkip.setOnClickListener(v -> {
            isSkip = true;
            if (sharedpreferences.getBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, false)) {
                Intent intent = new Intent(this, LogInActivity.class);
                intent.putExtra("is_splash", true);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            SharedPreferences.Editor editor = getPreferences().edit();
            editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, 1);
            editor.apply();
        });
    }

    //ToDo : add dot  dynamically in bottom of view pager
    private void addBottomDots(int currentPage, int length) {
        binding.layoutDots.removeAllViews();
        ImageView[] dots = new ImageView[length];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_intro_unfill, null));
            dots[i].setPadding(0, 0, 10, 0);
            binding.layoutDots.addView(dots[i]);
            dots[i].setColorFilter(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        }
        if (dots.length > 0 && dots.length >= currentPage) {
            dots[currentPage].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_intro_fill, null));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isSkip)
            finish();
        if (!getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
            startActivity(new Intent(IntroSliderActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(IntroSliderActivity.this, IntroSliderActivity.class));
        }
    }
}

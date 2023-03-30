package com.example.ciyashop.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ciyashop.R;

/**
 * Created by Bhumi Shah on 11/28/2017.
 */

public class CustomToast {
    private final Activity context;
    private TextView tvTitle;
    private LinearLayout toast_layout_root;
    Toast toast;
    private Animation animFadeIn;

    public CustomToast(Activity context) {
        this.context = context;
        toast = new Toast(context);
        toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(setLayout());
    }

    public void showToast(String toastString) {
        if (toastString != null) {
            tvTitle.setText(toastString);
        }

        toast_layout_root.setBackgroundColor(Color.BLACK);
        toast.show();
        animFadeIn = AnimationUtils.loadAnimation(context,
                R.anim.slide_up);
        toast_layout_root.startAnimation(animFadeIn);
    }

    public void cancelToast() {
        toast.cancel();
    }

    public void showRedBg() {
        toast_layout_root.setBackgroundColor(Color.RED);
    }

    public void showBlackBg() {
        toast_layout_root.setBackgroundColor(Color.BLACK);
    }

    public void showPrimaryBg() {
        toast_layout_root.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
    }

    public View setLayout() {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_toast,
                context.findViewById(R.id.toast_layout_root));
        tvTitle = layout.findViewById(R.id.tvTitle);
        toast_layout_root = layout.findViewById(R.id.toast_layout_root);
        return layout;
    }

}

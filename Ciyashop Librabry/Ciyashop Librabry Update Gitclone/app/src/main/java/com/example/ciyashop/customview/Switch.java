package com.example.ciyashop.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * Created by Bhumi Shah on 11/9/2017.
 */

public class Switch extends android.widget.Switch {

    public Switch(Context context) {
        super(context);
        init();
    }

    public Switch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Switch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/RobotoCondensed-Regular.ttf");
        setTypeface(tf, Typeface.BOLD);
    }

}


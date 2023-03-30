package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ciyashop.R;
import com.example.ciyashop.model.Intro;

import java.util.List;


public class IntroViewPagerAdapter extends PagerAdapter {
    private final List<Intro> list;
    private LayoutInflater layoutInflater;
    private final Activity activity;
    private int length;

    public IntroViewPagerAdapter(Activity activity) {
        this.activity = activity;
        list = new Intro().getIntroList(activity);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_intro_pager, container, false);
        container.addView(view);
        ImageView ivIntro = view.findViewById(R.id.ivIntro);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        ivIntro.setImageResource(list.get(position).image);
        tvDescription.setText(list.get(position).description);
        tvTitle.setText(list.get(position).title);
        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

}


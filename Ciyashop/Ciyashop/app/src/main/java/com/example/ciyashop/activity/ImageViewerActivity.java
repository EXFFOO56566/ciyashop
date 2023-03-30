package com.example.ciyashop.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.ProductImageViewPagerAdapter;
import com.example.ciyashop.customview.pinchtozoom.ImageMatrixTouchHandler;
import com.example.ciyashop.databinding.ActivityImageViewerBinding;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;


public class ImageViewerActivity extends BaseActivity {
    private int pos;
    private int cat_id;
    private final List<CategoryList.Image> imageList = ProductImageViewPagerAdapter.list;
    private ActivityImageViewerBinding binding;

    /**
     * Step 1: Download and set up v4 support library: http://developer.android.com/tools/support-library/setup.html
     * Step 2: Create ExtendedViewPager wrapper which calls TouchImageView.canScrollHorizontallyFroyo
     * Step 3: ExtendedViewPager is a custom view and must be referred to by its full package name in XML
     * Step 4: Write TouchImageAdapter, located below
     * Step 5. The ViewPager in the XML should be ExtendedViewPager
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setClickEvent();
        pos = getIntent().getIntExtra(RequestParamUtils.pos, 0);
        cat_id = getIntent().getIntExtra(RequestParamUtils.cat_id, 0);
        ViewPager mViewPager = findViewById(R.id.view_pager);

        mViewPager.setAdapter(new TouchImageAdapter(this, getImageList()));
        mViewPager.setCurrentItem(pos);
    }

    public List<CategoryList.Image> getImageList() {
        List<CategoryList.Image> list = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++) {
            if (imageList.get(i).type != null && imageList.get(i).type.equals("Video")) {

            } else {
                list.add(imageList.get(i));
            }
        }
        return list;
    }

    public void setClickEvent() {
        binding.tvImageDone.setOnClickListener(v -> onBackPressed());
    }

    static class TouchImageAdapter extends PagerAdapter {

        private final Context context;
        private LayoutInflater layoutInflater;
        List<CategoryList.Image> list;

        public TouchImageAdapter(Context context, List<CategoryList.Image> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @NonNull
        @Override
        public View instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.item_full_image, container, false);
            ImageView img = view.findViewById(R.id.ivImage);
            final ProgressBar progress_bar = view.findViewById(R.id.progress_bar);
            //      imageView.setImageResource(R.drawable.banner);
            container.addView(view);

            Glide.with(context)
                    .load(list.get(position).src)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progress_bar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(img);
            ImageMatrixTouchHandler imageMatrixTouchHandler = new ImageMatrixTouchHandler(context);
            img.setOnTouchListener(imageMatrixTouchHandler);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}

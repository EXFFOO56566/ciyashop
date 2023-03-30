package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.BlogDescriptionActivity;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.BlogPost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogHolder> {

    private List<BlogPost> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;

    public BlogAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        databaseHelper = new DatabaseHelper(activity);
    }

    public void addAll(List<BlogPost> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog, parent, false);
        return new BlogHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd,yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date1;

        try {
            date1 = sdf.parse(list.get(position).dateGmt);
            String formattedDate = null;
            if (date1 != null) {
                formattedDate = newFormat.format(date1);
            }
            holder.tvBlogDate.setText(formattedDate);
        } catch (ParseException e) {
            Log.e("Date Exception is ", e.getMessage());
        }

        if (list.get(position).featuredImageSrc != null && list.get(position).featuredImageSrc.medium != null) {
            Glide.with(activity.getBaseContext())
                    .asBitmap().format(DecodeFormat.PREFER_ARGB_8888)
                    .error(R.drawable.no_image_available)
                    .load(list.get(position).featuredImageSrc.medium)
                    .into(holder.ivBlog);
        } else {
            holder.ivBlog.setImageResource(R.drawable.no_image_available);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvBlogTitle.setText(Html.fromHtml(list.get(position).title.rendered, Html.FROM_HTML_MODE_COMPACT));
            holder.tvBlogContent.setText(Html.fromHtml(list.get(position).content.rendered, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvBlogTitle.setText(Html.fromHtml(list.get(position).title.rendered));
            holder.tvBlogContent.setText(Html.fromHtml(list.get(position).content.rendered));
        }

        holder.ivShare.setOnClickListener(v -> {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                String shareMessage = "\n" + list.get(position).title.rendered + "\n\n";
                shareMessage = shareMessage + list.get(position).link + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                activity.startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                Log.e("Exception is ", e.getMessage());
            }
        });

        holder.tvReadMore.setOnClickListener(v -> {
            Intent intent = new Intent(activity, BlogDescriptionActivity.class);
            intent.putExtra("date", holder.tvBlogDate.getText().toString());
            intent.putExtra("id", list.get(position).id);
            intent.putExtra("name", list.get(position).title.rendered);
            intent.putExtra("description", list.get(position).content.rendered);
            intent.putExtra("link", list.get(position).link);
            if (list.get(position).featuredImageSrc != null) {
                intent.putExtra("image", list.get(position).featuredImageSrc.medium);
            }
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class BlogHolder extends RecyclerView.ViewHolder {

        TextView tvBlogDate, tvBlogTitle, tvCategory, tvBlogContent, tvReadMore;
        ImageView ivBlog, ivShare;

        public BlogHolder(View view) {
            super(view);
            tvBlogDate = view.findViewById(R.id.tvBlogDate);
            tvBlogTitle = view.findViewById(R.id.tvBlogTitle);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvBlogContent = view.findViewById(R.id.tvBlogContent);
            tvReadMore = view.findViewById(R.id.tvReadMore);
            ivBlog = view.findViewById(R.id.ivBlog);
            ivShare = view.findViewById(R.id.ivShare);
        }
    }
}
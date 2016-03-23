package com.github.colinjeremie.newsbeautifier.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.User;
import com.github.colinjeremie.newsbeautifier.utils.OnMyFeedsChanged;

import java.util.List;

/**
 ** NewsBeautifier
 * Created by james_000 on 2/16/2016.
 */
public class RssGridAdapter extends ArrayAdapter<RSSFeed> {

    private User mUser;
    private Activity mActivity;

    public RssGridAdapter(Activity activity, List<RSSFeed> rssList){
        super(activity, R.layout.grid_view_rss_tile, rssList);

        mActivity = activity;
        mUser = ((MyApplication)mActivity.getApplication()).mUser;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        RSSFeed feed = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_view_rss_tile, parent, false);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
            holder.feedDescription = (TextView) convertView.findViewById(R.id.feed_description);
            holder.feedImage = (ImageView) convertView.findViewById(R.id.feed_image);
            holder.actionTextView = (TextView) convertView.findViewById(R.id.cardview_action);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        if (feed.getImage() == null) {
            holder.feedImage.setImageResource(R.drawable.rss);
        } else {
            Glide.with(mActivity).load(feed.getImage()).into(holder.feedImage);
        }
        holder.pos = position;
        holder.feedTitle.setText(feed.getTitle());
        holder.feedDescription.setText(feed.getDescription());
        holder.actionTextView.setOnClickListener(new OnRssFeedClick(holder));

        if (feed.getUserId() == null){
            holder.actionTextView.setText(convertView.getContext().getString(R.string.feed_subscribe));
            holder.actionTextView.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.cardview_subscribe_color));
        } else {
            holder.actionTextView.setText(convertView.getContext().getString(R.string.feed_unsubscribe));
            holder.actionTextView.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.cardview_unsubscribe_color));
        }

        return convertView;
    }

    private static class ViewHolder {
        public int pos;
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedDescription;
        public TextView actionTextView;
    }

    private class OnRssFeedClick implements View.OnClickListener {
        private ViewHolder mViewHolder;
        public OnRssFeedClick(ViewHolder viewHolder){
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            RSSFeed feed = getItem(mViewHolder.pos);
            if (feed.getUserId() == null){
                mUser.addFeed(feed);
                feed.setUserId(mUser.getId());
                mViewHolder.actionTextView.setText(v.getContext().getString(R.string.feed_unsubscribe));
                mViewHolder.actionTextView.setTextColor(ContextCompat.getColor(v.getContext(), R.color.cardview_unsubscribe_color));
            } else {
                feed.setUserId(null);
                mUser.removeFeed(feed);
                mViewHolder.actionTextView.setText(v.getContext().getString(R.string.feed_subscribe));
                mViewHolder.actionTextView.setTextColor(ContextCompat.getColor(v.getContext(), R.color.cardview_subscribe_color));
            }
            feed.update();

            try {
                ((OnMyFeedsChanged)mActivity).onMyFeedsChanged();
            } catch (ClassCastException e){
                e.printStackTrace();
            }
        }
    }
}
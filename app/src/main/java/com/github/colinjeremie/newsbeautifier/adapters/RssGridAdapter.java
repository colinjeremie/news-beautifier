package com.github.colinjeremie.newsbeautifier.adapters;

import android.app.Activity;
import android.content.Context;
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
    private int mLayoutResourceId;

    public RssGridAdapter(Activity activity, int layoutResourceId, List<RSSFeed> rssList){
        super(activity, layoutResourceId, rssList);

        mActivity = activity;
        mUser = ((MyApplication)mActivity.getApplication()).mUser;
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            holder.rssName = (TextView) convertView.findViewById(R.id.feedTitleTextView);
            holder.rssImage = (ImageView) convertView.findViewById(R.id.rssImage);
            holder.stateIcon = (ImageView) convertView.findViewById(R.id.stateIcon);
            holder.filter = convertView.findViewById(R.id.rssStateFilter);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        convertView.setOnClickListener(new OnRssFeedClick(holder));
        holder.pos = position;
        RSSFeed feed = getItem(position);
        if (feed.getImage() == null)
                holder.rssImage.setImageResource(R.drawable.rss);
        else
            Glide.with(mActivity).load(feed.getImage()).into(holder.rssImage);
        holder.rssName.setText(feed.getTitle());
        holder.stateIcon.setVisibility(feed.getUserId() == mUser.getId() ? View.VISIBLE : View.GONE);
        holder.filter.setVisibility(feed.getUserId() == mUser.getId() ? View.INVISIBLE : View.VISIBLE);

        convertView.setOnClickListener(new OnRssFeedClick(holder));
        return convertView;
    }

    private static class ViewHolder {
        public int pos;
        public ImageView rssImage;
        public ImageView stateIcon;
        public View filter;
        public TextView rssName;
    }

    private class OnRssFeedClick implements View.OnClickListener {
        private ViewHolder mViewHolder;
        public OnRssFeedClick(ViewHolder viewHolder){
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            RSSFeed feed = getItem(mViewHolder.pos);
            if (mViewHolder.stateIcon.getVisibility() == View.GONE) {
                mUser.addFeed(feed);
                feed.setUserId(mUser.getId());

                mViewHolder.stateIcon.setVisibility(View.VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.0f);
                fadeIn.setDuration(1000);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mViewHolder.filter.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mViewHolder.filter.startAnimation(fadeIn);
            } else {
                feed.setUserId(null);
                mUser.removeFeed(feed);
                mViewHolder.stateIcon.setVisibility(View.GONE);
                AlphaAnimation fadeOut = new AlphaAnimation(0.0f, 1.0f);
                fadeOut.setDuration(1000);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mViewHolder.filter.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mViewHolder.filter.startAnimation(fadeOut);
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
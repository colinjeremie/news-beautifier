package com.github.colinjeremie.newsbeautifier.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        super(activity, R.layout.grid_feed_item, rssList);

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
            convertView = inflater.inflate(R.layout.grid_feed_item, parent, false);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
            holder.feedDescription = (TextView) convertView.findViewById(R.id.feed_description);
            holder.actionTextView = (TextView) convertView.findViewById(R.id.cardview_action);
            holder.countArticles = (TextView) convertView.findViewById(R.id.feed_nb_articles);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.pos = position;
        holder.feedTitle.setText(feed.getTitle());
        holder.feedDescription.setText(feed.getDescription());

        int nb = feed.getItems().size();
        holder.countArticles.setText(convertView.getContext().getResources().getQuantityString(R.plurals.feed_nb_articles, nb, nb));
        holder.actionTextView.setOnClickListener(new OnRssFeedClick(holder));

        if (feed.getUserId() == null){
            holder.actionTextView.setText(convertView.getContext().getString(R.string.feed_subscribe));
            tintActionTextView(holder.actionTextView, R.color.cardview_subscribe_color);
        } else {
            holder.actionTextView.setText(convertView.getContext().getString(R.string.feed_unsubscribe));
            tintActionTextView(holder.actionTextView, R.color.cardview_unsubscribe_color);
        }

        return convertView;
    }

    private void tintActionTextView(TextView textView, int colorRes){
        textView.setTextColor(ContextCompat.getColor(textView.getContext(), colorRes));
        for (Drawable tmp : textView.getCompoundDrawables()){
            if (tmp != null){
                DrawableCompat.setTint(tmp, ContextCompat.getColor(textView.getContext(), colorRes));
            }
        }
        textView.requestLayout();
    }

    private static class ViewHolder {
        public int pos;
        public TextView feedTitle;
        public TextView feedDescription;
        public TextView actionTextView;
        public TextView countArticles;
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
                tintActionTextView(mViewHolder.actionTextView, R.color.cardview_unsubscribe_color);
            } else {
                feed.setUserId(null);
                mUser.removeFeed(feed);
                mViewHolder.actionTextView.setText(v.getContext().getString(R.string.feed_subscribe));
                tintActionTextView(mViewHolder.actionTextView, R.color.cardview_subscribe_color);
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
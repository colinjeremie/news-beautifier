package com.github.colinjeremie.newsbeautifier.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Adapter used in {@link com.github.colinjeremie.newsbeautifier.fragments.FeedSelectFragment}
 */
public class RssGridAdapter extends ArrayAdapter<RSSFeed> {

    /**
     * The mode of the display
     */
    @IntDef({MODE_LIST, MODE_GRID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayMode {}

    /**
     * If there is a list view
     */
    public static final int MODE_LIST = 1;

    /**
     * If there is a grid view
     */
    public static final int MODE_GRID = 2;

    /**
     * The current mode
     */
    private Integer mMode = MODE_LIST;

    /**
     * The user of the app
     */
    private User mUser;

    /**
     * The activity associated with the Adapter
     */
    private Activity mActivity;


    /**
     * Constructor
     *
     * @param activity Activity
     * @param rssList The data used
     */
    public RssGridAdapter(Activity activity, List<RSSFeed> rssList){
        super(activity, R.layout.list_feed_item, rssList);

        mActivity = activity;
        mUser = ((MyApplication)mActivity.getApplication()).mUser;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RSSFeed feed = getItem(position);

        if (convertView != null){
            holder = (ViewHolder)convertView.getTag();
        }
        if (convertView == null || holder.mode != mMode) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (mMode == MODE_GRID) {
                convertView = inflater.inflate(R.layout.grid_feed_item, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.list_feed_item, parent, false);
            }
            holder.feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
            holder.feedDescription = (TextView) convertView.findViewById(R.id.feed_description);
            holder.actionTextView = (TextView) convertView.findViewById(R.id.cardview_action);
            holder.countArticles = (TextView) convertView.findViewById(R.id.feed_nb_articles);
            holder.mode = mMode;
            convertView.setTag(holder);
        }

        holder.pos = position;
        holder.feedTitle.setText(feed.getTitle());
        holder.feedDescription.setText(feed.getDescription());

        int nb = feed.getItems().size();
        if (holder.countArticles != null) {
            holder.countArticles.setText(convertView.getContext().getResources().getQuantityString(R.plurals.feed_nb_articles, nb, nb));
        }
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

    /**
     * Tint the <code>textView</code> with the color res id <code>colorRes</code>
     *
     * @param textView the view to set the tint
     * @param colorRes the color res id used to tint
     */
    private void tintActionTextView(TextView textView, int colorRes) {
        textView.setTextColor(ContextCompat.getColor(textView.getContext(), colorRes));
        for (Drawable tmp : textView.getCompoundDrawables()){
            if (tmp != null){
                DrawableCompat.setTint(tmp, ContextCompat.getColor(textView.getContext(), colorRes));
            }
        }
        textView.requestLayout();
    }

    /**
     * View holder
     */
    private static class ViewHolder {
        public int pos;
        public TextView feedTitle;
        public TextView feedDescription;
        public TextView actionTextView;
        public TextView countArticles;
        public @DisplayMode int mode = MODE_LIST;
    }

    /**
     * Click listener on an item
     */
    private class OnRssFeedClick implements View.OnClickListener {

        /**
         * The ViewHolder of the item
         */
        private ViewHolder mViewHolder;

        /**
         * Constructor
         *
         * @param viewHolder ViewHolder
         */
        public OnRssFeedClick(ViewHolder viewHolder){
            mViewHolder = viewHolder;
        }

        /**
         * Add or remove the feed from the {@link User} feed list
         * We tint the {@link ViewHolder#actionTextView} to the right color depending if it's a user's feed or not
         *
         * @param v View
         */
        @Override
        public void onClick(View v) {
            RSSFeed feed = getItem(mViewHolder.pos);
            if (feed.getUserId() == null) {
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

    /**
     * Mode List or Grid
     * @param pMode int
     */
    public void setMode(@DisplayMode int pMode) {
        mMode = pMode;
    }
}
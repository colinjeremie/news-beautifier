package com.github.colinjeremie.newsbeautifier.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Service used to manage the different views of the Stack widget
 * Created by jerem_000 on 3/1/2016.
 */
public class StackWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext());
    }

    /**
     * Factory to create the views of the widget based on the data {@link StackRemoteViewsFactory#mWidgetItems
     */
    public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private int mCount = 10;
        private List<RSSItem> mWidgetItems = new ArrayList<>();
        private Context mContext;

        public StackRemoteViewsFactory(Context context) {
            mContext = context;
        }

        /**
         * On the creation we fetched our data from the {@link MyApplication}
         * We take the last articles with pictures, the limit is {@link #mCount}
         */
        public void onCreate() {
            mWidgetItems = ((MyApplication) mContext.getApplicationContext()).mUser.getLastArticlesWithPicture(mCount);
            mCount = mWidgetItems.size();
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mCount;
        }

        /**
         * Create the view for an article
         * We set an fill intent {@link Intent} for each {@link RSSItem} when an item is clicked
         *
         * @param position int
         * @return the article's view
         */
        @Override
        public RemoteViews getViewAt(int position) {
            RSSItem model = mWidgetItems.get(position);

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            rv.setTextViewText(R.id.widget_title_item, model.getTitle());

            if (!model.getImage().isEmpty()) {
                Bitmap bitmap = getImageBitmap(model.getImage());
                if (bitmap != null) {
                    rv.setImageViewBitmap(R.id.widget_image_item, bitmap);
                }
            }

            Bundle extras = new Bundle();
            extras.putParcelable(MyAppWidgetProvider.EXTRA_ITEM, model);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);

            rv.setOnClickFillInIntent(R.id.widget_item_root_layout, fillInIntent);

            return rv;
        }

        /**
         * Download and transform the image pointed by <code>url</code>
         *
         * @param url String the url of the image to download
         * @return the {@link Bitmap} created
         */
        private Bitmap getImageBitmap(String url) {
            try {
                URL aURL = new URL(url);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                Bitmap bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
                return bm;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}

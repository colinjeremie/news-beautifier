package com.github.colinjeremie.newsbeautifier.widget;

import android.appwidget.AppWidgetManager;
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
 * * NewsBeautifier
 * Created by jerem_000 on 3/1/2016.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private int mCount = 10;
        private List<RSSItem> mWidgetItems = new ArrayList<>();
        private Context mContext;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

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

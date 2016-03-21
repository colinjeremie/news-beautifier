package com.github.colinjeremie.newsbeautifier.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.activities.ArticleActivity;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.utils.MyRequestQueue;
import com.github.colinjeremie.newsbeautifier.utils.RSSParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 3/1/2016.
 */
public class MyAppWidgetProvider extends AppWidgetProvider {
    public static final String ACTION = "com.github.colinjeremie.newsbeautifier.widget.MyAppWidgetProvider.ACTION";
    public static final String EXTRA_ITEM = "com.github.colinjeremie.newsbeautifier.widget.MyAppWidgetProvider.EXTRA_ITEM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent intent2 = new Intent(context, ArticleActivity.class);
            intent2.putExtra(ArticleActivity.ARTICLE, intent.getParcelableExtra(EXTRA_ITEM));
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(ArticleActivity.class);
            stackBuilder.addNextIntent(intent2);
            stackBuilder.startActivities();
        }
        super.onReceive(context, intent);
    }

    @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        refreshUserFeeds(context, appWidgetManager, appWidgetIds);
    }

    private void refreshUserFeeds(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final List<RSSFeed> feeds = ((MyApplication) context.getApplicationContext()).mUser.getFeeds();

        // update with the previous articles
        updateWidgets(context, appWidgetManager, appWidgetIds);

        // refresh from internet
        if (feeds.size() > 0){
            for (final RSSFeed feed : feeds) {
                MyRequestQueue.getInstance(context).addToRequestQueue(getFeedStringRequest(feed,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                                    RSSParser.getInstance().readRssFeed(stream, feed);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (feeds.lastIndexOf(feed) == feeds.size() - 1) {
                                    updateWidgets(context, appWidgetManager, appWidgetIds);
                                }
                            }
                        }));
            }
        }
    }

    private void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StackWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.stack_view, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            Intent intent2 = new Intent(context, MyAppWidgetProvider.class);

            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            intent2.setAction(MyAppWidgetProvider.ACTION);
            intent2.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            rv.setPendingIntentTemplate(R.id.stack_view, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private StringRequest getFeedStringRequest(final RSSFeed feed, Response.Listener<String> listener){
        return new StringRequest(Request.Method.GET, feed.getUrl(), listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }
}

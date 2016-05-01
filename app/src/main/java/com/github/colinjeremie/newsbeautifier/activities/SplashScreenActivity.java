package com.github.colinjeremie.newsbeautifier.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.utils.MyRequestQueue;
import com.github.colinjeremie.newsbeautifier.utils.RSSParser;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    /**
     * Delay time to keep the activity alive in ms
     */
    public static final long DELAY_TIME = 2000;

    /**
     * Init the blogs and articles from {@link RSSParser#RSS_FEEDS }
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        List<RSSFeed> feeds = new Select().from(RSSFeed.class).queryList();
        addNewFeeds(feeds, Arrays.asList(RSSParser.RSS_FEEDS));

        for (RSSFeed feed : feeds){
            MyRequestQueue.getInstance(this).addToRequestQueue(getFeedStringRequest(feed));
        }
    }

    /**
     * Launch the {@link HomeActivity} after {@link #DELAY_TIME} milliseconds
     */
    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, DELAY_TIME);
    }

    private StringRequest getFeedStringRequest(final RSSFeed feed){
        return new StringRequest(Request.Method.GET, feed.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                            RSSParser.getInstance().readRssFeed(stream, feed);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private void addNewFeeds(List<RSSFeed> feeds, List<RSSFeed> defRssFeeds) {
        for (RSSFeed tmp : defRssFeeds){
            boolean added = true;
            for (RSSFeed tmp2 : feeds){
                if (tmp2.getUrl().equals(tmp.getUrl())){
                    added = false;
                    break;
                }
            }
            if (added){
                feeds.add(tmp);
            }
        }
    }
}

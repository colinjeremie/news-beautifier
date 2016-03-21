package com.github.colinjeremie.newsbeautifier.utils;

import android.os.AsyncTask;

import com.github.colinjeremie.newsbeautifier.models.RSSFeed;

import java.io.InputStream;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/18/2016.
 */
public class UpdateRSSFeedTask extends AsyncTask<RSSFeed, Boolean, RSSFeed> {

    @Override
    protected RSSFeed doInBackground(RSSFeed... params) {
        if (params.length == 0){
            return new RSSFeed();
        }
        InputStream in = RSSParser.getInstance().readFromUrl(params[0].getUrl());
        if (in == null){
            return params[0];
        }

        return RSSParser.getInstance().readRssFeed(in, params[0]);
    }
}

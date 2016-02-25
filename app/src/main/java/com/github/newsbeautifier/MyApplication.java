package com.github.newsbeautifier;

import android.app.Application;

import com.github.newsbeautifier.models.RSSFeed;
import com.github.newsbeautifier.models.User;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.ArrayList;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/15/2016.
 */
public class MyApplication extends Application {

    public User mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);

        mUser = new User();

        if (mUser.getFeeds() == null){
            mUser.setFeeds(new ArrayList<RSSFeed>());
        }
    }
}

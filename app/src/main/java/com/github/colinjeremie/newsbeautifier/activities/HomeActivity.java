package com.github.colinjeremie.newsbeautifier.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment;
import com.github.colinjeremie.newsbeautifier.fragments.FeedSelectFragment;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;
import com.github.colinjeremie.newsbeautifier.utils.OnMyFeedsChanged;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements OnMyFeedsChanged {

    public static final String POSITION = "POSITION";
    public static final String LIST_TYPE = "LIST_TYPE";
    private static final int HEADER_TYPE = 0;
    private static final int FEED_TYPE = 1;

    private DrawerLayout mDrawerLayout;
    private ListView mHeaderList;
    private ListView mFeedListView;
    private FeedAdapter mFeedAdapter;
    private String[] mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mHeaderList = (ListView) navigationView.getHeaderView(0).findViewById(R.id.left_drawer);
        mFeedListView = (ListView) findViewById(R.id.feed_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (mDrawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
        mTitles = getResources().getStringArray(R.array.nav_items);
        mHeaderList.setAdapter(new ArrayAdapter<>(this,
                R.layout.header_item, mTitles));

        mHeaderList.setOnItemClickListener(new HeaderItemClickListener());

        ((ListView) findViewById(R.id.settings_list_view)).setOnItemClickListener(settingsItemClickListener);

        initMyFeeds();

        restoreState(savedInstanceState);
    }

    private void restoreState(Bundle savedInstanceState){
        int position = 0;
        int type = HEADER_TYPE;

        if (savedInstanceState != null){
            position = savedInstanceState.getInt(POSITION);
            type = savedInstanceState.getInt(LIST_TYPE);
        }
        if (type == HEADER_TYPE){
            selectHeaderItem(position);
        } else {
            selectFeed(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mHeaderList.getSelectedItemPosition() != AdapterView.INVALID_POSITION){
            outState.putInt(POSITION, mHeaderList.getSelectedItemPosition());
            outState.putInt(LIST_TYPE, HEADER_TYPE);
        } else if (mFeedListView.getSelectedItemPosition() != AdapterView.INVALID_POSITION){
            outState.putInt(POSITION, mFeedListView.getSelectedItemPosition());
            outState.putInt(LIST_TYPE, FEED_TYPE);
        }
        super.onSaveInstanceState(outState);
    }

    private AdapterView.OnItemClickListener settingsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0){
                sendFeedBack();
            } else if (position == 1){
                startAboutActivity();
            }
        }
    };

    private void startAboutActivity() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void sendFeedBack() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, getResources().getStringArray(R.array.email_contacts));
        Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_title));
        startActivity(Intent.createChooser(Email, getString(R.string.feedback_send_title)));
    }

    private void initMyFeeds() {
        mFeedAdapter = new FeedAdapter();
        mFeedListView.setAdapter(mFeedAdapter);
        mFeedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFeed(position);
            }
        });
    }

    private void selectFeed(int position) {
        if (position != mFeedListView.getSelectedItemPosition()) {
            RSSFeed feed = mFeedAdapter.getItem(position);
            mHeaderList.clearChoices();
            mHeaderList.requestLayout();
            mFeedListView.setItemChecked(position, true);

            ArrayList<RSSItem> articles = new ArrayList<>();
            articles.addAll(feed.getItems());

            DisplayArticlesFragment fragment = new DisplayArticlesFragment();
            Bundle bundle = new Bundle();

            bundle.putString(DisplayArticlesFragment.FEED_URL, feed.getUrl());
            bundle.putLong(DisplayArticlesFragment.FEED_USER_ID, feed.getUserId());
            bundle.putParcelableArrayList(DisplayArticlesFragment.ARTICLES, articles);
            fragment.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            setTitle(feed.toString());
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMyFeedsChanged() {
            mFeedAdapter.notifyDataSetChanged();
    }

    private class HeaderItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectHeaderItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectHeaderItem(int position) {
        if (position != mHeaderList.getSelectedItemPosition()) {
            Fragment fragment = null;

            if (position == 0) {
                fragment = new DisplayArticlesFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(DisplayArticlesFragment.ARTICLES, getAllArticles());
                fragment.setArguments(bundle);
            } else if (position == 1) {
                fragment = new FeedSelectFragment();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mHeaderList.setItemChecked(position, true);
            mFeedListView.clearChoices();
            mFeedListView.requestLayout();

            if (position >= 0 && position < mTitles.length) {
                setTitle(mTitles[position]);
            } else {
                setTitle(getString(R.string.app_name));
            }
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private ArrayList<RSSItem> getAllArticles(){
        List<RSSFeed> feedList = ((MyApplication)(getApplication())).mUser.getFeeds();
        ArrayList<RSSItem> articleList = new ArrayList<>();

        for (RSSFeed feed : feedList) {
            articleList.addAll(feed.getItems());
        }
        return articleList;
    }

    private class FeedAdapter extends ArrayAdapter<RSSFeed>{
        public FeedAdapter(){
            super(HomeActivity.this, R.layout.menu_feed_item, ((MyApplication)getApplication()).mUser.getFeeds());
        }
    }
}

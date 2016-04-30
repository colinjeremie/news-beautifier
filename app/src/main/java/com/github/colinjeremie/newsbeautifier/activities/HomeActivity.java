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

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment;
import com.github.colinjeremie.newsbeautifier.fragments.FeedSelectFragment;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;
import com.github.colinjeremie.newsbeautifier.utils.OnMyFeedsChanged;

import java.util.ArrayList;
import java.util.List;

/**
 * The main activity
 */
public class HomeActivity extends AppCompatActivity implements OnMyFeedsChanged {

    /**
     * The Feed position selected
     */
    public static final String POSITION = "com.github.colinjeremie.newsbeautifier.activities.POSITION";

    /**
     * Header position
     */
    public static final String LIST_TYPE = "com.github.colinjeremie.newsbeautifier.activities.LIST_TYPE";

    /**
     * Header type clicked
     */
    private static final int HEADER_TYPE = 0;

    /**
     * Feed type clicked
     */
    private static final int FEED_TYPE = 1;

    /**
     * The drawer menu
     */
    private DrawerLayout mDrawerLayout;

    /**
     * The Header list
     */
    private ListView mHeaderList;

    /**
     * The {@link com.github.colinjeremie.newsbeautifier.models.User#feeds}
     */
    private ListView mFeedListView;

    /**
     * The adapter for the {@link #mFeedListView}
     */
    private FeedAdapter mFeedAdapter;

    /**
     * Title of the menu sections
     */
    private String[] mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (navigationView != null && navigationView.getHeaderView(0) != null) {
            mHeaderList = (ListView) navigationView.getHeaderView(0).findViewById(R.id.left_drawer);
        }
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

        ListView settingsListView = (ListView) findViewById(R.id.settings_list_view);
        if (settingsListView != null) {
            settingsListView.setOnItemClickListener(settingsItemClickListener);
        }

        initMyFeeds();

        if (savedInstanceState == null){
            selectHeaderItem(0, true);
        }
    }

    /**
     * Select the previous item
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int position = 0;
        int type = HEADER_TYPE;

        if (savedInstanceState != null){
            position = savedInstanceState.getInt(POSITION);
            type = savedInstanceState.getInt(LIST_TYPE);
        }
        if (type == HEADER_TYPE){
            selectHeaderItem(position, false);
        } else {
            selectFeed(position, false);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Save the current items from the menu selected
     *
     * @param outState Bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int headerPos = mHeaderList.getCheckedItemPosition();
        int feedPos = mFeedListView.getCheckedItemPosition();

        if (headerPos != AdapterView.INVALID_POSITION){
            outState.putInt(POSITION, headerPos);
            outState.putInt(LIST_TYPE, HEADER_TYPE);
        } else if (feedPos != AdapterView.INVALID_POSITION){
            outState.putInt(POSITION, feedPos);
            outState.putInt(LIST_TYPE, FEED_TYPE);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * OnClickListener for the settings ListView
     */
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
        String subject = getString(R.string.feedback_email_title);
        subject += " v" + BuildConfig.VERSION_NAME + "r-" + BuildConfig.VERSION_CODE;

        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, getResources().getStringArray(R.array.email_contacts));
        Email.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(Email, getString(R.string.feedback_send_title)));
    }

    /**
     * Init the display of the view
     */
    private void initMyFeeds() {
        mFeedAdapter = new FeedAdapter();
        mFeedListView.setAdapter(mFeedAdapter);
        mFeedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFeed(position, true);
            }
        });
    }

    /**
     * Select a feed and refresh the FrameLayout
     *
     * @param position int the position of the feed in the menu list
     * @param mustInstantiateFragment boolean
     */
    private void selectFeed(int position, boolean mustInstantiateFragment) {
        if (position >= 0 && position < mFeedAdapter.getCount()) {
            RSSFeed feed = mFeedAdapter.getItem(position);
            if (position != mFeedListView.getSelectedItemPosition() && mustInstantiateFragment) {
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
            }
            mHeaderList.clearChoices();
            mHeaderList.requestLayout();
            mFeedListView.setItemChecked(position, true);

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

    /**
     * Callback when a User feed changed its state
     */
    @Override
    public void onMyFeedsChanged() {
        mFeedAdapter.notifyDataSetChanged();
    }

    /**
     * Click on a Header item
     */
    private class HeaderItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectHeaderItem(position, true);
        }
    }

    /**
     * Swaps fragments in the main content view
     *
     * @param position int
     * @param mustInstantiateFragment boolean
     */
    private void selectHeaderItem(int position, boolean mustInstantiateFragment) {
        if (position >= 0 &&
                position != mHeaderList.getSelectedItemPosition() && mustInstantiateFragment) {
            Fragment fragment;

            if (position == 0) {
                fragment = new DisplayArticlesFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(DisplayArticlesFragment.ARTICLES, getAllArticles());
                fragment.setArguments(bundle);
            } else {
                fragment = new FeedSelectFragment();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

        }
        // Highlight the selected item, update the title, and close the drawer
        mHeaderList.setItemChecked(position, true);
        mFeedListView.clearChoices();
        mFeedListView.requestLayout();

        if (position >= 0 && position < mTitles.length) {
            setTitle(mTitles[position]);
        } else {
            setTitle(getString(R.string.app_name));
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Get all {@link com.github.colinjeremie.newsbeautifier.models.User#feeds}
     *
     * @return the {@link com.github.colinjeremie.newsbeautifier.models.User#feeds}
     */
    private ArrayList<RSSItem> getAllArticles(){
        List<RSSFeed> feedList = ((MyApplication)(getApplication())).mUser.getFeeds();
        ArrayList<RSSItem> articleList = new ArrayList<>();

        for (RSSFeed feed : feedList) {
            articleList.addAll(feed.getItems());
        }
        return articleList;
    }

    /**
     * Adapter to display the {@link RSSFeed} in the menu
     */
    private class FeedAdapter extends ArrayAdapter<RSSFeed>{
        public FeedAdapter(){
            super(HomeActivity.this, R.layout.menu_feed_item, ((MyApplication)getApplication()).mUser.getFeeds());
        }
    }
}

package com.github.colinjeremie.newsbeautifier.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.adapters.StaggeredRecyclerViewAdapter;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;
import com.github.colinjeremie.newsbeautifier.utils.MyRequestQueue;
import com.github.colinjeremie.newsbeautifier.utils.RSSParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment used to display multiple Articles
 */
public class DisplayArticlesFragment extends Fragment implements SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener,
        PopupMenu.OnMenuItemClickListener {

    public static final String SEARCH_TEXT = "com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment.SEARCH_TEXT";
    public static final String FILTER = "com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment.FILTER";

    public static final String FEED_URL = "com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment.FEED_URL";
    public static final String FEED_USER_ID = "com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment.FEED_USER_ID";
    public static final String ARTICLES = "com.github.colinjeremie.newsbeautifier.fragments.DisplayArticlesFragment.ARTICLES";

    /**
     * The view which displays the articles
     */
    private RecyclerView mRecyclerView;

    /**
     * The adapter for the {@link #mRecyclerView}
     */
    private StaggeredRecyclerViewAdapter mAdapter;

    /**
     * The data to display retrieved from the {@link Fragment#getArguments()}
     */
    private ArrayList<RSSItem> mArticleList;

    /**
     * The feed url associated with the {@link #mArticleList}
     */
    private String mFeedUrl;

    /**
     * If the articles are associated with a UserId
     */
    private Long mFeedUserId;

    /**
     * A {@link SwipeRefreshLayout} to refresh the articles with a pull down
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Filter from the {@link android.support.v7.app.ActionBar}
     */
    private String textSearched;

    /**
     * The position of the filter which was clicked
     * Used we the fragment is recreated
     */
    private int mFilterPosition = -1;

    public DisplayArticlesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display_articles, container, false);

        if (savedInstanceState == null) {
            mArticleList = getArguments().getParcelableArrayList(ARTICLES);
            Collections.sort(mArticleList, new RSSItem.DateComparatorAsc());
        } else {
            mArticleList = savedInstanceState.getParcelableArrayList(ARTICLES);
        }
        mFeedUrl = getArguments().getString(FEED_URL, null);
        mFeedUserId = getArguments().getLong(FEED_USER_ID, -1);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        mAdapter = new StaggeredRecyclerViewAdapter(getActivity(), mArticleList);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        setHasOptionsMenu(true);

        if (savedInstanceState != null){
            textSearched = savedInstanceState.getString(SEARCH_TEXT, null);
            mFilterPosition = savedInstanceState.getInt(FILTER, -1);
        }
        return v;
    }

    /**
     * We save the text searched and the position of the filter
     *
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (textSearched != null){
            outState.putString(SEARCH_TEXT, textSearched);
        }
        outState.putParcelableArrayList(ARTICLES, mArticleList);
        outState.putInt(FILTER, mFilterPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * Create the menu
     * Bind the {@link SearchView} and set the query if there was a text searched before
     * @param menu Menu
     * @param inflater MenuInflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.articles_search_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        if (textSearched != null && !textSearched.isEmpty()){
            String tmp = textSearched;
            item.expandActionView();
            searchView.setQuery(tmp, true);
            searchView.clearFocus();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * When there is a new input we filter {@link #filter(List, String)} the list with the <code>newText</code>
     * @param newText The text to search in the {@link RSSItem#title}
     * @return boolean
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        textSearched = newText;
        final List<RSSItem> filteredModelList = filter(mArticleList, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    /**
     * Filter the title of the list {@link RSSItem#title} with the <code>query</code>
     * We normalize the texts to create a better match
     *
     * @param models List<RSSItem>
     * @param query String the text searched
     * @return the list filtered
     */
    private List<RSSItem> filter(List<RSSItem> models, String query) {
        query = Normalizer.normalize(query, Normalizer.Form.NFD);
        query = query.replaceAll("[^\\p{ASCII}]", "").toLowerCase();

        final List<RSSItem> filteredModelList = new ArrayList<>();
        for (RSSItem model : models) {
            String text = model.getTitle();
            text = Normalizer.normalize(text, Normalizer.Form.NFD);
            text = text.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
            if (text.toLowerCase().contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.filter:
                showPopup(item.getItemId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Listener from the {@link SwipeRefreshLayout}
     */
    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (mFeedUrl == null){
            refreshUserFeeds();
        } else {
            refreshThisFeed();
        }
    }

    /**
     * Callback when the refresh is done
     */
    private void refreshDone() {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Refresh the feed associated with the {@link #mFeedUrl}
     */
    private void refreshThisFeed() {
        final RSSFeed feed = new RSSFeed(mFeedUrl);
        feed.setUserId(mFeedUserId == -1 ? null : mFeedUserId);

        mArticleList = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, feed.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                            RSSParser.getInstance().readRssFeed(stream, feed);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mArticleList.addAll(feed.getItems());
                        refreshDone();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mArticleList.addAll(feed.getItems());
                refreshDone();
            }
        });
        MyRequestQueue.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    /**
     * We refresh the user feeds
     */
    private void refreshUserFeeds() {
        final List<RSSFeed> feeds = ((MyApplication) getActivity().getApplication()).mUser.getFeeds();
        final ArrayList<RSSItem> newItems = new ArrayList<>();

        for (final RSSFeed feed : feeds){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, feed.getUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                                RSSParser.getInstance().readRssFeed(stream, feed);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            newItems.addAll(feed.getItems());
                            if (feeds.lastIndexOf(feed) == feeds.size() - 1) {
                                mArticleList = newItems;
                                refreshDone();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (feeds.lastIndexOf(feed) == feeds.size() - 1) {
                        mArticleList = newItems;
                        refreshDone();
                    }
                }
            });
            MyRequestQueue.getInstance(getActivity()).addToRequestQueue(stringRequest);
        }
    }

    /**
     * Create an overflow popup when we clicked on a menu item identified by <code>id</code>
     * @param id the menu item id
     */
    public void showPopup(int id) {
        View view = this.getActivity().findViewById(id);
        if (view != null) {
            PopupMenu popup = new PopupMenu(getActivity(), view);
            popup.setOnMenuItemClickListener(this);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.sort_menu, popup.getMenu());
            popup.show();
        }
    }

    /**
     * Listener for the new menu popup created from {@link #showPopup(int)}
     * @param item The {@link MenuItem} clicked
     * @return boolean
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_date_asc:
                mFilterPosition = 0;
                sortList(new RSSItem.DateComparatorAsc());
                return true;
            case R.id.sort_date_desc:
                mFilterPosition = 1;
                sortList(new RSSItem.DateComparatorDesc());
                return true;
            case R.id.sort_title_asc:
                mFilterPosition = 2;
                sortList(new RSSItem.TitleComparatorAsc());
                return true;
            case R.id.sort_title_desc:
                mFilterPosition = 3;
                sortList(new RSSItem.TitleComparatorDesc());
                return true;
        }
        return false;
    }

    /**
     * We sort the current list with the compartor
     * @param comparator the comparator used to sort the list
     */
    @SuppressWarnings("unchecked")
    private void sortList(final Comparator<RSSItem> comparator){
        new AsyncTask<List<RSSItem>, Void, Void>() {
            @Override
            protected Void doInBackground(List<RSSItem>... params) {
                for (List<RSSItem> tmp : params) {
                    Collections.sort(tmp, comparator);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (!isDetached() && isVisible()) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }.execute(mArticleList, mAdapter.getitemList());
    }
}

package com.github.colinjeremie.newsbeautifier.fragments;

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

public class DisplayArticlesFragment extends Fragment implements SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener,
        PopupMenu.OnMenuItemClickListener {

    public static final String FEED_URL = "FEED_URL";
    public static final String FEED_USER_ID = "FEED_USER_ID";
    public static final String ARTICLES = "ARTICLES";

    private RecyclerView mRecyclerView;
    private StaggeredRecyclerViewAdapter mAdapter;
    private List<RSSItem> mArticleList;
    private String mFeedUrl;
    private Long mFeedUserId;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public DisplayArticlesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display_articles, container, false);

        mArticleList = getArguments().getParcelableArrayList(ARTICLES);
        Collections.sort(mArticleList, new RSSItem.DateComparatorAsc());
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

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<RSSItem> filteredModelList = filter(mArticleList, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

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

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (mFeedUrl == null){
            refreshUserFeeds();
        } else {
            refreshThisFeed();
        }
    }

    private void refreshDone() {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.notifyDataSetChanged();
    }

    private void refreshThisFeed() {
        final RSSFeed feed = new RSSFeed(mFeedUrl);
        feed.setUserId(mFeedUserId == -1 ? null : mFeedUserId);

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
                        mArticleList = feed.getItems();
                        refreshDone();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mArticleList = feed.getItems();
                refreshDone();
            }
        });
        MyRequestQueue.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void refreshUserFeeds() {
        final List<RSSFeed> feeds = ((MyApplication) getActivity().getApplication()).mUser.getFeeds();
        final List<RSSItem> newItems = new ArrayList<>();

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

    public void showPopup(int id) {
        PopupMenu popup = new PopupMenu(this.getActivity(), this.getActivity().findViewById(id));
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_menu, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sort_date_asc:
                sortList(new RSSItem.DateComparatorAsc());
                return true;
            case R.id.sort_date_desc:
                sortList(new RSSItem.DateComparatorDesc());
                return true;
            case R.id.sort_title_asc:
                sortList(new RSSItem.TitleComparatorAsc());
                return true;
            case R.id.sort_title_desc:
                sortList(new RSSItem.TitleComparatorDesc());
                return true;
            case R.id.sort_language:
                sortList(new RSSItem.LanguageComparator());
                return true;
            case R.id.sort_category:
                sortList(new RSSItem.CategoryComparator());
                return true;
        }
        return false;
    }

    private void sortList(Comparator comparator){
        Collections.sort(mAdapter.getitemList(), comparator);
        mAdapter.notifyDataSetChanged();
    }
}

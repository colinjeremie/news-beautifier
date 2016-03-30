package com.github.colinjeremie.newsbeautifier.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.colinjeremie.newsbeautifier.MyApplication;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.adapters.RssGridAdapter;
import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.User;
import com.github.colinjeremie.newsbeautifier.utils.MyRequestQueue;
import com.github.colinjeremie.newsbeautifier.utils.OnMyFeedsChanged;
import com.github.colinjeremie.newsbeautifier.utils.RSSParser;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FeedSelectFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    public static final String FEEDS = "com.github.colinjeremie.newsbeautifier.fragments.FeedSelectFragment.FEEDS";

    private ArrayList<RSSFeed> mRssList = new ArrayList<>();
    private RssGridAdapter mRssGridAdapter;
    private TextInputLayout textInputLayout;
    private AlertDialog dialog;
    private MenuItem mGridMenuItem;
    private MenuItem mListMenuItem;
    private GridView mRssGridView;

    public FeedSelectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_feed_select, container, false);

        mRssGridView = (GridView) inflatedView.findViewById(R.id.rssGridView);

        if (savedInstanceState == null) {
            mRssList = new ArrayList<>(new Select()
                    .from(RSSFeed.class).queryList());
        } else {
            mRssList = savedInstanceState.getParcelableArrayList(FEEDS);
        }
        mRssGridAdapter = new RssGridAdapter(getActivity(), mRssList);
        mRssGridView.setAdapter(mRssGridAdapter);

        inflatedView.findViewById(R.id.btn_add_rss_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFeed();
            }
        });

        setHasOptionsMenu(true);
        return inflatedView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(FEEDS, mRssList);
        super.onSaveInstanceState(outState);
    }

    public interface OnAddRssListener {
        void onURLEntered(String url);
    }

    private void createAddRssDialog(final OnAddRssListener listener){
        textInputLayout = (TextInputLayout) LayoutInflater.from(getActivity()).inflate(R.layout.add_rss_feed_dialog, null);
        final EditText editText = (EditText) textInputLayout.findViewById(R.id.edit_text);
        textInputLayout.setErrorEnabled(true);

        final DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = editText.getText().toString();
                if (url.isEmpty() || !URLUtil.isValidUrl(url)) {
                    textInputLayout.setError(getString(R.string.url_cannot_be_empty));
                } else {
                    textInputLayout.setError(null);
                    listener.onURLEntered(url);
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle(getString(R.string.dialog_add_rss_title));
        builder.setIcon(null);
        builder.setView(textInputLayout);
        builder.setPositiveButton(android.R.string.ok, listener1);
        dialog = builder.create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener1.onClick(dialog, AlertDialog.BUTTON_POSITIVE);
            }
        });
    }

    private void createNewFeed() {
        createAddRssDialog(new OnAddRssListener() {
            @Override
            public void onURLEntered(String url) {
                final RSSFeed feed = new RSSFeed(url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, feed.getUrl(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                textInputLayout.setErrorEnabled(true);
                                try {
                                    InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                                    if (RSSParser.getInstance().readRssFeed(stream, feed) != null) {
                                        if (isFeedIsntAlreadyAdded(feed)) {
                                            User user = ((MyApplication) (getActivity().getApplication())).mUser;
                                            feed.setUserId(user.getId());
                                            feed.update();
                                            mRssGridAdapter.add(feed);
                                            try {
                                                user.addFeed(feed);
                                                ((OnMyFeedsChanged)getActivity()).onMyFeedsChanged();
                                            } catch (ClassCastException e){
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(getActivity(), getString(R.string.your_feed_has_been_added), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            textInputLayout.setError(getString(R.string.error_this_feed_already_exist));
                                        }
                                    } else {
                                        textInputLayout.setError(getString(R.string.error_this_feed_is_not_a_valid_rss_feed));
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                    textInputLayout.setError(getString(R.string.error_this_feed_cannot_be_added));
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), getString(R.string.error_an_error_hapenned_with_this_feed), Toast.LENGTH_SHORT).show();
                    }
                });
                MyRequestQueue.getInstance(getActivity()).addToRequestQueue(stringRequest);
            }
        });
    }

    private boolean isFeedIsntAlreadyAdded(RSSFeed feed) {
        for (RSSFeed tmp : mRssList){
            if (tmp.getUrl().equals(feed.getUrl()) ||
                    tmp.getLink().equals(feed.getLink())){
                return false;
            }
        }
        return true;
    }

    private void sortFeeds(final Comparator<RSSFeed> comparator){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void ... params) {
                Collections.sort(mRssList, comparator);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (!isDetached() && isVisible()) {
                    mRssGridAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mGridMenuItem = menu.findItem(R.id.display_grid_action);
        mListMenuItem = menu.findItem(R.id.display_list_action);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_menu, menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_more_articles:
                sortFeeds(new RSSFeed.ComparatorArticleCountDesc());
                return true;
            case R.id.sort_less_articles:
                sortFeeds(new RSSFeed.ComparatorArticleCountAsc());
                return true;
            case R.id.sort_feed_subscribed:
                sortFeeds(new RSSFeed.ComparatorSubscription());
                return true;
            case R.id.sort_feed_unsubscribed:
                sortFeeds(new RSSFeed.ComparatorUnSubscription());
                return true;
        }
        return false;
    }

    public void showOverFlowMenu(int id) {
        View view = this.getActivity().findViewById(id);
        if (view != null) {
            PopupMenu popup = new PopupMenu(getActivity(), view);
            popup.setOnMenuItemClickListener(this);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.overflow_feed_sort_menu, popup.getMenu());
            popup.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort_action){
            showOverFlowMenu(item.getItemId());
            return true;
        } else if (item.getItemId() == R.id.display_grid_action){
            mRssGridView.setNumColumns(2);
            mGridMenuItem.setVisible(false);
            mListMenuItem.setVisible(true);
            mRssGridAdapter.setMode(RssGridAdapter.MODE_GRID);
            mRssGridAdapter.notifyDataSetInvalidated();
            return true;
        } else if (item.getItemId() == R.id.display_list_action){
            mRssGridView.setNumColumns(1);
            mGridMenuItem.setVisible(true);
            mListMenuItem.setVisible(false);
            mRssGridAdapter.setMode(RssGridAdapter.MODE_LIST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


package com.github.colinjeremie.newsbeautifier.utils;

/**
 * Listener when a {@link com.github.colinjeremie.newsbeautifier.models.RSSFeed} has been added or removed from the {@link com.github.colinjeremie.newsbeautifier.models.User#feeds}
 * Used in {@link com.github.colinjeremie.newsbeautifier.fragments.FeedSelectFragment}
 *
 * Created by jerem_000 on 3/15/2016.
 */
public interface OnMyFeedsChanged{
    void onMyFeedsChanged();
}

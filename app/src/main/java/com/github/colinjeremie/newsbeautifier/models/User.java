package com.github.colinjeremie.newsbeautifier.models;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The user of the app
 * Created by jerem_000 on 2/19/2016.
 */

public class User {

    private Long id = (long)1;

    private List<RSSFeed> feeds = null;

    public User(){}

    /**
     * Fetch the user feed from the Db
     *
     * @return the user's feeds
     */
    public List<RSSFeed> getFeeds() {
        if (feeds == null){
            feeds = new Select()
                    .from(RSSFeed.class)
                    .where(RSSFeed_Table.userId.eq(getId()))
                    .queryList();
        }
        return feeds;
    }

    /**
     * Return the last <code>nbArticles</code> {@link RSSItem} of the User with a picture
     *
     * @param nbArticles the limit of {@link RSSItem} returned
     * @return List<RSSItem>
     */
    public List<RSSItem> getLastArticlesWithPicture(Integer nbArticles){
        List<RSSItem> list = new ArrayList<>();

        if (nbArticles > 0) {
            for (RSSFeed tmp : getFeeds()) {
                list.addAll(tmp.getItems());
            }
            List<RSSItem> f = new ArrayList<>();
            for (RSSItem tmp : list){
                if (!tmp.getImage().isEmpty()){
                    f.add(tmp);
                }
            }
            return nbArticles < f.size() ? f.subList(0, nbArticles) : f;
        }
        return list;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFeeds(List<RSSFeed> pFeeds) {
        feeds = pFeeds;
    }

    /**
     * Associate a {@link RSSFeed} with a {@link User}
     *
     * @param feed RSSFeed
     */
    public void addFeed(RSSFeed feed) {
        boolean allow = true;

        for (RSSFeed tmp : feeds){
            if (tmp.getUrl().equals(feed.getUrl())){
                allow = false;
                break;
            }
        }
        if (allow){
            feeds.add(feed);
        }
    }

    /**
     * Unmatch a {@link RSSFeed} with a {@link User}
     *
     * @param feed RSSFeed
     */
    public void removeFeed(RSSFeed feed) {
        for (Iterator<RSSFeed> it = feeds.iterator(); it.hasNext();) {
            if (it.next().getUrl().equals(feed.getUrl())) {
                it.remove();
                break;
            }
        }
    }
}

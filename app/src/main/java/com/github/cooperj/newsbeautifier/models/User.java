package com.github.cooperj.newsbeautifier.models;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/19/2016.
 */

public class User {

    private Long id = (long)1;

    private List<RSSFeed> feeds = null;

    public User(){}

    public List<RSSFeed> getFeeds() {
        if (feeds == null){
            feeds = new Select()
                    .from(RSSFeed.class)
                    .where(RSSFeed_Table.userId.eq(getId()))
                    .queryList();
        }
        return feeds;
    }

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

    public void removeFeed(RSSFeed feed) {
        for (Iterator<RSSFeed> it = feeds.iterator(); it.hasNext();) {
            if (it.next().getUrl().equals(feed.getUrl())) {
                it.remove();
                break;
            }
        }
    }
}

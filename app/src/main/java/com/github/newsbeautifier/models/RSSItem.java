package com.github.newsbeautifier.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.newsbeautifier.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/18/2016.
 */

@Table(database = MyDatabase.class)
public class RSSItem extends BaseModel implements Parcelable{
    public static final String GUID_TAG = "guid";
    public static final String TITLE_TAG = "title";
    public static final String CATEGORY_TAG = "category";
    public static final String THUMBNAIL_TAG = "media:thumbnail";
    public static final String ENCLOSURE_TAG = "enclosure";
    public static final String LANGUAGE_TAG = "language";
    public static final String DESCRIPTION_TAG = "description";
    public static final String CONTENT_TAG = "content:encoded";
    public static final String LINK_TAG = "link";
    public static final String AUTHOR_TAG = "author";
    public static final String CREATOR_TAG = "dc:creator";
    public static final String PUBDATE_TAG = "published";
    public static final String PUBDATE_TAG2 = "pubDate";


    @PrimaryKey
    @Column
    private String guid = "";

    @Column
    private String feedLink;

    @Column
    private String title = "";

    @Column
    private String category = "";

    @Column
    private String content = ""; // full content of the article

    @Column
    private String description = ""; // summary of the article

    @Column
    private String language = "";

    @Column
    private String link = "";

    @Column
    private String author = "";

    @Column
    private String image = "";

    @Column
    private String pubDate = "";

    public RSSItem() {

    }

    protected RSSItem(Parcel in) {
        guid = in.readString();
        feedLink = in.readString();
        title = in.readString();
        category = in.readString();
        content = in.readString();
        language = in.readString();
        description = in.readString();
        link = in.readString();
        author = in.readString();
        image = in.readString();
        pubDate = in.readString();
    }

    public static final Creator<RSSItem> CREATOR = new Creator<RSSItem>() {
        @Override
        public RSSItem createFromParcel(Parcel in) {
            return new RSSItem(in);
        }

        @Override
        public RSSItem[] newArray(int size) {
            return new RSSItem[size];
        }
    };

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String pContent) {
        content = pContent;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String pLink) {
        link = pLink;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String pAuthor) {
        author = pAuthor;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pPubdate) {
        pubDate = pPubdate;
        if (pubDate != null && pubDate.contains("+")){
            int idx = pubDate.lastIndexOf('+');
            if (idx > 0){
                pubDate = pubDate.substring(0, idx);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String pLanguage) {
        language = pLanguage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String pCategory) {
        category = pCategory;
    }

    public String getFeedLink() {
        return feedLink;
    }

    public void setFeedLink(String feedlink) {
        feedLink = feedlink;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(guid);
        dest.writeString(feedLink);
        dest.writeString(title);
        dest.writeString(category);
        dest.writeString(content);
        dest.writeString(language);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(author);
        dest.writeString(image);
        dest.writeString(pubDate);
    }
}

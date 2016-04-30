package com.github.colinjeremie.newsbeautifier.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.colinjeremie.newsbeautifier.MyDatabase;
import com.github.colinjeremie.newsbeautifier.utils.RSSItemHelper;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * The representation of an Article
 * Also a Table in the Db
 * Created by jerem_000 on 2/18/2016.
 */

@Table(database = MyDatabase.class)
public class RSSItem extends BaseModel implements Parcelable{
    /**
     * Keys used to parse a Rss feed entry
     */
    public static final String GUID_TAG = "guid";
    public static final String TITLE_TAG = "title";
    public static final String CATEGORY_TAG = "category";
    public static final String MEDIA_THUMBNAIL_TAG = "media:thumbnail";
    public static final String THUMBNAIL_TAG = "thumbnail";
    public static final String MEDIUM_THUMBNAIL_TAG = "mediumthumbnail";
    public static final String LARGE_THUMBNAIL_TAG = "largethumbnail";
    public static final String ENCLOSURE_TAG = "enclosure";
    public static final String LANGUAGE_TAG = "language";
    public static final String DESCRIPTION_TAG = "description";
    public static final String CONTENT_TAG = "content:encoded";
    public static final String LINK_TAG = "link";
    public static final String AUTHOR_TAG = "author";
    public static final String CREATOR_TAG = "dc:creator";
    public static final String PUBDATE_TAG = "published";
    public static final String PUBDATE_TAG2 = "pubDate";

    /**
     * Format of the date presents in the norme RSS2.0
     */
    static public final String FORMAT = "E',' d LLL yyyy k':'m':'s Z";

    @PrimaryKey
    @Column
    private String guid = "";

    @Column
    private String feedLink;

    @Column
    private String title = "";

    @Column
    private String category = "";

    /**
     * Full content of the article
     */
    @Column
    private String content = "";

    /**
     * Summary of the article
     */
    @Column
    private String description = "";

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

    private Date pubDateFormat;

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
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT, Locale.US);
        try {
            pubDateFormat = sdf.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getDescription() {
        return description;
    }

    /**
     * We set the description and extracts the possible image from it
     * @param pDescription String
     */
    public void setDescription(String pDescription) {
        description = pDescription;
        if (!description.isEmpty()){
            String url = RSSItemHelper.getUrlImgFromXmlString(description);
            if (url != null && image.isEmpty()){
                setImage(url);
                description = Jsoup.parse(description).text();
            }
        }
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

    /**
     * We parse the date in the {@link String} format to return a {@link Date} format
     * @return the Date object representation
     */
    public Date getPubDateFormat() {
        if (pubDateFormat == null){
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT, Locale.US);
            try {
                pubDateFormat = sdf.parse(pubDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return pubDateFormat;
    }

    public void setPubDateFormat(Date pubdateformat) {
        pubDateFormat = pubdateformat;
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

    @Override
    public String toString() {
        return title;
    }

    /**
     * Comparator of Date ASC
     */
    public static final class DateComparatorAsc implements Comparator<RSSItem> {
        @Override
        public int compare(RSSItem o1, RSSItem o2) {
            if (o1.pubDateFormat != null && o2.pubDateFormat != null){
                return o1.pubDateFormat.compareTo(o2.pubDateFormat);
            }
            return o1.getPubDate().compareTo(o2.getPubDate());
        }
    }

    /**
     * Comparator of Date DESC
     */
    public static final class DateComparatorDesc implements Comparator<RSSItem> {
        @Override
        public int compare(RSSItem o1, RSSItem o2) {
            if (o1.pubDateFormat != null && o2.pubDateFormat != null){
                return o1.pubDateFormat.compareTo(o2.pubDateFormat) * -1;
            }
            return o1.getPubDate().compareTo(o2.getPubDate()) * -1;
        }
    }

    /**
     * Comparator of Title ASC
     */
    public static final class TitleComparatorAsc implements Comparator<RSSItem> {
        @Override
        public int compare(RSSItem o1, RSSItem o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    }

    /**
     * Comparator of Title DESC
     */
    public static final class TitleComparatorDesc implements Comparator<RSSItem> {
        @Override
        public int compare(RSSItem o1, RSSItem o2) {
            return o1.getTitle().compareTo(o2.getTitle()) * -1;
        }
    }
}

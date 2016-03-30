package com.github.colinjeremie.newsbeautifier.utils;

import android.util.Xml;

import com.github.colinjeremie.newsbeautifier.models.RSSFeed;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/21/2016.
 */
public class RSSParser {
    public static final RSSFeed[] RSS_FEEDS = {
            new RSSFeed("http://www.legorafi.fr/feed/"),
            new RSSFeed("http://www.begeek.fr/feed"),
            new RSSFeed("http://feeds.feedburner.com/Kulturegeek?format=xml"),
            new RSSFeed("http://feeds2.feedburner.com/LeJournalduGeek"),
            new RSSFeed("http://feeds.feedburner.com/ubergizmo_fr"),
            new RSSFeed("http://droidsoft.fr/feed/rss/"),
            new RSSFeed("http://feeds.feedburner.com/Iphoneaddictfr?format=xml")
    };

    private static final String ns = null;
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private static RSSParser _instance = null;

    public static RSSParser getInstance(){
        if (_instance == null){
            _instance = new RSSParser();
        }
        return _instance;
    }


    public RSSFeed readRssFeed(InputStream in, RSSFeed feed) {
        XmlPullParser parser = Xml.newPullParser();

        for (RSSItem item : feed.getItems()){
            item.delete();
        }
        feed.getItems().clear();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                switch (name) {
                    case RSSFeed.FEED_TAG:
                        feed.setLanguage(readLanguage(parser));
                        parser.require(XmlPullParser.START_TAG, ns, RSSFeed.ENTRY_TAG);
                        break;
                    case RSSFeed.CHANNEL_TAG:
                        parser.require(XmlPullParser.START_TAG, ns, RSSFeed.CHANNEL_TAG);
                        break;
                    case RSSFeed.TITLE_TAG:
                        feed.setTitle(readText(parser));
                        break;
                    case RSSFeed.IMAGE_TAG:
                        feed.setImage(readImage(parser));
                        break;
                    case RSSFeed.DESCRIPTION_TAG:
                        feed.setDescription(readText(parser));
                        break;
                    case RSSFeed.ICON_TAG:
                        feed.setIcon(readText(parser));
                        break;
                    case RSSFeed.CATEGORY_TAG:
                        feed.setCategory(readText(parser));
                        break;
                    case RSSFeed.UPDATE_TAG:
                        feed.setUpdatedDate(readText(parser));
                        break;
                    case RSSFeed.LINK_TAG:
                        feed.setLink(readLink(parser));
                        break;
                    case RSSFeed.LANGUAGE_TAG:
                        feed.setLanguage(readText(parser));
                        break;
                    case RSSFeed.ENTRY_TAG:
                        feed.addEntry(readEntry(parser, RSSFeed.ENTRY_TAG));
                        break;
                    case RSSFeed.ITEM_TAG:
                        feed.addEntry(readEntry(parser, RSSFeed.ITEM_TAG));
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return null;
        }
        if (feed.exists()){
            feed.update();
        } else {
            feed.save();
        }
        for (RSSItem item : feed.getItems()){
            item.setFeedLink(feed.getLink());
            item.save();
        }
        return feed;
    }

    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        String url = "";
        parser.require(XmlPullParser.START_TAG, ns, RSSFeed.IMAGE_TAG);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("url")) {
                url = readText(parser);
            } else {
                skip(parser);
            }
        }
        return url;
    }

    private String readLanguage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, RSSFeed.FEED_TAG);

        return parser.getAttributeValue(null, RSSFeed.LANGUAGE_ATTRIBUTE);
    }

    private RSSItem readEntry(XmlPullParser parser, String itemTag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, itemTag);
        RSSItem item = new RSSItem();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch (name) {
                case RSSItem.GUID_TAG:
                    item.setGuid(readText(parser));
                    break;
                case RSSItem.TITLE_TAG:
                    item.setTitle(readText(parser));
                    break;
                case RSSItem.CONTENT_TAG:
                    item.setContent(readText(parser));
                    break;
                case RSSItem.DESCRIPTION_TAG:
                    item.setDescription(readText(parser));
                    break;
                case RSSItem.LANGUAGE_TAG:
                    item.setLanguage(readText(parser));
                    break;
                case RSSItem.CATEGORY_TAG:
                    item.setCategory(readText(parser));
                    break;
                case RSSItem.LINK_TAG:
                    item.setLink(readText(parser));
                    break;
                case RSSItem.AUTHOR_TAG:
                    item.setAuthor(readAuthor(parser));
                    break;
                case RSSItem.CREATOR_TAG:
                    item.setAuthor(readText(parser));
                    break;
                case RSSItem.PUBDATE_TAG:
                case RSSItem.PUBDATE_TAG2:
                    item.setPubDate(readText(parser));
                    break;
                case RSSItem.MEDIA_THUMBNAIL_TAG:
                    item.setImage(readMediaThumbNail(parser));
                    break;
                case RSSItem.THUMBNAIL_TAG:
                case RSSItem.MEDIUM_THUMBNAIL_TAG:
                case RSSItem.LARGE_THUMBNAIL_TAG:
                    item.setImage(readText(parser));
                    break;
                case RSSItem.ENCLOSURE_TAG:
                    String enclosure = readEnclosure(parser);
                    if (enclosure != null){
                        item.setImage(enclosure);
                    }
                default:
                    skip(parser);
                    break;
            }
        }
        return item;
    }

    private String readEnclosure(XmlPullParser parser) {
        String type = parser.getAttributeValue(null, "type");

        if (type != null && type.contains("image/")){
            return parser.getAttributeValue(null, "url");
        }
        return null;
    }

    private String readMediaThumbNail(XmlPullParser parser) {
        return parser.getAttributeValue(null, "url");
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, RSSItem.LINK_TAG);
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals(RSSItem.LINK_TAG)) {
            if (relType != null && relType.equals("alternate")){
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            } else {
                link = readText(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, RSSItem.LINK_TAG);

        return link;
    }

    private String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, RSSItem.AUTHOR_TAG);
        String author = "";

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("name")) {
                author = readText(parser);
            } else {
                skip(parser);
            }
        }
        return author;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    public InputStream readFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            return conn.getInputStream();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}

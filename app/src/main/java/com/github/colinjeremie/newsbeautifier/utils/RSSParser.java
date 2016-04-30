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
 * Parser rss to fetch blog and articles
 *
 * Created by jerem_000 on 2/21/2016.
 */
public class RSSParser {
    /**
     * Feeds by default in the app
     */
    public static final RSSFeed[] RSS_FEEDS = {
            new RSSFeed("http://www.legorafi.fr/feed/"),
            new RSSFeed("http://www.begeek.fr/feed"),
            new RSSFeed("http://feeds.feedburner.com/Kulturegeek?format=xml"),
            new RSSFeed("http://feeds2.feedburner.com/LeJournalduGeek"),
            new RSSFeed("http://feeds.feedburner.com/ubergizmo_fr"),
            new RSSFeed("http://droidsoft.fr/feed/rss/"),
            new RSSFeed("http://feeds.feedburner.com/Iphoneaddictfr?format=xml")
    };

    /**
     * Namespace used for the {@link XmlPullParser}
     */
    private static final String ns = null;

    /**
     * Read timeout to get an {@link InputStream} from an url
     */
    private static final int READ_TIMEOUT = 10000;

    /**
     * Connect timeout to get an {@link InputStream} from an url
     */
    private static final int CONNECT_TIMEOUT = 15000;

    /**
     * Static instance of {@link RSSParser}
     */
    private static RSSParser _instance = null;

    /**
     * Singleton to get the RSSParser {@link #_instance}
     * @return {@link #_instance}
     */
    public static RSSParser getInstance(){
        if (_instance == null){
            _instance = new RSSParser();
        }
        return _instance;
    }


    /**
     * Read a RssFeed from <code>feed</code>
     * The {@link RSSFeed} and the {@link RSSItem} are saved in the local databse
     * @param in the stream pointed to the Feed url
     * @param feed The feed to bind
     * @return feed
     */
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

    /**
     * Returns the url of the image
     * @param parser XMLPullParser
     * @return The url of the image
     * @throws IOException
     * @throws XmlPullParserException
     */
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

    /**
     * Read the language of the feed
     * @param parser XMLPullparser
     * @return The String of the language
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readLanguage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, RSSFeed.FEED_TAG);

        return parser.getAttributeValue(null, RSSFeed.LANGUAGE_ATTRIBUTE);
    }

    /**
     * Read a Feed entry starting at the <code>itemTag</code>
     * @param parser XMLPullParser
     * @param itemTag String eg. entry, item
     * @return The {@link RSSItem} with the data parsed from the <code>parser</code>
     * @throws IOException
     * @throws XmlPullParserException
     */
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

    /**
     * Parser a enclosure image
     * @param parser XMLPullParser
     * @return The url of the image
     */
    private String readEnclosure(XmlPullParser parser) {
        String type = parser.getAttributeValue(null, "type");

        if (type != null && type.contains("image/")){
            return parser.getAttributeValue(null, "url");
        }
        return null;
    }

    /**
     * Parse a thumbnail url
     * @param parser XMLPullParser
     * @return The url of the thumbnail
     */
    private String readMediaThumbNail(XmlPullParser parser) {
        return parser.getAttributeValue(null, "url");
    }

    /**
     * Skip the whole current tag of the <code>parser</code>
     * @param parser XMLPullParser
     * @throws XmlPullParserException
     * @throws IOException
     */
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

    /**
     * Read a link from the <code>parser</code>
     * @param parser XMLPullParser
     * @return the url link
     * @throws IOException
     * @throws XmlPullParserException
     */
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

    /**
     * Read the author from the {@link RSSItem#AUTHOR_TAG}
     * @param parser XMLPullParser
     * @return String the name of the author
     * @throws IOException
     * @throws XmlPullParserException
     */
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

    /**
     * Read text between 2 tags, eg <test>text to get</test>
     * @param parser XMLPullParser
     * @return String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Get a InputStream from an url
     * @param urlString String
     * @return InputStream
     */
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

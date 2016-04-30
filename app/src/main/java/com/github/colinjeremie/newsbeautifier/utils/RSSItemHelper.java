package com.github.colinjeremie.newsbeautifier.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Helper related to {@link com.github.colinjeremie.newsbeautifier.models.RSSItem}
 *
 * Created by jerem_000 on 2/27/2016.
 */
public class RSSItemHelper {

    /**
     * Parse the <code>xml</code> and return the url of a img tag if it exists
     *
     * @param xml a xml representation in the {@link String} format
     * @return the url of the first img valid tag found
     */
    static public String getUrlImgFromXmlString(String xml){
        Document doc = Jsoup.parse(xml);
        Elements imgs = doc.select("img");
        for (int j = 0; j < imgs.size(); j++) {
            Element img = imgs.get(j);
            if (img.hasAttr("src")) {
                String ext = img.attr("src");
                if (hasAImgExtension(ext)){
                    return ext;
                }
            }
        }
        return null;
    }

    /**
     * Check if the <code>fileName</code> is an image extension
     *
     * @param fileName String
     * @return true if it has an extension img
     */
    static public boolean hasAImgExtension(String fileName){
        String[] ext = new String[]{"png", "bmp", "jpg", "jpeg"};

        for (String e : ext){
            if (fileName.endsWith(e) || fileName.endsWith(e.toUpperCase())){
                return true;
            }
        }
        return false;
    }
}

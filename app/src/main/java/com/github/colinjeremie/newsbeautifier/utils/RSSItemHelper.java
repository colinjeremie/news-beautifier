package com.github.colinjeremie.newsbeautifier.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/27/2016.
 */
public class RSSItemHelper {

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

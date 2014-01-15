package com.cookietest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Utils {
    static String convertMapToPostData(HashMap<String, String> map) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            String value = URLEncoder.encode(map.get(key), "UTF-8");
            key = URLEncoder.encode(key, "UTF-8");
            builder.append(key).append("=").append(value)
                    .append("&");
        }
        return builder.toString();
    }

    static HashMap<String, String> parseHtml(String html) {
        HashMap<String, String> values = new HashMap<String, String>();

        Document doc = Jsoup.parse(html);
        for (Element element : doc.getElementsByTag("input")) {
            if (element.id().equals("__EVENTVALIDATION") || element.id().equals("__VIEWSTATE")) {
                values.put(element.id(), element.val());
            }
        }
        values.put("ActiveTab", "default");
        values.put("AdvancedOptionsStartUrl", "/sitecore/shell/applications/clientusesoswindows.aspx");
        values.put("Login$Login", "Login");
        return values;
    }
}

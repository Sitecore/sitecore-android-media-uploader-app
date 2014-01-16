package com.cookietest.retrofit;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class TokensApiConverter implements Converter {

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            String html =  IOUtils.toString(body.in());
            return parseTokensFromHtml(html);
        } catch (IOException e) {
            return null;
        }
    }

    private static HashMap<String, String> parseTokensFromHtml(String html) {
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

    @Override
    public TypedOutput toBody(final Object object) {
        return new TypedOutput() {
            @Override
            public String fileName() {
                return null;
            }

            @Override
            public String mimeType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            public long length() {
                return object.toString().length();
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(object.toString().getBytes());
            }
        };
    }
}

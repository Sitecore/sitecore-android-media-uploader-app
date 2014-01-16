package com.cookietest.retrofit;

import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookieStore;
import java.util.HashMap;

import com.cookietest.Cookies;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import retrofit.RetrofitError;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class LoginApiConverter implements Converter {

    private final CookieManager mCookieManager;

    public LoginApiConverter(CookieManager cookieManager) {
        mCookieManager = cookieManager;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            String html =  IOUtils.toString(body.in());
            if (!html.contains("Your login attempt was not successful.")) {
                CookieStore cookieJar = mCookieManager.getCookieStore();
                return new Cookies(cookieJar.getCookies());
            } else {
                throw new ConversionException("wrong login");
            }
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(final Object object) {
        final HashMap<String, String> data = (HashMap<String, String>) object;
        final String postData = convertTokensToPostData(data);
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
                return postData.length();
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(postData.getBytes());
            }
        };
    }

    private static String convertTokensToPostData(HashMap<String, String> map) {
        StringBuilder builder = new StringBuilder();

        for (String key : map.keySet()) {
            final String encodedKey = Uri.encode(key);
            final String encodedValue = Uri.encode(map.get(key));
            builder.append(encodedKey).append("=").append(encodedValue)
                    .append("&");
        }
        return builder.toString();
    }
}

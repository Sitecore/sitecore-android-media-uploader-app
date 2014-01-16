package com.cookietest;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URLEncoder;
import java.util.HashMap;

import com.cookietest.retrofit.LoginApi;
import com.cookietest.retrofit.StringConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import retrofit.Callback;
import retrofit.RestAdapter.Builder;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginUtils {

    private final LoginApi mLoginApi;
    private final CookieManager mCookieManager;

    public LoginUtils() {
        mLoginApi = new Builder().setServer(Constants.CMS_URL)
                .setConverter(new StringConverter()).build().create(LoginApi.class);

        mCookieManager = new CookieManager();
        mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);
    }

    public void performLogin(final String user, final String pass, final Callback<Tokens> callback) {
        Callback<String> listener = new Callback<String>() {
            @Override
            public void success(String html, Response response) {
                login(html, callback, user, pass);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        };

        mLoginApi.getMainPage(listener);
    }

    private void login(String html, final Callback<Tokens> callback, String user, String pass) {
        final HashMap<String, String> tokens = parseTokensFromHtml(html);
        tokens.put("Login$UserName", user);
        tokens.put("Login$Password", pass);
        tokens.put("Language", "");

        Callback<String> listener = new Callback<String>() {
            @Override
            public void success(String html, Response response) {
                CookieStore cookieJar = mCookieManager.getCookieStore();
                Tokens cookieTokens = new Tokens(cookieJar.getCookies());
                if (!html.contains("Your login attempt was not successful.")) callback.success(cookieTokens, response);
                else callback.success(null, response);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        };

        String postData = null;
        try {
            postData = LoginUtils.convertTokensToPostData(tokens);
        } catch (UnsupportedEncodingException e) {}

        mLoginApi.performLogin(postData, listener);
    }

    private static String convertTokensToPostData(HashMap<String, String> map) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            String value = URLEncoder.encode(map.get(key), "UTF-8");
            key = URLEncoder.encode(key, "UTF-8");
            builder.append(key).append("=").append(value)
                    .append("&");
        }
        return builder.toString();
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
}

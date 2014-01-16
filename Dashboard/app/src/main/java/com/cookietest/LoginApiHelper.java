package com.cookietest;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;

import com.cookietest.retrofit.ApiFactory;
import com.cookietest.retrofit.LoginApi;
import com.cookietest.retrofit.TokensApi;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginApiHelper {

    private final LoginApi mLoginApi;
    private final CookieManager mCookieManager;

    public LoginApiHelper() {
        mCookieManager = new CookieManager();
        mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);

        mLoginApi = ApiFactory.newLoginApi(mCookieManager);
    }

    public void performLogin(final String user, final String pass, final Callback<Cookies> callback) {
        Callback<HashMap<String, String>> listener = new Callback<HashMap<String, String>>() {
            @Override
            public void success(HashMap<String, String> tokens, Response response) {
                tokens.put("Login$UserName", user);
                tokens.put("Login$Password", pass);
                tokens.put("Language", "");

                login(tokens, callback, user, pass);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        };

        TokensApi api = ApiFactory.newTokensApi();
        api.getMainPage(listener);
    }

    private void login(HashMap<String, String> tokens, final Callback<Cookies> callback, String user, String pass) {
        Callback<Cookies> listener = new Callback<Cookies>() {
            @Override
            public void success(Cookies cookies, Response response) {
                callback.success(cookies, response);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        };

        mLoginApi.performLogin(tokens, listener);
    }

}

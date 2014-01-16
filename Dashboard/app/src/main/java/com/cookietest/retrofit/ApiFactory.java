package com.cookietest.retrofit;

import java.net.CookieManager;

import com.cookietest.AuthenticatedRequestInterceptor;
import com.cookietest.Config;
import com.cookietest.Cookies;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;

public class ApiFactory {

    public static TokensApi newTokensApi() {
        return new Builder()
                .setServer(Config.CMS_URL)
                .setConverter(new TokensApiConverter())
                .build()
                .create(TokensApi.class);
    }

    public static LoginApi newLoginApi(CookieManager cookieManager) {
        return new Builder()
                .setServer(Config.CMS_URL)
                .setConverter(new LoginApiConverter(cookieManager))
                .build()
                .create(LoginApi.class);
    }

    public static DashBoardApi newDashboardApi(Cookies tokens) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(Config.CMS_URL)
                .setConverter(new CsvConverter())
                .setRequestInterceptor(new AuthenticatedRequestInterceptor(tokens))
                .build();

        return restAdapter.create(DashBoardApi.class);
    }
}

package com.cookietest.retrofit;

import java.util.HashMap;

import com.cookietest.Constants;

import retrofit.Callback;
import retrofit.http.GET;

public interface TokensApi {

    @GET(Constants.LOGIN_ENDPOINT_URL)
    void getMainPage(Callback<HashMap<String, String>> listener);

}

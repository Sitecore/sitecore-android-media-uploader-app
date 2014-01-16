package com.cookietest.retrofit;

import com.cookietest.Constants;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface LoginApi {
    @GET(Constants.LOGIN_ENDPOINT_URL)
    void getMainPage(Callback<String> listener);

    @POST(Constants.LOGIN_ENDPOINT_URL)
    void performLogin(@Body String body, Callback<String> listener);
}

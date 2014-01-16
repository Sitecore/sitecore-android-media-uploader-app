package com.cookietest.retrofit;

import java.util.HashMap;

import com.cookietest.Constants;
import com.cookietest.Cookies;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface LoginApi {

    @POST(Constants.LOGIN_ENDPOINT_URL)
    void performLogin(@Body HashMap<String, String> body, Callback<Cookies> listener);

}

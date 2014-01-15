package com.cookietest.retrofit;

import java.util.LinkedList;

import com.cookietest.Constants;
import com.cookietest.csv.Data;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

public interface DashBoardApi {
    @GET(Constants.LOGIN_ENDPOINT_URL)
    void getMainPage(Callback<String> listener);

    @POST(Constants.LOGIN_ENDPOINT_URL)
    void performLogin(@Body String body, Callback<String> listener);

    @GET(Constants.TRAFFIC_SEARCH_KEYWORDS_URL)
    void getTrafficSearchKeywords(@Header(Constants.COOKIE_HEADER) String cookies, Callback<LinkedList<Data>> listener);
}

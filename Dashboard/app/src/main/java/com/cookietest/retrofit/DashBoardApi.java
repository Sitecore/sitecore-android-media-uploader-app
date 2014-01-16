package com.cookietest.retrofit;

import java.util.LinkedList;

import com.cookietest.Constants;
import com.cookietest.csv.Data;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface DashBoardApi {


    @GET(Constants.TRAFFIC_SEARCH_KEYWORDS_URL)
    void getTrafficSearchKeywords(Callback<LinkedList<Data>> listener);
}

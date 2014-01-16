package com.cookietest.retrofit;

import java.util.LinkedList;

import com.cookietest.Constants;
import com.cookietest.csv.DataRow;

import retrofit.Callback;
import retrofit.http.GET;

public interface DashBoardApi {

    @GET(Constants.TRAFFIC_SEARCH_KEYWORDS_URL)
    void getTrafficSearchKeywords(Callback<LinkedList<DataRow>> listener);
}

package com.cookietest.retrofit;

import android.content.Context;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class BasicCallbackHandler<T> implements Callback<T> {
    Context mContext;

    public BasicCallbackHandler(Context context) {
        mContext = context;
    }

    @Override
    public void failure(RetrofitError error) {
        Toast.makeText(mContext, "Error : " + error.getCause(), Toast.LENGTH_LONG).show();
    }
}

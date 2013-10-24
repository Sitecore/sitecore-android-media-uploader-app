package net.sitecore.android.mediauploader.util;

import com.android.volley.Response.Listener;

public class EmptyListener<T> implements Listener<T> {

    @Override
    public void onResponse(T t) {
    }
}

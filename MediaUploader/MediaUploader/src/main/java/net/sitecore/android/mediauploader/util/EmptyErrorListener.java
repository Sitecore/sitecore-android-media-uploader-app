package net.sitecore.android.mediauploader.util;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

public class EmptyErrorListener implements ErrorListener {

    @Override
    public void onErrorResponse(VolleyError volleyError) {
    }
}

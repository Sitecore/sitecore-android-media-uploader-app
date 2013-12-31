package net.sitecore.android.mediauploader.util;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import static net.sitecore.android.sdk.api.LogUtils.LOGE;

public class EmptyErrorListener implements ErrorListener {

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        LOGE(volleyError.getCause());
    }
}

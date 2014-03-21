package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment;

public class SimpleNetworkListenerImpl implements ItemsBrowserFragment.NetworkEventsListener {
    private Context mContext;

    public SimpleNetworkListenerImpl(Context context) {
        mContext = context;
    }

    @Override
    public void onUpdateRequestStarted() {

    }

    @Override
    public void onUpdateSuccess(ItemsResponse itemsResponse) {

    }

    @Override
    public void onUpdateError(VolleyError error) {
        Toast.makeText(mContext, Utils.getMessageFromError(error), Toast.LENGTH_LONG).show();
    }
}

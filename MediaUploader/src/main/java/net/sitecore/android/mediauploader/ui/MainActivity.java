package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.os.Bundle;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.browser.MediaBrowserFragment;
import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class MainActivity extends Activity implements Listener<ScApiSession>, ErrorListener {

    public static ScApiSession mSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.setLogEnabled(true);

        String url = "http://scmobileteam.cloudapp.net";
        String name = "extranet\\creatorex";
        String password = "creatorex";
        ScApiSession.getSession(this, url, name, password, this);
    }

    @Override
    public void onResponse(ScApiSession scApiSession) {
        mSession = scApiSession;
        mSession.setShouldCache(true);

        final MediaBrowserFragment fragment = (MediaBrowserFragment) getFragmentManager().findFragmentById(R.id.fragment_items);
        getContentResolver().delete(Items.CONTENT_URI, null, null);
        ScRequest request = mSession.getItems(new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse itemsResponse) {
                ScItem item = itemsResponse.getItems().get(0);
                fragment.setQueryParent(item.getParentItemId());
            }
        }, this).build();
        RequestQueueProvider.getRequestQueue(this).add(request);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {

    }


}
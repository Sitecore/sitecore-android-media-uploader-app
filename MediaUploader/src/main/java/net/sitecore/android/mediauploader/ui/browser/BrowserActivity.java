package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import javax.inject.Inject;

import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.internal.LogUtils;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.ContentTreePositionListener;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.NetworkEventsListener;

public class BrowserActivity extends Activity implements ContentTreePositionListener, NetworkEventsListener {

    private BrowserFragment mFragment;
    private TextView mHeaderText;

    @Inject ScApiSession mApiSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browser);
        UploaderApp.from(this).inject(this);

        LogUtils.setLogEnabled(true);
        mFragment = (BrowserFragment) getFragmentManager().findFragmentById(R.id.fragment_browser);
        mFragment.setContentTreePositionListener(this);
        mFragment.setNetworkEventsListener(this);

        mHeaderText = (TextView) findViewById(R.id.text_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFragment.loadContent(mApiSession);
    }

    @Override
    public void onGoUp(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onGoInside(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onInitialized(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onUpdateRequestStarted() {

    }

    @Override
    public void onUpdateSuccess(ItemsResponse itemsResponse) {
    }

    @Override
    public void onUpdateError(VolleyError volleyError) {
    }
}

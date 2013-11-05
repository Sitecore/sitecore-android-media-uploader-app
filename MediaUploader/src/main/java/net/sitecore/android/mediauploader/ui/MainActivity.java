package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.browser.MediaBrowserFragment;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.PayloadType;
import net.sitecore.android.sdk.api.model.ScItem;

import butterknife.InjectView;
import butterknife.Views;

import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_INSTANCES;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MEDIA_BROWSER;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MY_UPLOADS;
import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MainActivity extends Activity implements Listener<ScApiSession>, ErrorListener,
        SlidingNavigationFragment.Callbacks {

    public static ScApiSession mSession;

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private MediaBrowserFragment mMediaBrowserFragment;
    private MyUploadsListFragment mUploadsListFragment;
    private InstancesListFragment mInstancesListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        String url = "http://mobiledev1ua1.dk.sitecore.net:722";
        String name = "extranet\\creatorex";
        String password = "creatorex";
        ScApiSession.getSession(this, url, name, password, this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResponse(ScApiSession scApiSession) {
        mSession = scApiSession;
        mSession.setShouldCache(true);

        mMediaBrowserFragment = new MediaBrowserFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mMediaBrowserFragment).commit();
        ScRequest request = mSession.getItems(new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse itemsResponse) {
                ScItem item = itemsResponse.getItems().get(0);
                mMediaBrowserFragment.setRootItem(item);
            }
        }, this).withPayloadType(PayloadType.FULL).byItemId("{3D6658D8-A0BF-4E75-B3E2-D050FABCF4E1}").build();
        RequestQueueProvider.getRequestQueue(this).add(request);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        LOGD(Utils.getMessageFromError(volleyError));
    }

    @Override
    public void onNavigationItemSelected(int position) {
        Fragment currentSelection = null;
        if (position == POSITION_MEDIA_BROWSER) {
            if (mMediaBrowserFragment == null) mMediaBrowserFragment = new MediaBrowserFragment();
            currentSelection = mMediaBrowserFragment;
        } else if (position == POSITION_MY_UPLOADS) {
            if (mUploadsListFragment == null) mUploadsListFragment = new MyUploadsListFragment();
            currentSelection = mUploadsListFragment;
        } if (position == POSITION_INSTANCES) {
            if (mInstancesListFragment == null) mInstancesListFragment = new InstancesListFragment();
            currentSelection = mInstancesListFragment;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, currentSelection).commit();

        mDrawerLayout.closeDrawers();
    }
}
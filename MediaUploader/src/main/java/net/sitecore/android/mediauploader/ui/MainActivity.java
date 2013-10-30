package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.browser.MediaBrowserFragment;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
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

import butterknife.InjectView;
import butterknife.Views;

public class MainActivity extends Activity implements Listener<ScApiSession>, ErrorListener {

    public static ScApiSession mSession;

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @InjectView(R.id.list_left_drawer) ListView mLeftPanel;

    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mNavigationItems = {
            "Upload",
            "Media browser",
            "My uploads"
    };

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

        mLeftPanel.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNavigationItems));
        mLeftPanel.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startActivity(new Intent(MainActivity.this, UploadActivity.class));
                }
            }
        });

        String url = "http://scmobileteam.cloudapp.net";
        String name = "extranet\\creatorex";
        String password = "creatorex";
        ScApiSession.getSession(this, url, name, password, this);
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
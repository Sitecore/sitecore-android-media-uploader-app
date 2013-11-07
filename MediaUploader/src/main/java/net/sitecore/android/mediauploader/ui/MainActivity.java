package net.sitecore.android.mediauploader.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.browser.MediaBrowserFragment;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.ScUtils;

import butterknife.InjectView;
import butterknife.Views;

import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_INSTANCES;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MEDIA_BROWSER;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MY_UPLOADS;
import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MainActivity extends Activity implements SlidingNavigationFragment.Callbacks {

//    public static ScApiSession mSession;

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private MediaBrowserFragment mMediaBrowserFragment;
    private MyUploadsListFragment mUploadsListFragment;
    private InstancesListFragment mInstancesListFragment;
    private int mCurrentFragmentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);
        Views.inject(this);

        mActionBar = getActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

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

        if (savedInstanceState == null) {
            onNavigationItemSelected(POSITION_MEDIA_BROWSER);
        }
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
    public void onBackPressed() {
        if (mCurrentFragmentPosition == POSITION_MEDIA_BROWSER && mMediaBrowserFragment.getItemStack().canGoUp()) {
            mMediaBrowserFragment.goUp();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationItemSelected(int position) {
        if (mCurrentFragmentPosition == position) return;

        Fragment currentSelection = null;
        if (position == POSITION_MEDIA_BROWSER) {
            if (mMediaBrowserFragment == null) {
                String root = Prefs.from(this).getString(R.string.key_instance_root_folder, ScUtils.PATH_MEDIA_LIBRARY);
                mMediaBrowserFragment = MediaBrowserFragment.newInstance(root);
            }
            currentSelection = mMediaBrowserFragment;
            mCurrentFragmentPosition = POSITION_MEDIA_BROWSER;
            mActionBar.setTitle(R.string.title_media_browser);
        } else if (position == POSITION_MY_UPLOADS) {
            if (mUploadsListFragment == null) {
                mUploadsListFragment = new MyUploadsListFragment();
            }
            currentSelection = mUploadsListFragment;
            mCurrentFragmentPosition = POSITION_MY_UPLOADS;
            mActionBar.setTitle(R.string.title_my_uploads);
        } if (position == POSITION_INSTANCES) {
            if (mInstancesListFragment == null) {
                mInstancesListFragment = new InstancesListFragment();
            }
            currentSelection = mInstancesListFragment;
            mCurrentFragmentPosition = POSITION_INSTANCES;
            mActionBar.setTitle(R.string.title_instances_manager);
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, currentSelection).commit();
        mDrawerLayout.closeDrawers();
    }
}
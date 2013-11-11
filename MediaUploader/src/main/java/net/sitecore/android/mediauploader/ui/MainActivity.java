package net.sitecore.android.mediauploader.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.browser.MediaBrowserFragment;
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListFragment;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.ScUtils;

import butterknife.InjectView;
import butterknife.Views;

import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_INSTANCES;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MEDIA_BROWSER;
import static net.sitecore.android.mediauploader.ui.SlidingNavigationFragment.POSITION_MY_UPLOADS;
import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MainActivity extends Activity implements SlidingNavigationFragment.Callbacks {

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private SlidingNavigationFragment mNavigationFragment;
    private MediaBrowserFragment mMediaBrowserFragment;
    private MyUploadsListFragment mUploadsListFragment;
    private InstancesListFragment mInstancesListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);
        Views.inject(this);

        mNavigationFragment = (SlidingNavigationFragment) getFragmentManager().findFragmentById(R.id.fragment_navigation);

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
            onMediaBrowserSelected();
        }

        boolean sideMenuShown = Prefs.from(this).getBool(R.string.key_side_menu_shown, false);
        if (!sideMenuShown) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
            Prefs.from(this).put(R.string.key_side_menu_shown, true);
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
        if (mNavigationFragment.isMediaBrowserSelected() && mMediaBrowserFragment.getItemStack().canGoUp()) {
            mMediaBrowserFragment.goUp();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaBrowserSelected() {
        mActionBar.setTitle(R.string.title_media_browser);

        if (mMediaBrowserFragment == null) {
            String root = Prefs.from(this).getString(R.string.key_instance_root_folder, ScUtils.PATH_MEDIA_LIBRARY);
            mMediaBrowserFragment = MediaBrowserFragment.newInstance(root);
        }
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mMediaBrowserFragment).commit();
    }

    @Override
    public void onMyUploadsSelected() {
        mActionBar.setTitle(R.string.title_my_uploads);
        if (mUploadsListFragment == null) {
            mUploadsListFragment = new MyUploadsListFragment();
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mUploadsListFragment).commit();
    }

    @Override
    public void onInstanceManagerSelected() {
        mActionBar.setTitle(R.string.title_instances_manager);

        if (mInstancesListFragment == null) {
            mInstancesListFragment = new InstancesListFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mInstancesListFragment).commit();
    }

    @Override
    public void onSelectionDone() {
        mDrawerLayout.closeDrawers();
    }
}
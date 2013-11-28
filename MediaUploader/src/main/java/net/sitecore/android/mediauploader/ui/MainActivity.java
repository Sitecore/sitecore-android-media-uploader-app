package net.sitecore.android.mediauploader.ui;

import android.app.ActionBar;
import android.app.Activity;
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
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListFragment.OnDefaultInstanceChangeListener;
import net.sitecore.android.mediauploader.ui.upload.MyUploadsListFragment;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.UploaderPrefs;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity implements SlidingNavigationFragment.Callbacks, OnDefaultInstanceChangeListener {

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
        ButterKnife.inject(this);

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
            //            new AsyncQueryHandler(getContentResolver()){
            //            }.startDelete(0, null, Items.CONTENT_URI, null, null);
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
        // TODO : added function for moving up in the items browser
        if (mNavigationFragment != null && mNavigationFragment.isMediaBrowserSelected()) {
//            mMediaBrowserFragment.goUp();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaBrowserSelected() {
        mActionBar.setTitle(R.string.title_media_browser);

        String root = UploaderPrefs.from(this).getCurrentInstance().rootFolder;
        if (mMediaBrowserFragment == null) {
            mMediaBrowserFragment = MediaBrowserFragment.newInstance(root);
        } else {
            mMediaBrowserFragment.setRootFolder(root);
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

    @Override
    public void onDefaultInstanceSelected() {
        String root = UploaderPrefs.from(this).getCurrentInstance().rootFolder;;
        if (mMediaBrowserFragment != null) {
            mMediaBrowserFragment.setRootFolder(root);
            mMediaBrowserFragment.refresh();
        }
    }

    @Override
    public void onDefaultInstanceChanged() {
        mNavigationFragment.updateInstanceSelection();
    }
}
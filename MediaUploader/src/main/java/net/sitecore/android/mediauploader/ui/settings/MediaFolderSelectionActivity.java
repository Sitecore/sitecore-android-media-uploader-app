package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.MediaFolderOnlyFilter;
import net.sitecore.android.mediauploader.model.SortByMediaFolderTemplateComparator;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.ContentTreePositionListener;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.NetworkEventsListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MediaFolderSelectionActivity extends Activity implements ErrorListener {

    public static final String INSTANCE_KEY = "instance";

    @InjectView(R.id.instance_root_folder) TextView mInstanceRootFolder;
    @Inject ScRequestQueue mScRequestQueue;

    private Instance mInstance;
    private Uri mInstanceUri;
    private FolderSelectionFragment mFolderSelectionFragment;

    private final ContentTreePositionListener mContentTreePositionListener = new ContentTreePositionListener() {
        @Override public void onGoUp(ScItem item) {
            mInstanceRootFolder.setText(item.getPath());
        }

        @Override public void onGoInside(ScItem item) {
            mInstanceRootFolder.setText(item.getPath());
        }

        @Override public void onInitialized(ScItem item) {
            mInstanceRootFolder.setText(item.getPath());
        }
    };

    private final NetworkEventsListener mNetworkEventsListener = new NetworkEventsListener() {
        @Override public void onUpdateRequestStarted() {
        }

        @Override public void onUpdateSuccess(ItemsResponse itemsResponse) {
        }

        @Override public void onUpdateError(VolleyError volleyError) {
            onErrorResponse(volleyError);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_media_library);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        UploaderApp.from(getApplicationContext()).cleanInstanceCacheAsync();
        UploaderApp.from(this).inject(this);
        ButterKnife.inject(this);

        mInstanceUri = getIntent().getData();
        mInstance = getIntent().getParcelableExtra(INSTANCE_KEY);
        if (mInstance == null) {
            throw new IllegalStateException("You should pass instance to start this activity");
        }

        mFolderSelectionFragment = (FolderSelectionFragment) getFragmentManager().findFragmentById(R.id.browser_fragment);
        mFolderSelectionFragment.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);
        mFolderSelectionFragment.setItemsFilter(new MediaFolderOnlyFilter());
        mFolderSelectionFragment.setItemsSortOrder(new SortByMediaFolderTemplateComparator());
        mFolderSelectionFragment.setNetworkEventsListener(mNetworkEventsListener);
        mFolderSelectionFragment.setContentTreePositionListener(mContentTreePositionListener);

        ScApiSessionFactory.getSession(mScRequestQueue, mInstance.getUrl(), mInstance.getLogin(),
                mInstance.getPassword(), mSessionListener, this);
    }

    private final Listener<ScApiSession> mSessionListener = new Listener<ScApiSession>() {
        @Override public void onResponse(ScApiSession session) {
            session.setDefaultDatabase(mInstance.getDatabase());
            session.setDefaultSite(mInstance.getSite());
            mFolderSelectionFragment.loadContent(session);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.button_save)
    public void saveCurrentFolder() {
        if (mFolderSelectionFragment.getCurrentItem() != null) {
            mInstance.setRootFolder(mFolderSelectionFragment.getCurrentItem().getPath());
        }

        new InstancesAsyncHandler(getContentResolver()){
            @Override public void onInstancesLoaded(Cursor cursor) {
                onDuplicatesLoaded(cursor);
            }
        }.queryDuplicateInstances(mInstance);
    }

    private void onDuplicatesLoaded(Cursor data) {
        if (data.moveToFirst()) {
            if (checkIsCurrentInstance(data)) {
                performSave();
            } else {
                Toast.makeText(this, R.string.toast_instance_exists, Toast.LENGTH_LONG).show();
            }
        } else {
            new InstancesAsyncHandler(getContentResolver()){
                @Override public void onInstancesLoaded(Cursor cursor) {
                    if (cursor.getCount() == 0) mInstance.setSelected(true);
                    performSave();
                }
            }.queryInstances();
        }
    }

    private boolean checkIsCurrentInstance(Cursor data) {
        if (data.getCount() == 1 && mInstanceUri != null) {
            String currentInstanceId = Instances.getInstanceId(mInstanceUri);
            String dbInstanceId = String.valueOf(data.getInt(Query._ID));
            return currentInstanceId.equals(dbInstanceId);
        } else {
            return false;
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Toast.makeText(this, ScUtils.getMessageFromError(volleyError), Toast.LENGTH_LONG).show();
    }



    private void performSave() {
        InstancesAsyncHandler instancesAsyncHandler = new InstancesAsyncHandler(getContentResolver());
        if (mInstanceUri == null) {
            instancesAsyncHandler.insertNewInstance(mInstance);
        } else {
            instancesAsyncHandler.updateInstance(mInstanceUri, mInstance);
        }

        NavUtils.navigateUpFromSameTask(this);
    }

}

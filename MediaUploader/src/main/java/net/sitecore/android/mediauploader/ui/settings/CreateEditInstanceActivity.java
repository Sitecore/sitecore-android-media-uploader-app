package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import javax.inject.Inject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.RequestBuilder;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

public class CreateEditInstanceActivity extends Activity implements LoaderCallbacks<Cursor> {

    @InjectView(R.id.button_delete_instance) Button mDeleteButton;
    @InjectView(R.id.button_next) Button mNextButton;
    @Inject ScRequestQueue mRequestQueue;

    private InstanceFieldsFragment mInstanceFieldsFragment;
    private Uri mInstanceUri;
    private boolean mIsInstanceSelected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_edit_instance);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        mInstanceUri = getIntent().getData();
        if (mInstanceUri != null) {
            getLoaderManager().initLoader(0, null, this);
            setTitle(R.string.title_edit_instance);
        } else {
            mDeleteButton.setVisibility(View.INVISIBLE);
            setTitle(R.string.title_add_instance);
        }

        mInstanceFieldsFragment = (InstanceFieldsFragment) getFragmentManager().findFragmentById(R.id.instance_fragment);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mInstanceUri, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;
        Instance instance = Instance.fromInstanceCursor(data);
        mIsInstanceSelected = instance.isSelected();
        mInstanceFieldsFragment.setSourceInstance(instance);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.button_delete_instance)
    public void onDeleteInstanceClick() {
        new InstancesAsyncHandler(getContentResolver()) {
            @Override protected void onDeleteComplete(int token, Object cookie, int result) {
                showToast(CreateEditInstanceActivity.this, R.string.toast_instance_deleted);
                if (mIsInstanceSelected) selectLastInstance();
                finish();
            }
        }.deleteInstance(mInstanceUri);
    }

    private void selectLastInstance() {
        new InstancesAsyncHandler(getContentResolver()) {
            @Override public void onInstancesLoaded(Cursor cursor) {
                if (cursor.getCount() > 0) {
                    if (cursor.moveToLast()) {
                        String instanceId = cursor.getString(Query._ID);
                        new InstancesAsyncHandler(getContentResolver()).updateInstanceSelected(instanceId);
                        UploaderApp.from(getApplicationContext()).cleanInstanceCacheAsync();
                    }
                }
            }
        }.queryInstances();
    }

    @OnClick(R.id.button_next) void onNextClick() {
        if (mInstanceFieldsFragment.isFieldsValid()) {
            Instance instance = mInstanceFieldsFragment.getEnteredInstance();
            mRequestQueue.add(
                    ScApiSessionFactory.buildPublicKeyRequest(instance.getUrl(), mSuccessPublicKeyListener, mErrorListener)
            );

            setLoading(true);
        }
    }

    private void setLoading(boolean isLoading) {
        setProgressBarIndeterminateVisibility(isLoading);
        mDeleteButton.setEnabled(!isLoading);
        mNextButton.setEnabled(!isLoading);
    }

    private final Listener<ScPublicKey> mSuccessPublicKeyListener = new Listener<ScPublicKey>() {
        @Override public void onResponse(ScPublicKey scPublicKey) {
            final Instance enteredInstance = mInstanceFieldsFragment.getEnteredInstance();
            enteredInstance.setPublicKey(scPublicKey.getRawValue());

            Listener<ItemsResponse> mSuccess = new Listener<ItemsResponse>() {
                @Override
                public void onResponse(ItemsResponse response) {
                    if (response.getTotalCount() != 0) {
                        showMediaFolderSelectionActivity(enteredInstance);
                    } else {
                        UploaderApp app = UploaderApp.from(getApplicationContext());
                        app.trackEvent(R.string.event_add_site_failed, getString(R.string.toast_instance_is_not_valid));
                        showToast(CreateEditInstanceActivity.this, R.string.toast_instance_is_not_valid);
                    }

                    setLoading(false);
                }
            };

            ScApiSession session = ScApiSessionFactory.newSession(enteredInstance.getUrl(), scPublicKey,
                    enteredInstance.getLogin(), enteredInstance.getPassword());
            RequestBuilder builder = session.readItemsRequest(mSuccess, mErrorListener)
                    .database(mInstanceFieldsFragment.getEnteredInstance().getDatabase());
            if (!TextUtils.isEmpty(enteredInstance.getSite())) {
                builder.fromSite(enteredInstance.getSite());
            }

            mRequestQueue.add(builder.build());
        }
    };

    private void showMediaFolderSelectionActivity(Instance enteredInstance) {
        final Intent intent = new Intent(CreateEditInstanceActivity.this, MediaFolderSelectionActivity.class);
        if (mInstanceUri != null) {
            intent.setData(mInstanceUri);
        }
        intent.putExtra(MediaFolderSelectionActivity.INSTANCE_KEY, enteredInstance);
        startActivity(intent);
    }

    private final ErrorListener mErrorListener = new ErrorListener() {
        @Override public void onErrorResponse(VolleyError error) {
            setLoading(false);
            String message = ScUtils.getMessageFromError(getApplicationContext(), error);
            Toast.makeText(CreateEditInstanceActivity.this, message, Toast.LENGTH_LONG).show();

            UploaderApp app = UploaderApp.from(getApplicationContext());
            app.trackEvent(R.string.event_add_site_failed, message);
        }
    };

}
package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateEditInstanceActivity extends Activity implements LoaderCallbacks<Cursor>, ErrorListener, Listener<ScApiSession> {
    public static final int READ_INSTANCES_ACTION = 0;
    public static final int READ_NAMES_ACTION = 1;

    private static final String SELECTION = Instances.URL + "=? and " + Instances.LOGIN + "=? and " +
            Instances.PASSWORD + "=? and " + Instances.ROOT_FOLDER + "=?";

    private InstanceFragment mInstanceFragment;
    private Uri mInstanceUri;

    @Inject ScRequestQueue mRequestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        mInstanceUri = getIntent().getData();
        if (mInstanceUri != null) {
            getLoaderManager().initLoader(READ_INSTANCES_ACTION, null, this);
            setTitle(R.string.text_edit_instance_title);
        } else {
            setTitle(R.string.text_add_instance_title);
        }

        setContentView(R.layout.activity_edit_instance);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        mInstanceFragment = (InstanceFragment) getFragmentManager().findFragmentById(R.id.instance_fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_validate)
    public void validateConnection() {
        if (mInstanceFragment.isFieldsValid()) {
            Instance instance = mInstanceFragment.getEnteredInstance();
            ScApiSessionFactory.getSession(mRequestQueue, instance.url, instance.login, instance.password, this, this);
        }
    }

    @OnClick(R.id.button_save) void saveInstance() {
        if (mInstanceFragment.isFieldsValid()) {
            getLoaderManager().restartLoader(READ_NAMES_ACTION, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case READ_INSTANCES_ACTION:
                return new CursorLoader(this, mInstanceUri, Query.PROJECTION, null, null, null);
            case READ_NAMES_ACTION:
                Instance instance = mInstanceFragment.getEnteredInstance();
                String[] selectionArgs = new String[]{instance.url, instance.login,
                        instance.password, instance.rootFolder};
                return new CursorLoader(this, Instances.CONTENT_URI, null, SELECTION, selectionArgs, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case READ_INSTANCES_ACTION: {
                if (!data.moveToFirst()) return;
                mInstanceFragment.setSourceInstance(new Instance(data));
                break;
            }
            case READ_NAMES_ACTION: {
                if (data.moveToFirst()) {
                    Toast.makeText(this, R.string.text_instance_exists, Toast.LENGTH_LONG).show();
                } else {
                    saveInstanceToDB(mInstanceFragment.getEnteredInstance());
                    finish();
                }
                data.close();
                getLoaderManager().destroyLoader(READ_NAMES_ACTION);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void saveInstanceToDB(Instance instance) {
        if (mInstanceUri == null) {
            new AsyncQueryHandler(getContentResolver()) {
            }.startInsert(0, null, Instances.CONTENT_URI, instance.toContentValues());
        } else {
            new AsyncQueryHandler(getContentResolver()) {
            }.startUpdate(0, null, mInstanceUri, instance.toContentValues(), null, null);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, Utils.getMessageFromError(error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(ScApiSession session) {
        Listener<ItemsResponse> success = new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse response) {
                if (response.getTotalCount() != 0) {
                    Toast.makeText(CreateEditInstanceActivity.this, R.string.text_instance_is_valid, Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(CreateEditInstanceActivity.this, R.string.text_instance_is_not_valid,
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        mRequestQueue.add(session.readItemsRequest(success, this)
                .byItemPath(mInstanceFragment.getEnteredInstance().rootFolder)
                .build());
    }
}
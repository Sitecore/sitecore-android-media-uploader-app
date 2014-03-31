package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CreateEditInstanceActivity extends Activity implements LoaderCallbacks<Cursor>, ErrorListener, Listener<ScApiSession> {
    public static final int READ_INSTANCES_ACTION = 0;

    @InjectView(R.id.button_delete_instance) ImageButton mDeleteButton;
    @Inject ScRequestQueue mRequestQueue;
    @Inject UploaderPrefs mPrefs;

    private InstanceFragment mInstanceFragment;
    private Uri mInstanceUri;
    private boolean mIsInstanceSelected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_edit_instance);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        mInstanceUri = getIntent().getData();
        if (mInstanceUri != null) {
            getLoaderManager().initLoader(READ_INSTANCES_ACTION, null, this);
            setTitle(R.string.title_edit_instance);
        } else {
            mDeleteButton.setVisibility(View.INVISIBLE);
            setTitle(R.string.title_add_instance);
        }

        mInstanceFragment = (InstanceFragment) getFragmentManager().findFragmentById(R.id.instance_fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_delete_instance)
    public void deleteInstance() {
        if (mInstanceUri != null) {
            new AsyncQueryHandler(getContentResolver()) {
                @Override protected void onDeleteComplete(int token, Object cookie, int result) {
                    Toast.makeText(CreateEditInstanceActivity.this, R.string.toast_instance_deleted,
                            Toast.LENGTH_LONG).show();
                    if (mIsInstanceSelected) selectLastInstance();
                    finish();
                }
            }.startDelete(0, null, mInstanceUri, null, null);
        }
    }

    private void selectLastInstance() {
        new AsyncQueryHandler(getContentResolver()) {
            @Override protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor.getCount() > 0) {
                    if (cursor.moveToLast()) {
                        String id = cursor.getString(Query._ID);
                        ContentValues values = new ContentValues();
                        values.put(Instances.SELECTED, 1);

                        new AsyncQueryHandler(getContentResolver()) {
                        }.startUpdate(0, null, Instances.buildInstanceUri(id), values, null, null);

                        mPrefs.setSelectedInstance(new Instance(cursor));
                    }
                }
            }
        }.startQuery(0, null, Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    @OnClick(R.id.button_next) void checkConnection() {
        if (mInstanceFragment.isFieldsValid()) {
            Instance instance = mInstanceFragment.getEnteredInstance();
            ScApiSessionFactory.getSession(mRequestQueue, instance.getUrl(), instance.getLogin(),
                    instance.getPassword(), this, this);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case READ_INSTANCES_ACTION:
                return new CursorLoader(this, mInstanceUri, Query.PROJECTION, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case READ_INSTANCES_ACTION: {
                if (!data.moveToFirst()) return;
                Instance instance = new Instance(data);
                mIsInstanceSelected = instance.isSelected();
                mInstanceFragment.setSourceInstance(instance);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
                    Intent intent = new Intent(CreateEditInstanceActivity.this, ChooseMediaFolderActivity.class);
                    Instance enteredInstance = mInstanceFragment.getEnteredInstance();
                    if (mInstanceUri != null) {
                        intent.setData(mInstanceUri);
                    }
                    intent.putExtra(ChooseMediaFolderActivity.INSTANCE_KEY, enteredInstance);
                    startActivity(intent);
                } else {
                    Toast.makeText(CreateEditInstanceActivity.this, R.string.toast_instance_is_not_valid,
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        ScRequest request = session.readItemsRequest(success, this)
                .database(mInstanceFragment.getEnteredInstance().getDatabase()).build();
        mRequestQueue.add(request);
    }
}
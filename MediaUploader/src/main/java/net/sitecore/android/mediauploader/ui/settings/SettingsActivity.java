package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderOperation;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import javax.inject.Inject;

import java.util.ArrayList;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListAdapter;
import net.sitecore.android.mediauploader.util.UploaderPrefs;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class SettingsActivity extends Activity implements LoaderCallbacks<Cursor> {

    @InjectView(R.id.list_instances) ListView mList;
    @Inject UploaderPrefs mPrefs;

    private InstancesListAdapter mAdapter;
    private OnClickListener mNewSiteClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(SettingsActivity.this, CreateEditInstanceActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UploaderApp.from(this).inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        mList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        final View footerView = LayoutInflater.from(this).inflate(R.layout.button_new_site, null);
        footerView.setOnClickListener(mNewSiteClickListener);

        mList.addFooterView(footerView);
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPrefs.setSelectedInstance(new Instance(mAdapter.getCursor()));
                switchInstances(id);
            }
        });

        getLoaderManager().initLoader(0, null, this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }

    private void switchInstances(long id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(Instances.CONTENT_URI).withValue(Instances.SELECTED, 0)
                .build());
        ops.add(ContentProviderOperation.newUpdate(Instances.buildInstanceUri(String.valueOf(id)))
                .withValue(Instances.SELECTED, 1)
                .build());
        try {
            getContentResolver().applyBatch(UploadMediaContract.CONTENT_AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            LOGE(e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter = new InstancesListAdapter(this);
        mAdapter.swapCursor(data);
        mList.setAdapter(mAdapter)  ;

        while (data.moveToNext()) {
            if (data.getInt(Query.SELECTED) != 0) mList.setItemChecked(data.getPosition(), true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

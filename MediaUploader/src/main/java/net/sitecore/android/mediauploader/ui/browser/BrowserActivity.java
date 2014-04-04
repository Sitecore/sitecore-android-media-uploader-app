package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.ContentTreePositionListener;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.NetworkEventsListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BrowserActivity extends Activity implements ContentTreePositionListener, NetworkEventsListener,
        LoaderCallbacks<Cursor>, ErrorListener {

    @Inject ScRequestQueue mRequestQueue;
    @InjectView(R.id.text_title) TextView mHeaderText;
    @InjectView(R.id.instances_spinner) Spinner mInstancesSpinner;

    private BrowserFragment mFragment;
    private InstancesSpinnerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_browser);
        UploaderApp.from(this).inject(this);
        ButterKnife.inject(this);

        mHeaderText = (TextView) findViewById(R.id.text_title);

        mInstancesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(position);
                initFragment(new Instance(cursor));
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getLoaderManager().initLoader(0, null, this);
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

    private void initFragment(final Instance instance) {
        mFragment = BrowserFragment.newInstance(instance.getUrl());
        mFragment.setContentTreePositionListener(BrowserActivity.this);
        mFragment.setNetworkEventsListener(BrowserActivity.this);
        mFragment.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).commitAllowingStateLoss();

        Listener<ScApiSession> successListener = new Listener<ScApiSession>() {
            @Override public void onResponse(final ScApiSession session) {
                if (!TextUtils.isEmpty(instance.getSite())) session.setDefaultSite(instance.getSite());
                session.setDefaultDatabase(instance.getDatabase());
                mFragment.loadContent(session);
            }
        };

        ScApiSessionFactory.getSession(mRequestQueue, instance.getUrl(), instance.getLogin(), instance.getPassword(),
                successListener, BrowserActivity.this);

        new AsyncQueryHandler(getContentResolver()) {
        }.startDelete(0, null, Items.CONTENT_URI, null, null);
    }

    @Override public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, Utils.getMessageFromError(error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGoUp(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onGoInside(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onInitialized(ScItem item) {
        mHeaderText.setText(item.getPath());
    }

    @Override
    public void onUpdateRequestStarted() {

    }

    @Override
    public void onUpdateSuccess(ItemsResponse itemsResponse) {
    }

    @Override
    public void onUpdateError(VolleyError volleyError) {
    }

    @Override public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    @Override public void onLoadFinished(Loader loader, Cursor data) {
        Instance instance = null;
        int selectedInstancePos = 0;
        while (data.moveToNext()) {
            if (data.getInt(Query.SELECTED) == 1) {
                instance = new Instance(data);
                selectedInstancePos = data.getPosition();
                break;
            }
        }
        if (data.getCount() == 1) {
            mInstancesSpinner.setVisibility(View.GONE);
        } else {
            mAdapter = new InstancesSpinnerAdapter(this);
            mAdapter.swapCursor(data);
            mInstancesSpinner.setAdapter(mAdapter);
            mInstancesSpinner.setSelection(selectedInstancePos);
        }

        if (instance != null) {
            initFragment(instance);
        }
    }

    @Override public void onLoaderReset(Loader loader) {

    }

    class InstancesSpinnerAdapter extends CursorAdapter {

        InstancesSpinnerAdapter(Context context) {
            super(context, null, false);
        }

        @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View v = LayoutInflater.from(context).inflate(R.layout.instance_drop_down_view, parent, false);
            final ViewHolder holder = new ViewHolder(v);
            if (v != null) {
                v.setTag(holder);
            }
            return v;
        }

        @Override public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.instanceUrl.setText(cursor.getString(Query.URL));
            holder.instanceFolder.setText(cursor.getString(Query.ROOT_FOLDER));
        }

        class ViewHolder {
            @InjectView(R.id.instance_url) TextView instanceUrl;
            @InjectView(R.id.instance_root_folder) TextView instanceFolder;

            ViewHolder(View parent) {
                ButterKnife.inject(this, parent);
            }
        }
    }
}

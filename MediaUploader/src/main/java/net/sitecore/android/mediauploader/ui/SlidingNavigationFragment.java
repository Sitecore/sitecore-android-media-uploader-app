package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListFragment.OnDefaultInstanceChangedListener;
import net.sitecore.android.mediauploader.util.Utils;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

public class SlidingNavigationFragment extends Fragment implements LoaderCallbacks<Cursor>, OnDefaultInstanceChangedListener {

    public static final int POSITION_UPLOAD = 0;
    public static final int POSITION_MEDIA_BROWSER = 1;
    public static final int POSITION_MY_UPLOADS = 2;
    public static final int POSITION_INSTANCES = 3;

    interface Callbacks {

        public void onMediaBrowserSelected();
        public void onMyUploadsSelected();
        public void onInstanceManagerSelected();

        public void onSelectionDone();

    }

    @InjectView(R.id.spinner_instances) Spinner mInstances;

    private Callbacks mCallbacks;
    private int mCurrentPosition = POSITION_MEDIA_BROWSER;
    private InstancesAdapter mInstancesAdapter;

    @Override
    public void instanceChanged(String name) {
        Cursor data = mInstancesAdapter.getCursor();
        while (data.moveToNext()) {
            if (data.getString(Query.NAME).equals(name)) {
                mInstances.setSelection(data.getPosition());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_navigation, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks) mCallbacks = (Callbacks) activity;
        else throw new IllegalArgumentException("Activity must implement SlidingNavigationFragment.Callbacks");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mInstancesAdapter = new InstancesAdapter(getActivity());
        mInstances.setAdapter(mInstancesAdapter);
        mInstances.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mInstancesAdapter.getCursor();
                cursor.moveToPosition(position);

                String name = cursor.getString(Query.NAME);
                String url = cursor.getString(Query.URL);
                String login = cursor.getString(Query.LOGIN);
                String password = cursor.getString(Query.PASSWORD);
                String folder = cursor.getString(Query.ROOT_FOLDER);

                getActivity().getContentResolver().notifyChange(Instances.CONTENT_URI, null);

                Utils.setDefaultInstance(getActivity(), name, url, login, password, folder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    public boolean isMediaBrowserSelected() {
        return mCurrentPosition == POSITION_MEDIA_BROWSER;
    }

    @OnClick(R.id.button_media_browser)
    public void onMediaBrowserClick() {
        if (mCurrentPosition != POSITION_MEDIA_BROWSER) {
            mCallbacks.onMediaBrowserSelected();
            mCurrentPosition = POSITION_MEDIA_BROWSER;
        }
        mCallbacks.onSelectionDone();
    }

    @OnClick(R.id.button_my_uploads)
    public void onMyUploadsClick() {
        if (mCurrentPosition != POSITION_MY_UPLOADS) {
            mCallbacks.onMyUploadsSelected();
            mCurrentPosition = POSITION_MY_UPLOADS;
        }
        mCallbacks.onSelectionDone();
    }

    @OnClick(R.id.button_instances_manager)
    public void onInstancesManagerClick() {
        if (mCurrentPosition != POSITION_INSTANCES) {
            mCallbacks.onInstanceManagerSelected();
            mCurrentPosition = POSITION_INSTANCES;
        }
        mCallbacks.onSelectionDone();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mInstancesAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInstancesAdapter.swapCursor(null);
    }

    static class InstancesAdapter extends CursorAdapter {

        public InstancesAdapter(Context context) {
            super(context, null, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            final ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(cursor.getString(Query.NAME));
        }

        static class ViewHolder {
            @InjectView(android.R.id.text1) TextView name;

            ViewHolder(View v) {
                Views.inject(this, v);
            }
        }
    }
}

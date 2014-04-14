package net.sitecore.android.mediauploader.ui.settings;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderOperation;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import java.util.ArrayList;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.provider.UploadMediaContract;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class InstancesListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private InstancesListAdapter mAdapter;
    private OnClickListener mNewSiteClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getActivity(), CreateEditInstanceActivity.class));
        }
    };

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.button_new_site, null);
        footerView.setOnClickListener(mNewSiteClickListener);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().addFooterView(footerView);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        updateSelectedInstanceId(id);
        UploaderApp.from(getActivity()).cleanInstanceCacheAsync();
    }

    private void updateSelectedInstanceId(long id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(Instances.CONTENT_URI).withValue(Instances.SELECTED, 0)
                .build());
        ops.add(ContentProviderOperation.newUpdate(Instances.buildInstanceUri(String.valueOf(id)))
                .withValue(Instances.SELECTED, 1)
                .build());
        try {
            getActivity().getContentResolver().applyBatch(UploadMediaContract.CONTENT_AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            LOGE(e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new InstancesListAdapter(getActivity());
            setListAdapter(mAdapter);
        }

        int selectedPosition = 0;
        while (data.moveToNext()) {
            if (data.getInt(Query.SELECTED) != 0) selectedPosition = data.getPosition();
        }
        mAdapter.swapCursor(data);
        getListView().setItemChecked(selectedPosition, true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

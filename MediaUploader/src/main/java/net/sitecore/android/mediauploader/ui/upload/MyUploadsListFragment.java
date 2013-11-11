package net.sitecore.android.mediauploader.ui.upload;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.ui.ScFragment;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MyUploadsListFragment extends ScFragment implements LoaderCallbacks<Cursor> {

    @InjectView(android.R.id.list) ListView mListView;

    private UploadsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.onCreateView(inflater, container, savedInstanceState);
        Views.inject(this, v);

        return v;
    }

    @Override
    protected View onCreateEmptyView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_uploads_empty, null, false);
    }

    @OnClick(R.id.button_start_upload)
    public void onStartUploadClick() {
        final Intent intent = new Intent(getActivity(), UploadActivity.class);
        intent.putExtra(IntentExtras.ITEM_PATH, "/temp/hardcoded/path");
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new UploadsListAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uploads.CONTENT_URI, Uploads.Query.PROJECTION, null, null,
                Uploads.Query.ORDER_BY_STATUS);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LOGD("Loaded " + data.getCount() + " uploads.");
        mAdapter.swapCursor(data);
        if (data.getCount() > 0) {
            setContentShown(true);
        } else {
            setEmpty(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
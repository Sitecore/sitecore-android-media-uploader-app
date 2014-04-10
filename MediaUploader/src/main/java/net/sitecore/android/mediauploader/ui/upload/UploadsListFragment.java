package net.sitecore.android.mediauploader.ui.upload;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UploadsListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    @Inject Picasso mImageLoader;

    private UploadsCursorAdapter mAdapter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UploaderApp.from(getActivity()).inject(this);

        setEmptyText("No uploads");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uploads.CONTENT_URI, Query.PROJECTION, null, null,
                Query.ORDWE_BY_TIME_ADDED);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new UploadsCursorAdapter(getActivity());
            setListAdapter(mAdapter);
        }
        mAdapter.swapCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    class UploadsCursorAdapter extends CursorAdapter {

        public UploadsCursorAdapter(Context context) {
            super(context, null, true);
        }

        @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.fragment_uploads_list, parent, false);
            ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            return v;
        }

        @Override public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            final String itemUri = cursor.getString(Query.FILE_URI);
            final String name = cursor.getString(Query.ITEM_NAME);
            String url = cursor.getString(Query.URL);
            String path = cursor.getString(Query.ITEM_PATH);
            UploadStatus status = UploadStatus.valueOf(cursor.getString(Query.STATUS));

            mImageLoader.load(itemUri).resize(100, 100).placeholder(R.drawable.ic_placeholder).into(holder.preview);
            holder.name.setText(name);
            holder.url.setText(url);
            holder.path.setText(path);

            switch (status) {
                case DONE:
                    holder.statusButton.setImageResource(R.drawable.ic_upload_success);
                    holder.inProgress.setVisibility(View.GONE);
                    holder.statusButton.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    holder.statusButton.setImageResource(R.drawable.ic_upload_error);
                    holder.statusButton.setVisibility(View.VISIBLE);
                    holder.inProgress.setVisibility(View.GONE);
                    break;
                case IN_PROGRESS:
                    holder.inProgress.setVisibility(View.VISIBLE);
                    holder.statusButton.setVisibility(View.GONE);
                    break;
                case PENDING:
                    holder.statusButton.setVisibility(View.VISIBLE);
                    holder.statusButton.setImageResource(R.drawable.ic_upload);
                    holder.inProgress.setVisibility(View.GONE);
                    break;
            }

            holder.preview.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UploadedItemActivity.class);
                    intent.putExtra(UploadedItemActivity.EXTRA_IMAGE_URI, itemUri);
                    intent.putExtra(UploadedItemActivity.EXTRA_ITEM_NAME, name);
                    startActivity(intent);
                }
            });
        }
    }

    static class ViewHolder {
        @InjectView(R.id.upload_preview) ImageView preview;
        @InjectView(R.id.upload_name) TextView name;
        @InjectView(R.id.upload_url) TextView url;
        @InjectView(R.id.upload_path) TextView path;
        @InjectView(R.id.upload_status) ImageView statusButton;
        @InjectView(R.id.upload_in_progress) ProgressBar inProgress;

        ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }
}
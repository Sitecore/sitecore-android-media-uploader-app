package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.UploadWithInstanceQuery;
import net.sitecore.android.mediauploader.model.ImageResizer;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;

public class UploadsListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final int IMAGE_PREVIEW_WIDTH = 200;
    private static final int IMAGE_PREVIEW_HEIGHT = 200;

    public interface UploadsListCallbacks {
        public void onStartPendingUpload(String uploadId);

        public void onErrorUploadClicked(String uploadId, String itemName, String failMessage);
    }

    private UploadsListCallbacks mCallbacks;
    private UploadsCursorAdapter mAdapter;

    private String mSelection;
    @Inject Picasso mImageLoader;

    public UploadsListFragment() {
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (UploadsListCallbacks) activity;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UploaderApp.from(getActivity()).inject(this);

        setEmptyText("No uploads");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uploads.UPLOADS_JOIN_INSTANCE_URI, UploadWithInstanceQuery.PROJECTION,
                mSelection, null, Query.ORDER_BY_TIME_ADDED);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new UploadsCursorAdapter(getActivity(), mCallbacks, mImageLoader);
            setListAdapter(mAdapter);
        }
        mAdapter.swapCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void showAllUploads() {
        mSelection = null;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void showCompletedUploads() {
        mSelection = Query.SELECTION_COMPLETED_UPLOADS;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void showNotCompletedUploads() {
        mSelection = Query.SELECTION_NOT_COMPLETED_UPLOADS;
        getLoaderManager().restartLoader(0, null, this);
    }

    static class UploadsCursorAdapter extends CursorAdapter {

        private final UploadsListCallbacks mCallbacks;
        private final Picasso mImageLoader;
        private final ImageResizer mImageResizer;

        public UploadsCursorAdapter(Context context, UploadsListCallbacks callbacks, Picasso imageLoader) {
            super(context, null, false);
            mCallbacks = callbacks;
            mImageLoader = imageLoader;
            mImageResizer = new ImageResizer(context);
        }

        @Override public View newView(final Context context, Cursor cursor, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_item_upload, parent, false);
            final ViewHolder holder = new ViewHolder(v);
            holder.statusButton.setOnClickListener(v1 -> {
                switch (holder.status) {
                    case UPLOAD_LATER:
                        mCallbacks.onStartPendingUpload(holder.uploadId);
                        break;

                    case ERROR:
                        mCallbacks.onErrorUploadClicked(holder.uploadId, holder.itemName, holder.failMessage);
                        break;
                }
            });
            v.setTag(holder);
            return v;
        }

        @Override public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.imageUri = cursor.getString(UploadWithInstanceQuery.FILE_URI);
            holder.itemName = cursor.getString(UploadWithInstanceQuery.ITEM_NAME);
            holder.uploadId = cursor.getString(UploadWithInstanceQuery._ID);
            holder.status = UploadStatus.valueOf(cursor.getString(UploadWithInstanceQuery.STATUS));
            holder.failMessage = cursor.getString(UploadWithInstanceQuery.FAIL_MESSAGE);

            final boolean isImage = cursor.getInt(UploadWithInstanceQuery.IS_IMAGE) == 1;
            if (isImage) {
                loadImageThumbnail(holder);
            } else {
                holder.preview.setImageResource(R.drawable.ic_template_video);
            }

            holder.name.setText(holder.itemName);
            holder.url.setText(cursor.getString(UploadWithInstanceQuery.URL));
            holder.path.setText(cursor.getString(UploadWithInstanceQuery.ROOT_FOLDER));

            switch (holder.status) {
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
                case UPLOAD_LATER:
                    holder.inProgress.setVisibility(View.GONE);
                    holder.statusButton.setImageResource(R.drawable.ic_upload);
                    holder.statusButton.setVisibility(View.VISIBLE);
                    break;
                case PENDING:
                    holder.statusButton.setVisibility(View.GONE);
                    holder.inProgress.setVisibility(View.VISIBLE);
                    break;
            }
        }

        private void loadImageThumbnail(ViewHolder holder) {
            mImageLoader.load(holder.imageUri)
                    .fit().centerInside()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_action_cancel)
                    .into(holder.preview);
        }
    }

    static class ViewHolder {
        @InjectView(R.id.upload_preview) ImageView preview;
        @InjectView(R.id.upload_name) TextView name;
        @InjectView(R.id.upload_url) TextView url;
        @InjectView(R.id.upload_path) TextView path;
        @InjectView(R.id.image_status) ImageView statusIcon;
        @InjectView(R.id.button_status_action) ImageButton statusButton;
        @InjectView(R.id.upload_in_progress) ProgressBar inProgress;

        public String imageUri;
        public String itemName;
        public String uploadId;
        public String failMessage;
        public UploadStatus status;

        ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

}
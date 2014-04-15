package net.sitecore.android.mediauploader.ui.upload;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import net.sitecore.android.mediauploader.provider.UploadsAsyncHandler;
import net.sitecore.android.mediauploader.util.StartUploadTask;

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
                Query.ORDER_BY_TIME_ADDED);
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

    private static void startUpload(final Context context, final String id) {
        new UploadsAsyncHandler(context.getContentResolver()){
            @Override protected void onUpdateComplete(int token, Object cookie, int result) {
                new StartUploadTask(context).execute(id);
            }
        }.updateUploadStatus(id, UploadStatus.PENDING);
    }

    class UploadsCursorAdapter extends CursorAdapter {

        public UploadsCursorAdapter(Context context) {
            super(context, null, true);
        }

        @Override public View newView(final Context context, Cursor cursor, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_uploads_item, parent, false);
            final ViewHolder holder = new ViewHolder(v);
            holder.preview.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    Intent intent = new Intent(context, UploadedItemActivity.class);
                    intent.putExtra(UploadedItemActivity.EXTRA_IMAGE_URI, holder.imageUri);
                    intent.putExtra(UploadedItemActivity.EXTRA_ITEM_NAME, holder.itemName);
                    context.startActivity(intent);
                }
            });

            holder.statusButton.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    switch (holder.status) {
                        case UPLOAD_LATER:
                            startUpload(context, holder.id);
                            break;

                        case ERROR:
                            RetryDialogFragment dialogFragment = RetryDialogFragment.newInstance(holder.itemName,
                                    holder.id, holder.failMessage);
                            dialogFragment.show(getFragmentManager(), "dialog");
                            break;
                    }
                }
            });
            v.setTag(holder);
            return v;
        }

        @Override public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.imageUri = cursor.getString(Query.FILE_URI);
            holder.itemName = cursor.getString(Query.ITEM_NAME);
            holder.id = cursor.getString(Query._ID);
            holder.status = UploadStatus.valueOf(cursor.getString(Query.STATUS));
            holder.failMessage = cursor.getString(Query.FAIL_MESSAGE);

            mImageLoader.load(holder.imageUri).resize(150, 150)
                    .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_action_cancel)
                    .into(holder.preview);

            holder.name.setText(holder.itemName);

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
    }

    static class ViewHolder {
        @InjectView(R.id.upload_preview) ImageView preview;
        @InjectView(R.id.upload_name) TextView name;
        @InjectView(R.id.upload_url) TextView url;
        @InjectView(R.id.upload_path) TextView path;
        @InjectView(R.id.upload_status) ImageView statusButton;
        @InjectView(R.id.upload_in_progress) ProgressBar inProgress;

        public String imageUri;
        public String itemName;
        public String id;
        public String failMessage;
        public UploadStatus status;

        ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

    static class RetryDialogFragment extends DialogFragment {

        private static final String EXTRA_UPLOAD_ID = "id";
        private static final String EXTRA_FAIL_MESSAGE = "message";
        private static final String EXTRA_ITEM_NAME = "name";

        public static RetryDialogFragment newInstance(String name, String uploadId, String failMessage) {
            RetryDialogFragment frag = new RetryDialogFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_UPLOAD_ID, uploadId);
            args.putString(EXTRA_FAIL_MESSAGE, failMessage);
            args.putString(EXTRA_ITEM_NAME, name);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String id = getArguments().getString(EXTRA_UPLOAD_ID);
            String failMessage = getArguments().getString(EXTRA_FAIL_MESSAGE);
            String name = getArguments().getString(EXTRA_ITEM_NAME);

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_upload_error)
                    .setTitle(name)
                    .setMessage(failMessage)
                    .setPositiveButton(R.string.text_retry,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    startUpload(getActivity(), id);
                                }
                            }
                    )
                    .setNegativeButton(R.string.text_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dismiss();
                                }
                            }
                    )
                    .create();
        }
    }
}
package net.sitecore.android.mediauploader.ui.upload;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;

import butterknife.InjectView;
import butterknife.Views;

public class UploadsListAdapter extends CursorAdapter implements OnMenuItemClickListener {

    interface OnUploadActionsListener {

        void onResumePendingUpload();

        void onCancelInProgressUpload();

        void onRetryErrorUpload();

        void onRemoveUpload();
    }

    OnUploadActionsListener mActionsListener;

    public UploadsListAdapter(Context context, OnUploadActionsListener actionsListener) {
        super(context, null, false);
        mActionsListener = actionsListener;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                mActionsListener.onCancelInProgressUpload();
                return true;

            case R.id.action_upload:
                mActionsListener.onResumePendingUpload();
                return true;

            case R.id.action_remove:
                mActionsListener.onRemoveUpload();
                return true;

            case R.id.action_retry:
                mActionsListener.onRetryErrorUpload();
                return true;

            default:
                return false;
        }
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_upload, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        v.setTag(holder);

        holder.more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.popup.show();
            }
        });
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final UploadStatus status = UploadStatus.valueOf(cursor.getString(Query.STATUS));

        holder.progress.setVisibility(status == UploadStatus.IN_PROGRESS
                ? View.VISIBLE
                : View.GONE);

        holder.popup = newPopupMenuForStatus(context, holder.more, status);
        if (holder.popup != null) holder.popup.setOnMenuItemClickListener(this);
        holder.more.setVisibility(holder.popup == null
                ? View.INVISIBLE
                : View.VISIBLE);

        holder.name.setText(cursor.getString(Query.ITEM_NAME));
        holder.path.setText(cursor.getString(Query.ITEM_PATH));
        holder.status.setText(status.name());
    }

    private PopupMenu newPopupMenuForStatus(Context context, View view, UploadStatus uploadStatus) {
        PopupMenu popupMenu = null;
        if (uploadStatus == UploadStatus.DONE) {
            popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.upload_item_done);
        } else if (uploadStatus == UploadStatus.ERROR) {
            popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.upload_item_error);
        }
        if (uploadStatus == UploadStatus.PENDING) {
            popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.upload_item_pending);
        }
        if (uploadStatus == UploadStatus.IN_PROGRESS) {
            popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.upload_item_in_progress);
        }

        return popupMenu;
    }

    static class ViewHolder {
        @InjectView(R.id.text_upload_name) TextView name;
        @InjectView(R.id.text_upload_path) TextView path;
        @InjectView(R.id.text_upload_status) TextView status;
        @InjectView(R.id.button_more) ImageButton more;
        @InjectView(R.id.progress) ProgressBar progress;

        PopupMenu popup;

        ViewHolder(View v) {
            Views.inject(this, v);
        }
    }

}

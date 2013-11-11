package net.sitecore.android.mediauploader.ui.upload;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;

import butterknife.InjectView;
import butterknife.Views;

public class UploadsListAdapter extends CursorAdapter {

    public UploadsListAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_upload, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(cursor.getString(Query.ITEM_NAME));
        holder.path.setText(cursor.getString(Query.ITEM_PATH));
        holder.status.setText(cursor.getString(Query.STATUS));
    }

    class ViewHolder {
        @InjectView(R.id.text_upload_name) TextView name;
        @InjectView(R.id.text_upload_path) TextView path;
        @InjectView(R.id.text_upload_status) TextView status;

        ViewHolder(View v) {
            Views.inject(this, v);
        }

    }

}

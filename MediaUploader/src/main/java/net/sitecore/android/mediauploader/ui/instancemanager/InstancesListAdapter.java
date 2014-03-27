package net.sitecore.android.mediauploader.ui.instancemanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstancesListAdapter extends CursorAdapter {

    public InstancesListAdapter(Context context, Cursor c) {
        super(context, c, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_instance, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        if (v != null) {
            v.setTag(holder);
        }
        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String url = c.getString(c.getColumnIndex(Instances.URL));
        holder.instanceUrl.setText(url);
    }

    class ViewHolder {
        @InjectView(R.id.text_instance_url) TextView instanceUrl;

        ViewHolder(View parent) {
            ButterKnife.inject(this, parent);
        }
    }
}

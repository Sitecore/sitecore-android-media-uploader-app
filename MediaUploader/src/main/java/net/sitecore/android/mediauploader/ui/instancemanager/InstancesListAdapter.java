package net.sitecore.android.mediauploader.ui.instancemanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

import butterknife.InjectView;
import butterknife.Views;

public class InstancesListAdapter extends CursorAdapter {

    public InstancesListAdapter(Context context) {
        super(context, null, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.layout_instance_item, parent, false);
        v.setTag(new ViewHolder(v));
        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.instanceName.setText(c.getString(Query.URL));
        holder.deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    class ViewHolder {
        @InjectView(R.id.instance_name) TextView instanceName;
        @InjectView(R.id.button_instance_delete) ImageButton deleteButton;

        ViewHolder(View parent) {
            Views.inject(this, parent);
        }
    }
}

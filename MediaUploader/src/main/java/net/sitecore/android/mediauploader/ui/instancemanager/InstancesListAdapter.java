package net.sitecore.android.mediauploader.ui.instancemanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.settings.CreateEditInstanceActivity;

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
        holder.editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateEditInstanceActivity.class);
                intent.setData(Instances.buildInstanceUri(c.getString(Query._ID)));
                context.startActivity(intent);
            }
        });
    }

    class ViewHolder {
        @InjectView(R.id.text_instance_url) TextView instanceUrl;
        @InjectView(R.id.radio_button_default_instance) RadioButton defaultInstance;
        @InjectView(R.id.button_edit_instance) ImageButton editButton;

        ViewHolder(View parent) {
            ButterKnife.inject(this, parent);
        }
    }
}

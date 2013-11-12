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
import net.sitecore.android.mediauploader.util.Utils;

import butterknife.InjectView;
import butterknife.Views;

public class InstancesListAdapter extends CursorAdapter {
    private OnDeleteButtonClicked mDeleteButtonClicked;

    interface OnDeleteButtonClicked {
        public void click(String name);
    }

    public InstancesListAdapter(Context context, OnDeleteButtonClicked listener) {
        super(context, null, true);
        mDeleteButtonClicked = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_instance, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        holder.deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteButtonClicked.click(holder.instanceName.getText().toString());
            }
        });
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (Utils.isDefaultInstance(context, c.getString(Query.NAME))) {
            holder.defaultInstance.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.defaultInstance.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        }
        holder.instanceName.setText(c.getString(Query.NAME));
        holder.instanceUrl.setText(c.getString(Query.URL));
    }

    class ViewHolder {
        @InjectView(R.id.instance_name) TextView instanceName;
        @InjectView(R.id.instance_url) TextView instanceUrl;
        @InjectView(R.id.button_instance_delete) ImageButton deleteButton;
        @InjectView(R.id.checkbox_default_instance) View defaultInstance;

        ViewHolder(View parent) {
            Views.inject(this, parent);
        }
    }
}

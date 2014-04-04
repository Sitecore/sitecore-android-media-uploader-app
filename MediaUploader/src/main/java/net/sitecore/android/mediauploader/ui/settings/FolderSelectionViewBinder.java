package net.sitecore.android.mediauploader.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FolderSelectionViewBinder implements ItemViewBinder {
    @Override
    public void bindView(Context context, View v, ScItem item) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.name.setText(item.getDisplayName());
    }

    @Override
    public View newView(Context context, ViewGroup parent, LayoutInflater inflater, ScItem item) {
        View v = LayoutInflater.from(context).inflate(R.layout.media_selection_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        if (v != null) {
            v.setTag(holder);
        }
        return v;
    }

    class ViewHolder {
        @InjectView(R.id.item_name) TextView name;

        ViewHolder(View parent) {
            ButterKnife.inject(this, parent);
        }
    }
}
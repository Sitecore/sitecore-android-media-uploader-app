package net.sitecore.android.mediauploader.ui.browser;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.Views;

class ItemsCursorAdapter extends CursorAdapter {

    private ImageLoader mImageLoader;

    public ItemsCursorAdapter(Context context) {
        super(context, null, false);
        mImageLoader = UploaderApp.from(context).getImageLoader();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.layout_browser_item, parent, false);
        v.setTag(new ViewHolder(v));
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.itemName.setText(c.getString(Query.DISPLAY_NAME));
        if (ScUtils.isImage(c.getString(Query.TEMPLATE))) {
            String url = ScUtils.getMediaDownloadUrl(c.getString(Query.ITEM_ID));
            holder.itemIcon.setImageUrl(url, mImageLoader);
        } else {
            holder.itemIcon.setImageDrawable(null);
        }
    }

    class ViewHolder {
        @InjectView(R.id.item_name) TextView itemName;
        @InjectView(R.id.item_icon) NetworkImageView itemIcon;

        ViewHolder(View parent) {
            Views.inject(this, parent);
        }
    }


}
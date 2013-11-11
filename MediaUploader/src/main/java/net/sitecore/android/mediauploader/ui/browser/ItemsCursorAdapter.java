package net.sitecore.android.mediauploader.ui.browser;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.ScUtils.MediaParamsBuilder;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.Views;

class ItemsCursorAdapter extends CursorAdapter {
    private static final int PREVIEW_IMAGE_ICON_WIDTH = 50;
    private static final int PREVIEW_IMAGE_ICON_HEIGHT = 0;

    private Picasso mImageLoader;


    public ItemsCursorAdapter(Context context) {
        super(context, null, false);
        mImageLoader = UploaderApp.from(context).getImageLoader();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_browser, parent, false);
        v.setTag(new ViewHolder(v));
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.itemName.setText(c.getString(Query.DISPLAY_NAME));
        if (ScUtils.isImage(c.getString(Query.TEMPLATE))) {
            MediaParamsBuilder params = new MediaParamsBuilder().height(PREVIEW_IMAGE_ICON_HEIGHT)
                    .width(PREVIEW_IMAGE_ICON_WIDTH);
            final String url = ScUtils.getMediaDownloadUrl(context, c.getString(Query.ITEM_ID), params);
            mImageLoader.load(url).placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_action_cancel)
                    .into(holder.itemIcon);
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_sc_folder);
        }
    }

    class ViewHolder {
        @InjectView(R.id.item_name) TextView itemName;
        @InjectView(R.id.item_icon) ImageView itemIcon;

        ViewHolder(View parent) {
            Views.inject(this, parent);
        }
    }


}

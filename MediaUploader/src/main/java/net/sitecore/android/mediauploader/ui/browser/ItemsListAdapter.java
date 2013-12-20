package net.sitecore.android.mediauploader.ui.browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.util.MediaParamsBuilder;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.widget.ItemViewBinder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ItemsListAdapter implements ItemViewBinder {
    private static final int PREVIEW_IMAGE_ICON_WIDTH = 64;
    private static final int PREVIEW_IMAGE_ICON_HEIGHT = 0;

    public static final MediaParamsBuilder IMAGE_PREVIEW_PARAMS = new MediaParamsBuilder()
            .height(PREVIEW_IMAGE_ICON_HEIGHT)
            .width(PREVIEW_IMAGE_ICON_WIDTH);

    private Picasso mImageLoader;

    public ItemsListAdapter(Picasso imageLoader) {
        mImageLoader = imageLoader;
    }

    @Override
    public void bindView(Context context, View v, ScItem item) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        final String name = item.getDisplayName();
        final String template = item.getTemplate();

        holder.itemName.setText(name);
        holder.template.setText(template);

        if (ScUtils.isImageTemplate(template)) {
            final String url = ScUtils.getMediaDownloadUrl(context, item.getId(), IMAGE_PREVIEW_PARAMS);
            mImageLoader.load(url).placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_action_cancel)
                    .into(holder.itemIcon);
        } else {
            mImageLoader.cancelRequest(holder.itemIcon);
            holder.itemIcon.setImageResource(R.drawable.ic_sc_folder);
        }
    }

    @Override
    public View newView(Context context, ViewGroup parent, LayoutInflater inflater, ScItem item) {
        final View v = LayoutInflater.from(context).inflate(R.layout.list_item_media, parent, false);
        v.setTag(new ViewHolder(v));
        return v;
    }

    class ViewHolder {
        @InjectView(R.id.item_name) TextView itemName;
        @InjectView(R.id.item_template) TextView template;
        @InjectView(R.id.item_icon) ImageView itemIcon;

        ViewHolder(View parent) {
            ButterKnife.inject(this, parent);
        }
    }


}

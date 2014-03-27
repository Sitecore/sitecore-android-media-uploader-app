package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.DownloadMediaOptions;
import net.sitecore.android.sdk.api.DownloadMediaOptions.Builder;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;
import net.sitecore.android.sdk.ui.ItemsGridBrowserFragment;

public class BrowserFragment extends ItemsGridBrowserFragment {

    public static final DownloadMediaOptions IMAGE_OPTIONS = new Builder()
            .maxWidth(200)
            .maxHeight(200)
            .build();

    private ScApiSession mSession;

    @Override
    protected View onCreateUpButtonView(LayoutInflater inflater) {
        return new Button(getActivity());
    }

    @Override
    protected ItemViewBinder onCreateItemViewBinder() {
        final BrowserItemViewBinder viewBinder = new BrowserItemViewBinder();
        UploaderApp.from(getActivity()).inject(viewBinder);
        return viewBinder;
    }

    @Override
    public void onScItemClick(ScItem item) {
        if (ScUtils.isImageTemplate(item.getTemplate())) {
            Intent intent = new Intent(getActivity(), PreviewActivity.class);
            intent.putExtra(IntentExtras.PARENT_ITEM_ID, item.getParentItemId());
            intent.putExtra(IntentExtras.CURRENT_ITEM_ID, item.getId());
            startActivity(intent);
        } else {
            super.onScItemClick(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSession = UploaderApp.from(activity).getSession();
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }

    public static class BrowserItemViewBinder implements ItemViewBinder {

        @Inject ScApiSession mApiSession;
        @Inject Resources mResources;
        @Inject Picasso mImageLoader;

        @Override
        public void bindView(Context context, View v, ScItem item) {
            ViewHolder holder = (ViewHolder) v.getTag();

            holder.name.setText(item.getDisplayName());
            if (ScUtils.isImageTemplate(item.getTemplate())) {

                String itemUrl = item.getMediaDownloadUrl(IMAGE_OPTIONS);
                //TODO: inject prefs instead of session
                mImageLoader.load(mApiSession.getBaseUrl() + itemUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_action_cancel)
                        .into(holder.icon);
            } else {
                holder.icon.setImageDrawable(mResources.getDrawable(R.drawable.ic_sc_folder));
            }
        }

        @Override
        public View newView(Context context, ViewGroup parent, LayoutInflater inflater, ScItem item) {
            View v = inflater.inflate(R.layout.browser_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.icon = (ImageView) v.findViewById(R.id.item_icon);
            holder.name = (TextView) v.findViewById(R.id.item_name);
            v.setTag(holder);

            return v;
        }
    }
}
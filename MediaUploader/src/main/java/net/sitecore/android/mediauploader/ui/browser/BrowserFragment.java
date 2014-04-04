package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;
import net.sitecore.android.sdk.ui.ItemsGridBrowserFragment;

public class BrowserFragment extends ItemsGridBrowserFragment {

    @Override
    protected View onCreateUpButtonView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.layout_up_button, null);
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
    }

}
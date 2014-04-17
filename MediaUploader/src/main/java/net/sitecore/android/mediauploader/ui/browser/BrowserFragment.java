package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.SortByMediaFolderTemplateComparator;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;
import net.sitecore.android.sdk.ui.ItemsGridBrowserFragment;

public class BrowserFragment extends ItemsGridBrowserFragment {
    public static final String INSTANCE_URL = "url";
    public static final String INSTANCE_DATABASE = "database";

    public static BrowserFragment newInstance(String url, String database) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(INSTANCE_URL, url);
        args.putString(INSTANCE_DATABASE, database);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setItemsSortOrder(new SortByMediaFolderTemplateComparator());
    }

    @Override
    protected View onCreateUpButtonView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.layout_up_button, null);
    }

    @Override
    protected ItemViewBinder onCreateItemViewBinder() {
        final BrowserItemViewBinder viewBinder = new BrowserItemViewBinder(getArguments().getString(INSTANCE_URL, ""),
                getArguments().getString(INSTANCE_DATABASE, ""));
        UploaderApp.from(getActivity()).inject(viewBinder);
        return viewBinder;
    }

    @Override
    public void onScItemClick(ScItem item) {
        if (ScUtils.isImageTemplate(item.getTemplate())) {
            Intent intent = new Intent(getActivity(), PreviewActivity.class);
            intent.putExtra(PreviewActivity.PARENT_ITEM_ID, item.getParentItemId());
            intent.putExtra(PreviewActivity.CURRENT_ITEM_ID, item.getId());
            intent.putExtra(INSTANCE_URL, getArguments().getString(INSTANCE_URL, ""));
            intent.putExtra(INSTANCE_DATABASE, getArguments().getString(INSTANCE_DATABASE, ""));
            startActivity(intent);
        } else {
            super.onScItemClick(item);
        }
    }

}
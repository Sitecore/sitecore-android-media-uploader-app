package net.sitecore.android.mediauploader.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;
import net.sitecore.android.sdk.ui.ItemsListBrowserFragment;

public class FoldersBrowserListFragment extends ItemsListBrowserFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override protected ItemViewBinder onCreateItemViewBinder() {
        return super.onCreateItemViewBinder();
    }

    static class FoldersViewBinder implements ItemViewBinder {

        @Override public void bindView(Context context, View view, ScItem scItem) {

        }

        @Override
        public View newView(Context context, ViewGroup viewGroup, LayoutInflater layoutInflater, ScItem scItem) {
            return null;
        }
    }
}

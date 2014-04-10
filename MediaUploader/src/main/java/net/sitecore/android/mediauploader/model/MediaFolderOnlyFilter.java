package net.sitecore.android.mediauploader.model;

import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.model.ScItemsLoaderFilter;

public class MediaFolderOnlyFilter implements ScItemsLoaderFilter {

    @Override
    public boolean shouldShow(ScItem item) {
        return item.getTemplate().equals("System/Media/Media folder");
    }
}
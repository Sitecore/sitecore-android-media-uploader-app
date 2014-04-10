package net.sitecore.android.mediauploader.model;

import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.model.ScItemsLoaderFilter;

import static net.sitecore.android.mediauploader.util.ScUtils.TEMPLATE_MEDIA_FOLDER;

public class MediaFolderOnlyFilter implements ScItemsLoaderFilter {

    @Override
    public boolean shouldShow(ScItem item) {
        return item.getTemplate().equals(TEMPLATE_MEDIA_FOLDER);
    }
}
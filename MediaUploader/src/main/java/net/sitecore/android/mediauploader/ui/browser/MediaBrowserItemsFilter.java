package net.sitecore.android.mediauploader.ui.browser;

import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.model.ScItemsLoaderFilter;

import static net.sitecore.android.mediauploader.util.ScUtils.TEMPLATE_MEDIA_FOLDER;
import static net.sitecore.android.mediauploader.util.ScUtils.isFileTemplate;
import static net.sitecore.android.mediauploader.util.ScUtils.isImageTemplate;

public class MediaBrowserItemsFilter implements ScItemsLoaderFilter {

    @Override
    public boolean shouldShow(ScItem item) {
        final String template = item.getTemplate();

        return isImageTemplate(template)|| isFileTemplate(template) || template.equals(TEMPLATE_MEDIA_FOLDER);
    }
}
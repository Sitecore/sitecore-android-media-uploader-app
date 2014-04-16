package net.sitecore.android.mediauploader.model;

import java.util.Comparator;

import net.sitecore.android.sdk.api.model.ScItem;

import static net.sitecore.android.mediauploader.util.ScUtils.TEMPLATE_MEDIA_FOLDER;

public class SortByMediaFolderTemplateComparator implements Comparator<ScItem> {

    @Override public int compare(ScItem lhs, ScItem rhs) {
        final String template1 = lhs.getTemplate();
        final String template2 = rhs.getTemplate();
        if (template1.equals(template2)) return lhs.getDisplayName().compareToIgnoreCase(rhs.getDisplayName());

        if (template1.equals(TEMPLATE_MEDIA_FOLDER)) return -1;
        if (template2.equals(TEMPLATE_MEDIA_FOLDER)) return 1;

        return lhs.getDisplayName().compareToIgnoreCase(rhs.getDisplayName());
    }
}
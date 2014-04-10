package net.sitecore.android.mediauploader.model;

import java.util.Comparator;

import net.sitecore.android.sdk.api.model.ScItem;

public class SortByMediaFolderTemplateComparator implements Comparator<ScItem> {

    public static final String MEDIA_FOLDER_TEMPLATE = "System/Media/Media folder";

    @Override public int compare(ScItem lhs, ScItem rhs) {
        final String folder = MEDIA_FOLDER_TEMPLATE;

        final String template1 = lhs.getTemplate();
        final String template2 = rhs.getTemplate();
        if (template1.equals(template2)) return lhs.getDisplayName().compareTo(rhs.getDisplayName());

        if (template1.equals(folder)) return -1;
        if (template2.equals(folder)) return 1;

        return lhs.getDisplayName().compareTo(rhs.getDisplayName());
    }
}
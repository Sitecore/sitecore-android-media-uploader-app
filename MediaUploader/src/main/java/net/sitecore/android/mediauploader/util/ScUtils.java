package net.sitecore.android.mediauploader.util;

import net.sitecore.android.mediauploader.ui.MainActivity;

public class ScUtils {

    public static boolean isImage(String template) {
        if (template.equals("System/Media/Unversioned/Jpeg")) return true;
        else return template.equals("System/Media/Unversioned/Image");
    }

    public static String getMediaDownloadUrl(String itemId) {
        String id = itemId.replace("{", "").replace("}", "").replace("-", "");
        //TODO: fix session
        return String.format("%s/~/media/%s.ashx?w=50", MainActivity.mSession.getBaseUrl(), id);
    }
}

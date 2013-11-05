package net.sitecore.android.mediauploader.util;

import net.sitecore.android.mediauploader.ui.MainActivity;

public class ScUtils {

    public static boolean isImage(String template) {
        if (template.equals("System/Media/Unversioned/Jpeg")) return true;
        else return template.equals("System/Media/Unversioned/Image");
    }

    public static String getMediaDownloadUrl(String itemId) {
        return makeDownloadUrl(itemId).toString();
    }

    private static StringBuilder makeDownloadUrl(String itemId) {
        String id = itemId.replace("{", "").replace("}", "").replace("-", "");
        //TODO: fix session
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s/~/media/%s.ashx", MainActivity.mSession.getBaseUrl(), id));
        return builder;
    }

    public static String getMediaDownloadUrl(String itemId, int width, int height) {
        StringBuilder builder = makeDownloadUrl(itemId);
        builder.append("?w=").append(width);
        builder.append("&h=").append(height);
        return builder.toString();
    }
}

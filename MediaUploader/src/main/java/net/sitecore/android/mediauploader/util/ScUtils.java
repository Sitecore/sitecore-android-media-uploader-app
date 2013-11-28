package net.sitecore.android.mediauploader.util;

import android.content.Context;

import net.sitecore.android.mediauploader.R;

public class ScUtils {

    public static final String PATH_MEDIA_LIBRARY = "/sitecore/media library";

    public static final String TEMPLATE_JPEG = "System/Media/Unversioned/Jpeg";
    public static final String TEMPLATE_IMAGE = "System/Media/Unversioned/Image";

    public static boolean isImageTemplate(String template) {
        if (template.equals(TEMPLATE_JPEG)) return true;
        else return template.equals(TEMPLATE_IMAGE);
    }

    public static String getMediaDownloadUrl(Context context, String itemId, MediaParamsBuilder params) {
        return makeDownloadUrl(context, itemId).append(params.toString()).toString();
    }

    private static StringBuilder makeDownloadUrl(Context context, String itemId) {
        String id = itemId.replace("{", "").replace("}", "").replace("-", "");
        final String url = Prefs.from(context).getString(R.string.key_instance_url);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s/~/media/%s.ashx", url, id));
        return builder;
    }

}

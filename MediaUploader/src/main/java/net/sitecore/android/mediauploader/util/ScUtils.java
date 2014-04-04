package net.sitecore.android.mediauploader.util;

import android.content.Context;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.DownloadMediaOptions;

public class ScUtils {

    public static final String PATH_MEDIA_LIBRARY = "/sitecore/media library";

    public static final String TEMPLATE_JPEG = "System/Media/Unversioned/Jpeg";
    public static final String TEMPLATE_IMAGE = "System/Media/Unversioned/Image";

    public static boolean isImageTemplate(String template) {
        if (template.equals(TEMPLATE_JPEG)) return true;
        else return template.equals(TEMPLATE_IMAGE);
    }
}

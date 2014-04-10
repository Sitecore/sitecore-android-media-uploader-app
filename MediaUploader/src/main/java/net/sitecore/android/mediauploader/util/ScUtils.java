package net.sitecore.android.mediauploader.util;

public class ScUtils {

    public static final String PATH_MEDIA_LIBRARY = "/sitecore/media library";

    public static final String TEMPLATE_UNVERSIONED_JPEG = "System/Media/Unversioned/Jpeg";
    public static final String TEMPLATE_VERSIONED_JPEG = "System/Media/Versioned/Jpeg";
    public static final String TEMPLATE_UNVERSIONED_IMAGE = "System/Media/Unversioned/Image";

    public static final String TEMPLATE_MEDIA_FOLDER = "System/Media/Media folder";

    public static boolean isImageTemplate(String template) {
        if (template.equals(TEMPLATE_UNVERSIONED_JPEG)) return true;
        if (template.equals(TEMPLATE_VERSIONED_JPEG)) return true;
        if (template.equals(TEMPLATE_UNVERSIONED_IMAGE)) return true;

        return false;
    }
}

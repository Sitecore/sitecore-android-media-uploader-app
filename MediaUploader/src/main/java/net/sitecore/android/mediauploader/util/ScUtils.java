package net.sitecore.android.mediauploader.util;

import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import net.sitecore.android.sdk.api.model.ScError;

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

    public static String getMessageFromError(VolleyError error) {
        String message;
        if (error instanceof ScError) {
            ScError e = (ScError) error;
            message = String.format("Operation failed: (%s) %s", e.getStatusCode(), e.getMessage());
        } else if (error instanceof ServerError) {
            ServerError serverError = (ServerError) error;
            if (serverError.networkResponse != null) {
                message = "Server error " + serverError.networkResponse.statusCode;
            } else {
                message = "Server error " + serverError.getMessage();
            }
        } else if (error instanceof NoConnectionError) {
            message = "No connection available, please check your internet settings";
        } else {
            message = "Unknown error : " + error.toString();
        }
        return message;
    }
}

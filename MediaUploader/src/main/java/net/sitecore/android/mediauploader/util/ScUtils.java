package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.UnknownHostException;

import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.model.ScError;

public class ScUtils {

    public static final String PATH_MEDIA_LIBRARY = "/sitecore/media library";

    public static final String TEMPLATE_UNVERSIONED_JPEG = "System/Media/Unversioned/Jpeg";
    public static final String TEMPLATE_VERSIONED_JPEG = "System/Media/Versioned/Jpeg";
    public static final String TEMPLATE_UNVERSIONED_IMAGE = "System/Media/Unversioned/Image";

    public static final String TEMPLATE_UNVERSIONED_FILE = "System/Media/Unversioned/File";

    public static final String TEMPLATE_MEDIA_FOLDER = "System/Media/Media folder";

    public static boolean isImageTemplate(@NonNull String template) {
        if (template.equals(TEMPLATE_UNVERSIONED_JPEG)) return true;
        if (template.equals(TEMPLATE_VERSIONED_JPEG)) return true;
        if (template.equals(TEMPLATE_UNVERSIONED_IMAGE)) return true;

        return false;
    }

    public static boolean isFileTemplate(String template) {
        return TEMPLATE_UNVERSIONED_FILE.equals(template);
    }

    public static String getMessageFromError(Context context, VolleyError error) {
        String message;
        if (error instanceof ScError) {
            message = error.getMessage();
        } else if (error instanceof ServerError) {
            message = context.getString(R.string.error_server_connection);
        } else if (error instanceof NoConnectionError || error.getCause() instanceof UnknownHostException) {
            message = context.getString(R.string.error_connection_failed);
        } else {
            message = context.getString(R.string.error);
        }
        return message;
    }
}
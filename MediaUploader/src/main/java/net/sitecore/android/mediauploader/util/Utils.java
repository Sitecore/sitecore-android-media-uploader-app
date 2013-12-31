package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.model.ScError;

public class Utils {
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

    public static void showToast(Context context, int stringResourceId) {
        Toast.makeText(context, stringResourceId, Toast.LENGTH_LONG).show();
    }


}

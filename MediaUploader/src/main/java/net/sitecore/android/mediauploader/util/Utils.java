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

    public static String getDefaultInstanceName(Context context) {
        return Prefs.from(context).getString(R.string.key_instance_name);
    }

    public static String getDefaultInstanceFolder(Context context) {
        return Prefs.from(context).getString(R.string.key_instance_root_folder);
    }

    public static void showToast(Context context, int stringResourceId) {
        Toast.makeText(context, stringResourceId, Toast.LENGTH_LONG).show();
    }

    public static boolean isDefaultInstance(Context context, String instanceName) {
        return instanceName.equals(Prefs.from(context).getString(R.string.key_instance_name));
    }

    public static void setDefaultInstance(Context context, String name, String url, String login, String password, String folder) {
        Prefs prefs = Prefs.from(context);

        prefs.put(R.string.key_instance_name, name);
        prefs.put(R.string.key_instance_url, url);
        prefs.put(R.string.key_instance_login, login);
        prefs.put(R.string.key_instance_password, password);
        prefs.put(R.string.key_instance_root_folder, folder);
    }
}

package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.model.ScError;

import static net.sitecore.android.sdk.api.LogUtils.LOGE;

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
        prefs.put(R.string.key_instance_exist, true);
    }

    public static void setDefaultInstance(Context context, Instance instance) {
        Prefs prefs = Prefs.from(context);
        prefs.put(R.string.key_instance_name, instance.name);
        prefs.put(R.string.key_instance_url, instance.url);
        prefs.put(R.string.key_instance_login, instance.login);
        prefs.put(R.string.key_instance_password, instance.password);
        prefs.put(R.string.key_instance_root_folder, instance.rootFolder);
    }

    public static ScPublicKey getPublicKey(Context context) {
        String keyValue = Prefs.from(context).getString(R.string.key_public_key_value);
        try {
            return new ScPublicKey(keyValue);
        } catch (InvalidKeySpecException e) {
            LOGE(e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            LOGE(e);
            return null;
        }
    }

    public static void saveKeyToPrefs(Context context, ScPublicKey key) {
        String keyValue = key.getRawValue();
        Prefs.from(context).put(R.string.key_public_key_value, keyValue);
    }

    public static Instance getCurrentInstance(Context context) {
        Prefs prefs = Prefs.from(context);
        String name = prefs.getString(R.string.key_instance_name);
        String url = prefs.getString(R.string.key_instance_url);
        String login = prefs.getString(R.string.key_instance_login);
        String password = prefs.getString(R.string.key_instance_password);
        String rootFolder = prefs.getString(R.string.key_instance_root_folder);
        return new Instance(name, url, login, password, rootFolder);
    }
}

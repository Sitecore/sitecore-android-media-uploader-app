package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.widget.Toast;

public class Utils {

    public static void showToast(Context context, int stringResourceId) {
        Toast.makeText(context, stringResourceId, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}

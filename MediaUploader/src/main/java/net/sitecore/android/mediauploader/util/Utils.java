package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.model.ScError;

public class Utils {

    public static void showToast(Context context, int stringResourceId) {
        Toast.makeText(context, stringResourceId, Toast.LENGTH_LONG).show();
    }


}

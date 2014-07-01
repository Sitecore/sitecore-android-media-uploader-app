package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class Utils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_hh_mm");

    public static void showToast(Context context, int stringResourceId) {
        Toast.makeText(context, stringResourceId, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public static LatLng getLatLngFromImage(Uri uri) {
        LatLng latLng = null;
        try {
            ExifInterface exif = new ExifInterface(uri.toString());
            String latitudeString = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitudeString = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (latitudeString != null && longitudeString != null) {
                double latitude = Double.parseDouble(latitudeString);
                double longitude = Double.parseDouble(longitudeString);
                latLng = new LatLng(latitude, longitude);
            }
        } catch (IOException | NumberFormatException e) {
            LOGE(e);
        }
        return latLng;
    }

}


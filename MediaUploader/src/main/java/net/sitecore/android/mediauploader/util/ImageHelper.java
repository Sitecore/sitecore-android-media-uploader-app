package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.ui.settings.ImageSize;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class ImageHelper {
    private static final String IMAGE_FILE_NAME_TEMPLATE = "temp_image_";

    private Context mContext;

    public ImageHelper(Context context) {
        mContext = context;
    }

    public boolean isResizeNeeded(String imageUri, ImageSize imageSize) {
        return isResizeNeeded(imageUri, imageSize.getWidth(), imageSize.getHeight());
    }

    public boolean isResizeNeeded(String imageUri, int desiredWidth, int desiredHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(getInputStreamFromUri(imageUri), null, options);

            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            return imageWidth > desiredWidth || imageHeight > desiredHeight;
        } catch (IOException e) {
            LOGE(e);
            return false;
        }
    }

    public String resize(String imageUri, ImageSize imageSize) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(getInputStreamFromUri(imageUri), null, options);
        options.inSampleSize = calculateSampleSize(options, imageSize.getWidth(), imageSize.getHeight());

        options.inJustDecodeBounds = false;
        File targetFile = getTemporaryImageFile();

        Bitmap bitmap = BitmapFactory.decodeStream(getInputStreamFromUri(imageUri), null, options);
        bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(targetFile, false));
        bitmap.recycle();

        return targetFile.getAbsolutePath();
    }

    private File getTemporaryImageFile() throws IOException {
        String name = IMAGE_FILE_NAME_TEMPLATE + new SimpleDateFormat("yyyyMMddhhmmss")
                .format(new Date(System.currentTimeMillis())) + ".jpeg";

        File file = new File(mContext.getCacheDir().getAbsolutePath(), name);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        boolean isWidthBigger = Math.max(width, height) == width;

        if (!isWidthBigger) {
            int temp = reqWidth;
            reqWidth = reqHeight;
            reqHeight = temp;
        }

        if (height > reqHeight || width > reqWidth) {
            if (isWidthBigger) {
                while ((width / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            } else {
                while ((height / inSampleSize) > reqHeight) {
                    inSampleSize *= 2;
                }
            }
        }

        return inSampleSize;
    }

    public InputStream getInputStreamFromUri(String path) throws IOException {
        if (path.startsWith("content:")) {
            Uri uri = Uri.parse(path);
            return mContext.getContentResolver().openInputStream(uri);
        }
        if (path.startsWith("http:") || path.startsWith("https:") || path.startsWith("file:")) {
            URL url = new URL(path);
            return url.openStream();
        } else {
            return new FileInputStream(path);
        }
    }

    public static LatLng getLatLngFromImage(String uri) {
        LatLng latLng = null;
        try {
            ExifInterface exif = new ExifInterface(uri);
            String latitudeString = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longtitudeString = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (latitudeString != null && longtitudeString != null) {
                double latitude = Double.parseDouble(latitudeString);
                double longtitude = Double.parseDouble(longtitudeString);
                latLng = new LatLng(latitude, longtitude);
            }
        } catch (IOException | NumberFormatException e) {
            LOGE(e);
        }
        return latLng;
    }
}

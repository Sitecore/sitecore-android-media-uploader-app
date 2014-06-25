package net.sitecore.android.mediauploader.service;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.IOException;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;
import net.sitecore.android.mediauploader.model.ImageResizer;
import net.sitecore.android.mediauploader.util.NotificationUtils;
import net.sitecore.android.sdk.api.UploadMediaRequestOptions;
import net.sitecore.android.sdk.api.UploadMediaService;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class MediaUploaderService extends UploadMediaService {

    public static final String EXTRA_UPLOAD_OPTIONS = "net.sitecore.android.sdk.api.EXTRA_UPLOAD_OPTIONS";
    public static final String EXTRA_UPLOAD_URI = "upload_uri";
    public static final String EXTRA_MEDIA_FOLDER = "media_folder";

    @Override protected void onHandleIntent(Intent intent) {
        LOGD("MediaUploaderService.onHandleIntent");

        UploadMediaRequestOptions options = intent.getParcelableExtra(EXTRA_UPLOAD_OPTIONS);
        Uri uploadItemUri = intent.getParcelableExtra(EXTRA_UPLOAD_URI);
        String mediaFolder = intent.getStringExtra(EXTRA_MEDIA_FOLDER);

        LOGD("Original media options: " + options);
        String mediaFilePath = resizeIfNeeded(uploadItemUri, options.getMediaFilePath());

        options.setMediaFilePath(mediaFilePath);

        String itemName = options.getItemName();

        String fileExtension = getFileExtension(mediaFilePath);
        if (fileExtension != null) {
            if (itemName.contains(".")) {
                options.setFileName(itemName);
            } else {
                options.setFileName(itemName + "." + fileExtension);
            }
        }

        changeUploadStatus(uploadItemUri);
        Notification n = NotificationUtils.showInProgressNotification(getApplicationContext(), itemName, mediaFolder);
        startForeground(itemName.hashCode(), n);

        LOGD("Updated media options: " + options);
        super.onHandleIntent(intent);
    }

    private void changeUploadStatus(Uri uploadUri) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        getContentResolver().update(uploadUri, values, null, null);
    }

    private String getFileExtension(String filePath) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(getContentResolver().getType(Uri.parse(filePath)));

        if (extension == null) {
            extension = filePath.substring(filePath.lastIndexOf(".") + 1);
        }
        return extension;
    }

    private String resizeIfNeeded(Uri uploadItemUri, String mediaFilePath) {
        String newMediaFilePath = mediaFilePath;

        ImageSize imageSize = ImageSize.ACTUAL;
        Cursor cursor = getContentResolver().query(uploadItemUri, Query.PROJECTION, null, null, null);
        if (cursor.moveToFirst()) {
            imageSize = ImageSize.valueOf(cursor.getString(Query.IMAGE_SIZE));
        }
        cursor.close();

        if (imageSize == ImageSize.ACTUAL) return newMediaFilePath;

        ImageResizer imageResizer = new ImageResizer(this);
        boolean isResizeNeeded = imageResizer.isResizeNeeded(mediaFilePath, imageSize);
        if (isResizeNeeded) {
            try {
                newMediaFilePath = imageResizer.resize(mediaFilePath, imageSize);
            } catch (IOException e) {
                LOGE(e);
            }
        }
        return newMediaFilePath;
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}
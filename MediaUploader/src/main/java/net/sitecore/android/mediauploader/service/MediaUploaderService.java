package net.sitecore.android.mediauploader.service;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
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

    public static final String EXTRA_UPLOAD_URI = "upload_uri";
    public static final String EXTRA_MEDIA_FOLDER = "media_folder";
    public static final String EXTRA_IS_IMAGE_SELECTED = "is_image_selected";

    @Override protected void onMediaUploadStarted(Intent intent, UploadMediaRequestOptions options) {
        LOGD("MediaUploaderService.onMediaUploadStarted");
        final Uri uploadItemUri = intent.getParcelableExtra(EXTRA_UPLOAD_URI);
        final String mediaFolder = intent.getStringExtra(EXTRA_MEDIA_FOLDER);
        final String itemName = options.getItemName();

        LOGD("Original media options: " + options);

        // resize media
        String mediaFilePath = resizeIfNeeded(uploadItemUri, options.getMediaFilePath());
        options.setMediaFilePath(mediaFilePath);

        // fix extension
        if (itemName.contains(".")) {
            options.setFileName(itemName);
        } else {
            final String mimeType = getContentResolver().getType(Uri.parse(mediaFilePath));
            final String mimeExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!TextUtils.isEmpty(mimeExt)) {
                options.setFileName(itemName + "." + mimeExt);
            } else if (mediaFilePath.contains(".")) {
                final String fileExt = mediaFilePath.substring(mediaFilePath.lastIndexOf(".") + 1);
                options.setFileName(itemName + "." + fileExt);
            } else {
                final boolean isImage = intent.getBooleanExtra(EXTRA_IS_IMAGE_SELECTED, true);
                final String defaultExt = isImage ? "png" : "mp4";
                options.setFileName(itemName + "." + defaultExt);
            }
        }

        setUploadInProgressStatus(uploadItemUri);

        // Show notification
        final Notification n = NotificationUtils.showInProgressNotification(getApplicationContext(), itemName, mediaFolder);
        startForeground(NotificationUtils.NOTIFICATION_IN_PROGRESS, n);

        LOGD("Updated media options: " + options);
    }

    private void setUploadInProgressStatus(Uri uploadUri) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        getContentResolver().update(uploadUri, values, null, null);
    }

    private String resizeIfNeeded(Uri uploadItemUri, String mediaFilePath) {
        String newMediaFilePath = mediaFilePath;

        ImageSize imageSize = getImageSize(uploadItemUri);
        if (imageSize == ImageSize.ACTUAL) {
            return newMediaFilePath;
        }

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

    private ImageSize getImageSize(Uri uploadItemUri) {
        ImageSize imageSize = ImageSize.ACTUAL;

        Cursor cursor = getContentResolver().query(uploadItemUri, Query.PROJECTION, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                imageSize = ImageSize.valueOf(cursor.getString(Query.IMAGE_SIZE));
            }
        } finally {
            cursor.close();
        }
        return imageSize;
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}
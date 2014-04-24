package net.sitecore.android.mediauploader.service;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.io.IOException;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;
import net.sitecore.android.mediauploader.util.ImageHelper;
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

        String mediaFilePath = resizeIfNeeded(uploadItemUri, options.getMediaFilePath());
        options.setMediaFilePath(mediaFilePath);

        changeUploadStatus(uploadItemUri);
        NotificationUtils.showInProgressNotification(getApplicationContext(), options.getItemName(), mediaFolder);

        super.onHandleIntent(intent);
    }

    private void changeUploadStatus(Uri uploadUri) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        getContentResolver().update(uploadUri, values, null, null);
    }

    private String resizeIfNeeded(Uri uploadItemUri, String mediaFilePath) {
        String newMediaFilePath = mediaFilePath;

        Cursor cursor = getContentResolver().query(uploadItemUri, Query.PROJECTION, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ImageSize imageSize = ImageSize.valueOf(cursor.getString(Query.IMAGE_SIZE));

            if (imageSize == ImageSize.ACTUAL) return newMediaFilePath;

            ImageHelper imageHelper = new ImageHelper(this);
            boolean isResizeNeeded = imageHelper.isResizeNeeded(mediaFilePath, imageSize.getWidth(),
                    imageSize.getHeight());
            if (isResizeNeeded) {
                try {
                    newMediaFilePath = imageHelper.resize(mediaFilePath, imageSize.getWidth(), imageSize.getHeight());
                } catch (IOException e) {
                    LOGE(e);
                }
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
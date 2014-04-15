package net.sitecore.android.mediauploader.service;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.util.NotificationUtils;
import net.sitecore.android.sdk.api.UploadMediaRequestOptions;
import net.sitecore.android.sdk.api.UploadMediaService;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class MediaUploaderService extends UploadMediaService {

    public static final String EXTRA_UPLOAD_OPTIONS = "net.sitecore.android.sdk.api.EXTRA_UPLOAD_OPTIONS";
    public static final String EXTRA_UPLOAD_URI = "upload_uri";
    public static final String EXTRA_MEDIA_FOLDER = "media_folder";

    @Override protected void onHandleIntent(Intent intent) {
        LOGD("MediaUploaderService.onHandleIntent");

        UploadMediaRequestOptions options = intent.getParcelableExtra(EXTRA_UPLOAD_OPTIONS);
        Uri uploadItemUri = intent.getParcelableExtra(EXTRA_UPLOAD_URI);
        String mediaFolder = intent.getStringExtra(EXTRA_MEDIA_FOLDER);

        changeUploadStatus(uploadItemUri);
        NotificationUtils.showInProgressNotification(getApplicationContext(), options.getItemName(), mediaFolder);

        super.onHandleIntent(intent);
    }

    private void changeUploadStatus(Uri uploadUri) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        getContentResolver().update(uploadUri, values, null, null);
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

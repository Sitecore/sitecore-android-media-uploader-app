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

    @Override protected void onHandleIntent(Intent intent) {
        LOGD("MediaUploaderService.onHandleIntent");

        UploadMediaRequestOptions options = intent.getParcelableExtra(EXTRA_UPLOAD_OPTIONS);
        Uri uploadItemUri = intent.getParcelableExtra(EXTRA_UPLOAD_URI);

        onUploadStarted(uploadItemUri, options.getItemName());

        super.onHandleIntent(intent);
    }

    public void onUploadStarted(Uri uploadUri, String name) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        getContentResolver().update(uploadUri, values, null, null);
        NotificationUtils.showNotification(getApplicationContext(), UploadStatus.IN_PROGRESS.name(), name, name.hashCode());
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

package net.sitecore.android.mediauploader.service;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
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

        String folder = "";
        Instance instance = getInstance(uploadUri);
        if (instance != null) {
            folder = instance.getRootFolder();
        }

        getContentResolver().update(uploadUri, values, null, null);
        NotificationUtils.showInProgressNotification(getApplicationContext(), name, folder);
    }

    private Instance getInstance(Uri uploadUri) {
        Instance instance = null;

        Cursor c = getContentResolver().query(uploadUri, Uploads.Query.PROJECTION, null, null, null);
        String instanceId = "";
        if (c != null && c.moveToFirst()) {
            instanceId = c.getString(Uploads.Query.INSTANCE_ID);
        }

        Cursor cursor = getContentResolver().query(Instances.buildInstanceUri(instanceId),
                Query.PROJECTION, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            instance = new Instance(cursor);
        }
        return instance;
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

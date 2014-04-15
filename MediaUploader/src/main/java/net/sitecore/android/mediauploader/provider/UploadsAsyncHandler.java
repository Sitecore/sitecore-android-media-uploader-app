package net.sitecore.android.mediauploader.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class UploadsAsyncHandler extends AsyncQueryHandler {

    public UploadsAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void updateUploadStatus(String uploadId, UploadStatus status) {
        ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, status.name());

        startUpdate(0, null, Uploads.buildUploadUri(uploadId), values, null, null);
    }
}

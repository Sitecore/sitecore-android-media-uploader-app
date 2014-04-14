package net.sitecore.android.mediauploader.util;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class InstancesAsyncHandler extends AsyncQueryHandler {

    public InstancesAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void insertPendingUpload(final String itemName, final Uri fileUri, final Instance instance) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, fileUri.toString());
        values.put(Uploads.STATUS, UploadStatus.PENDING.name());
        values.put(Uploads.INSTANCE_ID, instance.getId());

        startInsert(0, null, Uploads.CONTENT_URI, values);
    }
}

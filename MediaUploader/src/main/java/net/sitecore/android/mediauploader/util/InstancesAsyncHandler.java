package net.sitecore.android.mediauploader.util;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class InstancesAsyncHandler extends AsyncQueryHandler {

    public static final int TOKEN_INSERT_PENDING = 0;

    public InstancesAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void insertDelayedUpload(final String itemName, final Uri fileUri, final Instance instance) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, fileUri.toString());
        values.put(Uploads.STATUS, UploadStatus.UPLOAD_LATER.name());
        values.put(Uploads.INSTANCE_ID, instance.getId());

        startInsert(TOKEN_INSERT_PENDING, null, Uploads.CONTENT_URI, values);
    }

}

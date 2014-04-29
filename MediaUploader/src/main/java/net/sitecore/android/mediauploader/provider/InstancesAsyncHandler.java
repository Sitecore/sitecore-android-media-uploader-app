package net.sitecore.android.mediauploader.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;

public class InstancesAsyncHandler extends AsyncQueryHandler {

    public static final int TOKEN_INSERT_PENDING = 0;
    public static final int TOKEN_UPDATE_SELECTED = 1;

    public InstancesAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void queryInstances() {
        startQuery(0, null, Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    public void insertDelayedUpload(final String itemName, final Uri fileUri, final Instance instance,
            ImageSize imageSize, Address imageAddress) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, fileUri.toString());
        values.put(Uploads.STATUS, UploadStatus.UPLOAD_LATER.name());
        values.put(Uploads.IMAGE_SIZE, imageSize.name());
        values.put(Uploads.INSTANCE_ID, instance.getId());

        if (imageAddress != null) {
            values.put(Uploads.ADDRESS_NAME, imageAddress.address);
            values.put(Uploads.COUNTRY_CODE, imageAddress.countryCode);
            values.put(Uploads.ZIP_CODE, imageAddress.zipCode);
            values.put(Uploads.LATITUDE, imageAddress.latLng.latitude);
            values.put(Uploads.LONGITUDE, imageAddress.latLng.longitude);
        }

        startInsert(TOKEN_INSERT_PENDING, null, Uploads.CONTENT_URI, values);
    }

    public void updateInstanceSelected(String instanceId) {
        ContentValues values = new ContentValues();
        values.put(Instances.SELECTED, 1);

        startUpdate(TOKEN_UPDATE_SELECTED, null, Instances.buildInstanceUri(instanceId), values, null, null);
    }

    public void deleteInstance(Uri instanceUri) {
        startDelete(0, null, instanceUri, null, null);
    }

}

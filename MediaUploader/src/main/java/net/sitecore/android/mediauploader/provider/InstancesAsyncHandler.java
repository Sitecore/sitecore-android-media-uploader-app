package net.sitecore.android.mediauploader.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;

public class InstancesAsyncHandler extends AsyncQueryHandler {

    private static final int TOKEN_INSERT_PENDING = 0;
    private static final int TOKEN_UPDATE_SELECTED = 1;

    private static final int TOKEN_QUERY_ALL = 0;
    private static final int TOKEN_QUERY_DUPLICATES = 0;

    private static final String SELECTION_DUPLICATE = Instances.URL + "=? and "
            + Instances.LOGIN + "=? and "
            + Instances.PASSWORD + "=? and "
            + Instances.ROOT_FOLDER + "=? and "
            + Instances.SITE + "=?";

    public InstancesAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void queryInstances() {
        startQuery(TOKEN_QUERY_ALL, null, Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
    }

    public void queryDuplicateInstances(Instance instance) {
        final String[] selectionArgs = new String[]{
                instance.getUrl(),
                instance.getLogin(),
                instance.getPassword(),
                instance.getRootFolder(),
                instance.getSite()
        };

        startQuery(TOKEN_QUERY_DUPLICATES, null, Instances.CONTENT_URI, Query.PROJECTION, SELECTION_DUPLICATE, selectionArgs, null);
    }

    @Override protected final void onQueryComplete(int token, Object cookie, Cursor cursor) {
        try {
            onInstancesLoaded(cursor);
        } finally {
            cursor.close();
        }
    }

    public void onInstancesLoaded(Cursor cursor) {
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

    public void insertNewInstance(Instance instance) {
        startInsert(0, null, Instances.CONTENT_URI, instance.toContentValues());
    }

    public void updateInstance(Uri instanceUri, Instance instance) {
        startUpdate(0, null, instanceUri, instance.toContentValues(), null, null);
    }

}

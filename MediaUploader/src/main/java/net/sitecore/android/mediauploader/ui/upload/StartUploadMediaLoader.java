package net.sitecore.android.mediauploader.ui.upload;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract;
import net.sitecore.android.mediauploader.provider.UploaderMediaDatabase;
import net.sitecore.android.mediauploader.service.UploadHelper;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.provider.ScItemsDatabase;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import static net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.UploadWithInstanceQuery;
import static net.sitecore.android.mediauploader.provider.UploaderMediaDatabase.Tables;

public class StartUploadMediaLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context mContext;
    private final String mUploadId;

    public StartUploadMediaLoader(Context context, String uploadId) {
        mContext = context;
        mUploadId = uploadId;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String selection = Tables.UPLOADS + "." + Uploads._ID + "='" + mUploadId + "'";

        return new CursorLoader(mContext, Uploads.UPLOADS_JOIN_INSTANCE_URI,
                UploadWithInstanceQuery.PROJECTION, selection, null, Uploads.Query.ORDER_BY_TIME_ADDED);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            if (!data.moveToFirst()) return;

            final Uri uploadUri = Uploads.buildUploadUri(mUploadId);
            final String instanceId = data.getString(UploadWithInstanceQuery.INSTANCE_ID);

            if (!TextUtils.isEmpty(instanceId)) {
                final String itemName = data.getString(UploadWithInstanceQuery.ITEM_NAME);
                final Uri fileUri = Uri.parse(data.getString(UploadWithInstanceQuery.FILE_URI));
                final Address imageAddress = getAddressFromCursor(data);
                final Instance instance = Instance.fromUploadsJoinInstanceCursor(data);

                final ScPublicKey key = new ScPublicKey(instance.getPublicKey());
                final ScApiSession session = ScApiSessionFactory.newSession(instance.getUrl(), key,
                        instance.getLogin(), instance.getPassword());

                final UploadHelper helper = new UploadHelper(mContext);
                if (!TextUtils.isEmpty(instance.getSite())) session.setDefaultSite(instance.getSite());

                helper.startUploadService(session, uploadUri, instance, itemName,
                        fileUri.toString(), imageAddress);
            } else {
                // instance deleted -> set error
                ContentValues values = new ContentValues();
                values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                values.put(Uploads.FAIL_MESSAGE, "Site has been deleted");
                mContext.getContentResolver().update(uploadUri, values, null, null);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //TODO: handle bad public key
        } finally {
            data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private Address getAddressFromCursor(Cursor cursor) {
        String addressName = cursor.getString(UploadWithInstanceQuery.ADDRESS_NAME);
        if (TextUtils.isEmpty(addressName)) return null;

        String countryCode = cursor.getString(UploadWithInstanceQuery.COUNTRY_CODE);
        String zipCode = cursor.getString(UploadWithInstanceQuery.ZIP_CODE);
        double latitude = cursor.getDouble(UploadWithInstanceQuery.LATITUDE);
        double longitude = cursor.getDouble(UploadWithInstanceQuery.LONGITUDE);
        return new Address(addressName, countryCode, zipCode, new LatLng(latitude, longitude));
    }

}

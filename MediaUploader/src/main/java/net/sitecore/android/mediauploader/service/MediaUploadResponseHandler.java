package net.sitecore.android.mediauploader.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.util.NotificationUtils;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.ScItem;

public class MediaUploadResponseHandler implements Listener<ItemsResponse>, ErrorListener {

    private final Context mContext;
    private final Uri mUploadUri;
    private final String mFolder;
    private final String mItemName;
    private final ScApiSession mSession;
    private final Address mAddress;

    @Inject ScRequestQueue mScRequestQueue;

    public MediaUploadResponseHandler(Context context, ScApiSession session, Uri uploadUri, String folder,
            String itemName, Address address) {
        mContext = context;
        mUploadUri = uploadUri;
        mFolder = folder;
        mItemName = itemName;
        mSession = session;
        mAddress = address;
        UploaderApp.from(context).inject(this);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        if (itemsResponse.getResultCount() == 1) {
            if (mAddress != null) {
                performItemUpdate(itemsResponse.getItems().get(0));
            } else {
                onUploadFinished();
            }
        } else {
            onUploadFailed(mContext.getString(R.string.error_upload_failed));
        }
    }

    private void performItemUpdate(ScItem item) {
        Map<String, String> fields = new HashMap<>(5);

        if (!TextUtils.isEmpty(mAddress.address)) fields.put("LocationDescription", mAddress.address);
        if (!TextUtils.isEmpty(mAddress.countryCode)) fields.put("CountryCode", mAddress.countryCode);
        if (!TextUtils.isEmpty(mAddress.zipCode)) fields.put("ZipCode", mAddress.zipCode);
        fields.put("Latitude", String.valueOf(mAddress.latLng.latitude));
        fields.put("Longitude", String.valueOf(mAddress.latLng.longitude));

        Listener<ItemsResponse> updateListener = itemsResponse -> onUploadFinished();
        ErrorListener errorListener = error -> onUploadFinished();

        mScRequestQueue.add(mSession.editItemFields(item, fields, updateListener, errorListener));
    }

    @Override public void onErrorResponse(VolleyError volleyError) {
        onUploadFailed(ScUtils.getMessageFromError(mContext, volleyError));
    }

    public void onUploadFinished() {
        UploaderApp.from(mContext).trackEvent(R.string.event_image_uploaded);

        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.DONE.name());
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showFinishedNotification(mContext, mItemName, mFolder);
    }

    public void onUploadFailed(String errorMessage) {
        UploaderApp.from(mContext).trackEvent(R.string.event_image_upload_failed, errorMessage);

        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.ERROR.name());
        values.put(Uploads.FAIL_MESSAGE, errorMessage);
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showErrorNotification(mContext, mItemName, errorMessage);
    }
}

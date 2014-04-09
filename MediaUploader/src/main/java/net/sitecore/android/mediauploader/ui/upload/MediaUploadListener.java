package net.sitecore.android.mediauploader.ui.upload;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.util.NotificationUtils;
import net.sitecore.android.sdk.api.model.ItemsResponse;

class MediaUploadListener implements Listener<ItemsResponse>, ErrorListener {
    private Context mContext;
    private Uri mUploadUri;
    private String mItemName;

    public MediaUploadListener(Context context, Uri uploadUri, String itemName) {
        mContext = context;
        mUploadUri = uploadUri;
        mItemName = itemName;
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        onUploadFinished();
    }

    @Override public void onErrorResponse(VolleyError volleyError) {
        onUploadFailed();
    }

    public void onUploadFinished() {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.DONE.name());
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showNotification(mContext, UploadStatus.DONE.name(), mItemName, mItemName.hashCode());
    }

    public void onUploadFailed() {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.ERROR.name());
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showNotification(mContext, UploadStatus.ERROR.name(), mItemName, mItemName.hashCode());
    }
}

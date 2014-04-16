package net.sitecore.android.mediauploader.ui.upload;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.util.NotificationUtils;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.model.ItemsResponse;

public class MediaUploadListener implements Listener<ItemsResponse>, ErrorListener {

    private Context mContext;
    private Uri mUploadUri;
    private String mFolder;
    private String mItemName;

    public MediaUploadListener(Context context, Uri uploadUri, String folder, String itemName) {
        mContext = context;
        mUploadUri = uploadUri;
        mFolder = folder;
        mItemName = itemName;
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        if (itemsResponse.getResultCount() == 1) {
            onUploadFinished();
        } else {
            onUploadFailed(mContext.getString(R.string.error_upload_failed));
        }
    }

    @Override public void onErrorResponse(VolleyError volleyError) {
        onUploadFailed(ScUtils.getMessageFromError(volleyError));
    }

    public void onUploadFinished() {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.DONE.name());
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showFinishedNotification(mContext, mItemName, mFolder);
    }

    public void onUploadFailed(String errorMessage) {
        final ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.ERROR.name());
        values.put(Uploads.FAIL_MESSAGE, errorMessage);
        //TODO: change to AsyncQueryHandler when sdk UploadMediaIntentBuilder will be fixed (wrong thread)
        mContext.getContentResolver().update(mUploadUri, values, null, null);
        NotificationUtils.showFialNotification(mContext, mItemName, errorMessage);
    }
}

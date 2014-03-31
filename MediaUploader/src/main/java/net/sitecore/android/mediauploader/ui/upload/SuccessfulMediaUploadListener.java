package net.sitecore.android.mediauploader.ui.upload;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.android.volley.Response.Listener;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.sdk.api.model.ItemsResponse;

class SuccessfulMediaUploadListener implements Listener<ItemsResponse> {

    private ContentResolver mContentResolver;
    private Uri mUploadUri;

    SuccessfulMediaUploadListener(ContentResolver contentResolver, Uri uploadUri) {
        mContentResolver = contentResolver;
        mUploadUri = uploadUri;
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        ContentValues values = new ContentValues();
        values.put(Uploads.STATUS, UploadStatus.DONE.name());
        new AsyncQueryHandler(mContentResolver) {
        }.startUpdate(0, null, mUploadUri, values, null, null);
    }
}

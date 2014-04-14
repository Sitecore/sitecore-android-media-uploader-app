package net.sitecore.android.mediauploader.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import javax.inject.Inject;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;
import net.sitecore.android.mediauploader.service.MediaUploaderService;
import net.sitecore.android.mediauploader.ui.upload.MediaUploadListener;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.UploadMediaIntentBuilder;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class UploadHelper {
    public Context mContext;
    public ContentResolver mContentResolver;

    @Inject ScApiSession mSession;

    public UploadHelper(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();

        UploaderApp.from(mContext).inject(this);
    }

    public void startUpload(String uploadID) {
        new StartUploadTask(uploadID).execute((Void) null);
    }

    public void uploadMedia(final Uri uploadUri, final Instance instance, final String name,
            final String fileUri) {
        uploadMedia(mSession, uploadUri, instance, name, fileUri);
    }

    public void uploadMedia(ScApiSession session, final Uri uploadUri, final Instance instance, final String name,
            final String fileUri) {
        session.setMediaLibraryPath("/");

        MediaUploadListener responseListener = new MediaUploadListener(mContext, uploadUri, name);

        UploadMediaIntentBuilder builder = session.uploadMediaIntent(instance.getRootFolder(), name, fileUri.toString())
                .setDatabase(instance.getDatabase())
                .setSuccessListener(responseListener)
                .setErrorListener(responseListener)
                .setUploadMediaServiceClass(MediaUploaderService.class);

        if (!TextUtils.isEmpty(instance.getSite())) builder.setSite(instance.getSite());

        Intent intent = builder.build(mContext);
        intent.putExtra(MediaUploaderService.EXTRA_UPLOAD_URI, uploadUri);

        mContext.startService(intent);
    }

    class StartUploadTask extends AsyncTask<Void, Void, Void> {
        private String uploadId;

        StartUploadTask(String uploadId) {
            this.uploadId = uploadId;
        }

        @Override protected Void doInBackground(Void... params) {
            try {
                Cursor cursor = mContentResolver.query(Uploads.buildUploadUri(uploadId), Query.PROJECTION, null, null,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    String itemName = cursor.getString(Query.ITEM_NAME);
                    Uri fileUri = Uri.parse(cursor.getString(Query.FILE_URI));
                    UploadStatus status = UploadStatus.valueOf(cursor.getString(Query.STATUS));

                    if (status == UploadStatus.IN_PROGRESS) {
                        LOGD("Upload with id " + uploadId + " already started");
                        return null;
                    }

                    String instanceId = cursor.getString(Query.INSTANCE_ID);
                    Cursor c = mContentResolver.query(Instances.buildInstanceUri(instanceId),
                            Instances.Query.PROJECTION, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        Instance instance = new Instance(c);
                        ScPublicKey key = new ScPublicKey(instance.getPublicKey());
                        ScApiSession session = ScApiSessionFactory.newSession(instance.getUrl(), key,
                                instance.getLogin(), instance.getPassword());

                        uploadMedia(session, Uploads.buildUploadUri(uploadId), instance, itemName, fileUri.toString());
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                        values.put(Uploads.FAIL_MESSAGE, "Instance has been deleted");
                        mContentResolver.update(Uploads.buildUploadUri(uploadId), values, null, null);
                    }
                } else {
                    LOGE("Upload with id " + uploadId + " has been deleted");
                    return null;
                }
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                LOGE(e);
            }
            return null;
        }
    }
}

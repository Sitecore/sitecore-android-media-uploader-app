package net.sitecore.android.mediauploader.util;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import javax.inject.Inject;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import net.sitecore.android.mediauploader.R;
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

import static net.sitecore.android.mediauploader.util.Utils.showToast;
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

    public void createUpload(final String itemName, final Uri fileUri) {
        new GetSelectedInstance(mContentResolver) {
            @Override public void onComplete(final Instance instance, int instanceID) {
                ContentValues values = build(itemName, fileUri);
                if (instance == null) {
                    //TODO: make upload failed, cause connected instance was deleted
                    values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                    values.put(Uploads.FAIL_MESSAGE, "Instance has been deleted");
                    return;
                } else {
                    values.put(Uploads.INSTANCE_ID, instanceID);
                    new AsyncQueryHandler(mContentResolver) {
                        @Override
                        protected void onInsertComplete(int token, Object cookie, final Uri uri) {
                            showToast(mContext, R.string.toast_added_to_uploads);
                        }
                    }.startInsert(0, null, Uploads.CONTENT_URI, values);
                }
            }
        }.start();
    }

    public void createAndStartUpload(final String itemName, final Uri fileUri) {
        new GetSelectedInstance(mContext.getContentResolver()) {
            @Override public void onComplete(final Instance instance, int instanceID) {
                ContentValues values = build(itemName, fileUri);
                if (instance == null) {
                    //TODO: make upload failed, cause connected instance was deleted
                    values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                    values.put(Uploads.FAIL_MESSAGE, "Instance has been deleted");
                    return;
                } else {
                    values.put(Uploads.INSTANCE_ID, instanceID);

                    new AsyncQueryHandler(mContext.getContentResolver()) {
                        @Override
                        protected void onInsertComplete(int token, Object cookie, final Uri uri) {
                            uploadMedia(mSession, uri, instance, itemName, fileUri.toString());
                        }
                    }.startInsert(0, null, Uploads.CONTENT_URI, values);
                    Toast.makeText(mContext, "Uploading media started.", Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }

    public void startUpload(String uploadID) {
        new StartUploadTask(uploadID).execute((Void) null);
    }

    private void uploadMedia(ScApiSession session, final Uri uploadUri, final Instance instance, final String name,
            final String fileUri) {
        session.setMediaLibraryPath("/");

        MediaUploadListener responseListener = new
                MediaUploadListener(mContext, uploadUri, name);

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

    private ContentValues build(String itemName, Uri fileUri) {
        ContentValues values = new ContentValues();
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, fileUri.toString());
        values.put(Uploads.STATUS, UploadStatus.PENDING.name());
        return values;
    }

    class StartUploadTask extends AsyncTask<Void, Void, Void> {
        private String uploadID;

        StartUploadTask(String uploadID) {
            this.uploadID = uploadID;
        }

        @Override protected Void doInBackground(Void... params) {
            try {
                Cursor cursor = mContentResolver.query(Uploads.buildUploadUri(uploadID), Query.PROJECTION, null, null,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    String itemName = cursor.getString(Query.ITEM_NAME);
                    Uri fileUri = Uri.parse(cursor.getString(Query.FILE_URI));
                    UploadStatus status = UploadStatus.valueOf(cursor.getString(Query.STATUS));

                    if (status == UploadStatus.IN_PROGRESS) {
                        LOGD("Upload with id " + uploadID + " already started");
                        return null;
                    }

                    String instanceID = cursor.getString(Query.INSTANCE_ID);
                    Cursor c = mContentResolver.query(Instances.buildInstanceUri(instanceID),
                            Instances.Query.PROJECTION, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        Instance instance = new Instance(c);
                        ScPublicKey key = new ScPublicKey(instance.getPublicKey());
                        ScApiSession session = ScApiSessionFactory.newSession(instance.getUrl(), key,
                                instance.getLogin(), instance.getPassword());

                        uploadMedia(session, Uploads.buildUploadUri(uploadID), instance, itemName, fileUri.toString());
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                        values.put(Uploads.FAIL_MESSAGE, "Instance has been deleted");
                        mContentResolver.update(Uploads.buildUploadUri(uploadID), values, null, null);
                    }
                } else {
                    LOGE("Upload with id " + uploadID + " has been deleted");
                    return null;
                }
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                LOGE(e);
            }
            return null;
        }
    }
}

package net.sitecore.android.mediauploader.ui.upload;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class StartUploadTask extends AsyncTask<String, Void, Void> {

    private Context mContext;

    public StartUploadTask(Context context) {
        mContext = context;
    }

    @Override protected Void doInBackground(String... params) {
        ContentResolver contentResolver = mContext.getContentResolver();
        String uploadId = params[0];
        try {
            // load upload fields
            Cursor cursor = contentResolver.query(Uploads.buildUploadUri(uploadId), Query.PROJECTION, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String itemName = cursor.getString(Query.ITEM_NAME);
                Uri fileUri = Uri.parse(cursor.getString(Query.FILE_URI));
                UploadStatus status = UploadStatus.valueOf(cursor.getString(Query.STATUS));

                if (status == UploadStatus.IN_PROGRESS) {
                    LOGD("Upload with id " + uploadId + " already started");
                    return null;
                }

                //load instance fields
                String instanceId = cursor.getString(Query.INSTANCE_ID);
                Cursor c = contentResolver.query(Instances.buildInstanceUri(instanceId), Instances.Query.PROJECTION, null, null, null);

                if (c != null && c.moveToFirst()) {
                    Instance instance = new Instance(c);
                    ScPublicKey key = new ScPublicKey(instance.getPublicKey());
                    ScApiSession session = ScApiSessionFactory.newSession(instance.getUrl(), key,
                            instance.getLogin(), instance.getPassword());

                    UploadHelper helper = new UploadHelper(mContext);
                    helper.uploadMedia(session, Uploads.buildUploadUri(uploadId), instance, itemName, fileUri.toString());
                } else {
                    // instance deleted -> set error
                    ContentValues values = new ContentValues();
                    values.put(Uploads.STATUS, UploadStatus.ERROR.name());
                    values.put(Uploads.FAIL_MESSAGE, "Instance has been deleted");
                    contentResolver.update(Uploads.buildUploadUri(uploadId), values, null, null);
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

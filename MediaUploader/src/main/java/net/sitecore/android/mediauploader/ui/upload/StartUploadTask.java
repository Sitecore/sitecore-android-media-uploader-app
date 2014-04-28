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
        Cursor uploadsCursor = contentResolver.query(Uploads.buildUploadUri(uploadId), Query.PROJECTION, null, null, null);
        try {
            // load upload fields
            if (uploadsCursor != null && uploadsCursor.moveToFirst()) {
                String itemName = uploadsCursor.getString(Query.ITEM_NAME);
                Uri fileUri = Uri.parse(uploadsCursor.getString(Query.FILE_URI));
                UploadStatus status = UploadStatus.valueOf(uploadsCursor.getString(Query.STATUS));

                if (status == UploadStatus.IN_PROGRESS) {
                    LOGD("Upload with uploadId " + uploadId + " already started");
                    return null;
                }

                //load instance fields
                String instanceId = uploadsCursor.getString(Query.INSTANCE_ID);
                Cursor instancesCursor = contentResolver.query(Instances.buildInstanceUri(instanceId), Instances.Query.PROJECTION, null, null, null);

                try {
                    if (instancesCursor != null && instancesCursor.moveToFirst()) {
                        Instance instance = new Instance(instancesCursor);
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
                } finally {
                    instancesCursor.close();
                }
            } else {
                LOGE("Upload with uploadId " + uploadId + " has been deleted");
                return null;
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGE(e);
        } finally {
            uploadsCursor.close();
        }
        return null;
    }
}

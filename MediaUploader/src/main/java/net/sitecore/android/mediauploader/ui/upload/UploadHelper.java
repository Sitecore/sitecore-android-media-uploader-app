package net.sitecore.android.mediauploader.ui.upload;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.service.MediaUploaderService;
import net.sitecore.android.mediauploader.ui.upload.MediaUploadListener;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.UploadMediaIntentBuilder;

public class UploadHelper {

    public Context mContext;

    public UploadHelper(Context context) {
        mContext = context;
    }

    public void uploadMedia(ScApiSession session, final Uri uploadUri, final Instance instance, final String name,
            final String fileUri) {
        session.setMediaLibraryPath("/");

        MediaUploadListener responseListener = new MediaUploadListener(mContext, uploadUri, instance.getRootFolder(),
                name);

        UploadMediaIntentBuilder builder = session.uploadMediaIntent(instance.getRootFolder(), name, fileUri.toString())
                .setDatabase(instance.getDatabase())
                .setSuccessListener(responseListener)
                .setErrorListener(responseListener)
                .setUploadMediaServiceClass(MediaUploaderService.class);

        if (!TextUtils.isEmpty(instance.getSite())) builder.setSite(instance.getSite());

        Intent intent = builder.build(mContext);
        intent.putExtra(MediaUploaderService.EXTRA_UPLOAD_URI, uploadUri);
        intent.putExtra(MediaUploaderService.EXTRA_MEDIA_FOLDER, instance.getRootFolder());

        mContext.startService(intent);
    }

}

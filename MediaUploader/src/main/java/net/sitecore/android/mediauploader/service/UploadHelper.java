package net.sitecore.android.mediauploader.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.UploadMediaIntentBuilder;

public class UploadHelper {

    public final Context mContext;

    public UploadHelper(Context context) {
        mContext = context;
    }

    public void startUploadService(ScApiSession session,
            final Uri uploadUri,
            final Instance instance,
            final String name,
            final String fileUri,
            final Address address) {
        session.setMediaLibraryPath("/");

        MediaUploadResponseHandler responseListener = new MediaUploadResponseHandler(mContext,
                session, uploadUri, instance.getRootFolder(), name, address);

        // ItemsWebAPI doesn't understand non-alphanumeric chars
        final String cleanName = name.replaceAll("[^A-Za-z0-9]", "");
        UploadMediaIntentBuilder builder = session.uploadMediaIntent(instance.getRootFolder(), cleanName, fileUri)
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

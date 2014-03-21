package net.sitecore.android.mediauploader.service;

import android.content.Intent;

import net.sitecore.android.sdk.api.UploadMediaService;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

;

public class MediaUploaderService extends UploadMediaService {

    static final String EXTRA_UPLOAD_OPTIONS = "net.sitecore.android.sdk.api.EXTRA_UPLOAD_OPTIONS";

    private static final int NOTIFICATION_IN_PROGRESS = 123;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD("MediaUploaderService.onStartCommand");
//        UploadMediaRequestOptions options = intent.getParcelableExtra(EXTRA_UPLOAD_OPTIONS);
//
//        NotificationCompat.Builder builder = new Builder(this);
//        PendingIntent showMainActivityIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
//
//        builder.setContentTitle("Uploading ...")
//                .setContentText(options.getFullUrl())
//                .setContentIntent(showMainActivityIntent)
//                .setSmallIcon(R.drawable.ic_launcher);
//
//        Notification n = builder.build();
//        startForeground(NOTIFICATION_IN_PROGRESS, n);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

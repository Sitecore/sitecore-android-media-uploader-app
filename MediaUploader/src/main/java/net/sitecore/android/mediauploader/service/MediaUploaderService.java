package net.sitecore.android.mediauploader.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.sdk.api.UploadMediaRequestOptions;
import net.sitecore.android.sdk.api.UploadMediaService;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class MediaUploaderService extends UploadMediaService {

    static final String EXTRA_UPLOAD_OPTIONS = "net.sitecore.android.sdk.api.EXTRA_UPLOAD_OPTIONS";

    private static final int NOTIFICATION_IN_PROGRESS = 123;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD("MediaUploaderService.onStartCommand");
        UploadMediaRequestOptions options = intent.getParcelableExtra(EXTRA_UPLOAD_OPTIONS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        PendingIntent showMainActivityIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        builder.setContentTitle("Uploading ...")
                .setContentText(options.getFullUrl())
                .setContentIntent(showMainActivityIntent)
                .setSmallIcon(R.drawable.ic_launcher);

        Notification n = builder.build();
        startForeground(NOTIFICATION_IN_PROGRESS, n);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

package net.sitecore.android.mediauploader.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.sdk.api.UploadMediaService;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MediaUploaderService extends UploadMediaService {

    @Override
    public void onCreate() {
        super.onCreate();
        LOGD("MediaUploaderService.onCreate");

        NotificationCompat.Builder builder = new Builder(this);

        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        builder.setContentTitle("Uploading ....")
                .setContentText("/home/item/name")
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_launcher);
        //                .setSubText("sub text bla bla");

        Notification n = builder.build();
        startForeground(123, n);
        //NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //nm.notify(123, n);

    }

    @Override
    public void onDestroy() {
        LOGD("MediaUploaderService.onDestroy");
        stopForeground(true);
        super.onDestroy();
    }
}

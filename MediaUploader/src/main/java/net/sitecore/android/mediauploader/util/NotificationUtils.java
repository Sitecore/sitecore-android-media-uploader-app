package net.sitecore.android.mediauploader.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.upload.UploadsListActivity;

public class NotificationUtils {

    public static void showNotification(Context context, String title, String contentText, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        PendingIntent showMainActivityIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, UploadsListActivity.class), 0);

        builder.setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(showMainActivityIntent)
                .setSmallIcon(R.drawable.ic_launcher);

        Notification n = builder.build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, n);
    }
}

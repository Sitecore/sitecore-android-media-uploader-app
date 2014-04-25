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

    public static Notification showInProgressNotification(Context context, String title, String contentText) {
        final Notification n = buildDefaultNotification(context, title, contentText, false, R.drawable.stat_sys_upload)
                .setProgress(0, 0, true)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(title.hashCode(), n);
        return n;
    }

    public static Notification showFinishedNotification(Context context, String title, String contentText) {
        final Notification n = buildDefaultNotification(context, title, contentText, true, R.drawable.stat_notify_info)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(title.hashCode(), n);
        return n;
    }

    public static Notification showErrorNotification(Context context, String title, String contentText) {
        final Notification n = buildDefaultNotification(context, title, contentText, true, R.drawable.stat_notify_error)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(title.hashCode(), n);
        return n;
    }

    private static NotificationCompat.Builder buildDefaultNotification(Context context, String title,
            String contentText, boolean autoCancel, int notificationIcon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        PendingIntent showMainActivityIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, UploadsListActivity.class), 0);

        builder.setContentTitle(title)
                .setAutoCancel(autoCancel)
                .setContentText(contentText)
                .setContentIntent(showMainActivityIntent)
                .setSmallIcon(notificationIcon);

        return builder;
    }
}

package com.efi.PrintMeGoogle.notificationmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.RegistrationActivity;

public class NotificationHandler {

    public static void unEmailHash(Context context, NotificationCompat.Builder mBuilder, NotificationManager mNotifyManager, int id) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle(context.getResources().getString(R.string.printmeUploadFailed))
                .setContentText(context.getResources().getString(R.string.printmeRegistration))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getResources().getString(R.string.printmeRegistration)));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(BitmapFactory. decodeResource (context.getResources() , R.drawable.notification)) ;
            mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
            mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
        } else {
               mBuilder.setSmallIcon(R.drawable.notification);
        }
        mBuilder.setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setProgress(0, 0, false)
        .setContentIntent(contentIntent);
        mNotifyManager.notify(id, mBuilder.build());
    }


    public static void handlNotifcationError(Context context, NotificationCompat.Builder mBuilder, NotificationManager mNotifyManager, int id, int error) {
        //Log.i("Error: ", "" + error);
        try {
            switch (error) {
                case 502:
                case 400:
                case 401:
                    NotifiactionManager(context, mBuilder, mNotifyManager, id, context.getResources().getString(R.string.general));
                    break;
                case 404:
                    NotifiactionManager(context, mBuilder, mNotifyManager, id, context.getResources().getString(R.string.codeNotFound));
                    break;
                case 429:
                    NotifiactionManager(context, mBuilder, mNotifyManager, id, context.getResources().getString(R.string.rateLimit));
                    break;
                case 408:
                case 504:
                case 503:
                default:
                    NotifiactionManager(context, mBuilder, mNotifyManager, id, context.getResources().getString(R.string.timeout));
                    break;
            }
        } catch (Exception e) {
            //Log.i("Notification Exception", e.toString());
        }
    }

    private static void NotifiactionManager(Context context, NotificationCompat.Builder mBuilder, NotificationManager mNotifyManager, int id, String string) {
//        mNotifyManager.notify(id, mBuilder.build());
        mBuilder.setContentTitle(context.getResources().getString(R.string.printmeUploadFailed))
                .setContentText(string)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(string));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(BitmapFactory. decodeResource (context.getResources() , R.drawable.notification)) ;
            mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
            mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            mBuilder.setSmallIcon(R.drawable.notification);
        }
        mBuilder.setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setProgress(0, 0, false);
        mNotifyManager.notify(id, mBuilder.build());
    }


}

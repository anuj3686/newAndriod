package com.efi.PrintMeGoogle.nativeprint;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.BarcodeActivity;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.notificationmanager.NotificationHandler;
import com.efi.PrintMeGoogle.utils.ApiUtils;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;
import com.efi.PrintMeGoogle.share.ShareAsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import static com.efi.PrintMeGoogle.constants.GeneralConstants.setUploading;


public class UploadAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String CHANNEL_ID = "efipoc";
    private static NotificationManager mNotifyManager;
    private static NotificationCompat.Builder mBuilder;
    NotificationChannel channel;
    static int id = 485612;
    boolean flag;
    long size;
    ArrayList<String> actualFileName = new ArrayList<String>();
    ArrayList<String> fileName = new ArrayList<String>();
    String errorMessage = "";
    static Context context;
    Response response;

    public UploadAsyncTask(ArrayList filename, ArrayList actFileName, String errMessage, long size, Context contxt) {
        this.fileName = filename;
        this.actualFileName = actFileName;
        this.errorMessage = errMessage;
        this.context = contxt;
        this.size = size;
        CommonUtils.getUniqueId(this.context);
    }

    @Override
    protected void onPreExecute() {
        //show loading

        //Log.i("UploadAsyncTask", "Inside onPreExecute");
        try {
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
            mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                    .setContentText(context.getResources().getString(R.string.uploadInprogress))
                    .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(context.getResources().getString(R.string.uploadInprogress)));
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
            } else {
                mBuilder.setSmallIcon(R.drawable.notification);
            }
            Log.e("ShareAsyncTask", "notification uploadAsynk started @@@@@@@@@@ "  );

            mBuilder.setProgress(10, 10, true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getResources().getString(R.string.printMeUpload);
                String description = context.getResources().getString(R.string.uploadInprogress);
                int importance = NotificationManager.IMPORTANCE_LOW;
                channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                //NotificationManager notificationManager = getSystemService(NotificationManager.class);
                mNotifyManager.createNotificationChannel(channel);
            }
            if (size > 70) {
                mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                        .setContentText(context.getResources().getString(R.string.fileSizeError))
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getResources().getString(R.string.fileSizeError)));
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                    mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                    mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    mBuilder.setSmallIcon(R.drawable.notification);
                }
                mBuilder.setProgress(0, 0, false);

            }
            //mNotifyManager.notify(id, mBuilder.build());
            Log.e("ShareAsyncTask", "notification uploadAsynk calling  ShareAsyncTask.notify_notification "  );

            Upload_notify_notification( );
        } catch (Exception e) {
            //Log.e("UploadAsyncTask", "onPreExecute" + e.toString());
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.e("ShareAsyncTask", "notification doInBackground started @@@@@@@@@@ "  );

        //Log.i("UploadAsyncTask", "Inside doInBackground");
        try {
            String email = "";
            SharedPreferences OwnerEmail = context.getSharedPreferences(GeneralConstants.OWNER_EMAIL_PREF, Context.MODE_PRIVATE);
            if (OwnerEmail != null) {
                email = OwnerEmail.getString(GeneralConstants.OWNER_EMAIL, "");
                if (email == "") {
                    flag = true;
                    return null;
                }
            } else {
                //Log.i(GeneralConstants.AppLog, "No Owner Email found ");
                flag = true;
                return null;

            }
            boolean flag = false;
            if (size > 70) {
                cancel(true);
            }
            //Log.d("UploadAsyncTask: ", "OwnerEmail :" + email);
            //Log.d("UploadAsyncTask: ", "actualFileName :" + actualFileName.toString());
            //Log.d("UploadAsyncTask: ", "fileName :" + fileName.toString());
            response = ApiUtils.uploadFileToPrintMe(fileName, actualFileName, email, context, flag);
            //Log.d("UploadAsyncTask: ", "" + response.statusCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onPostExecute(Void s) {
        try {
            Log.e("ShareAsyncTask", "notification onPostExecute started @@@@@@@@@@ "  );

            if (Connectivity.isConnected(context)) {
//            SharedPreferences FileDelete = context.getSharedPreferences(GeneralConstants.Uploading, Context.MODE_PRIVATE);
//            FileDelete.edit().putBoolean(GeneralConstants.Uploading, false).commit();
                setUploading(false);

                if (response != null) {
                    if ((response.statusCode == HttpURLConnection.HTTP_NO_CONTENT ||
                            response.statusCode == HttpURLConnection.HTTP_CREATED ||
                            response.statusCode == HttpURLConnection.HTTP_OK)) {
                        String docId = null;
                        try {
                            JSONObject object = new JSONObject(response.responseData);
                            docId = object.getString("DocID");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayList<String> status = new ArrayList<String>();
                        //Log.d("UploadAsyncTask: ", "response.Status" + response.Status + response.Status.size() + fileName.size());
                        for (int i = 0; i < response.Status.size(); i++) {
                            if (response.Status.get(i).equals("4")) {
                                status.add(response.Status.get(i));
                            }
                        }
                        //Log.d("UploadAsyncTask: ", "response.Status" + status + response.toString());
                        if (status.size() != 0) {
                            try {
                                JSONObject object = new JSONObject(response.responseData);
                                docId = object.getString("DocID");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, BarcodeActivity.class);
                            intent.putExtra("docId", docId);
                            PendingIntent contentIntent;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                contentIntent = PendingIntent.getActivity(context,
                                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                            }else {
                                contentIntent = PendingIntent.getActivity(context,
                                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            }
                                if (mNotifyManager != null) {
                                mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                                        .setContentText(context.getResources().getString(R.string.uploadCompleted))
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(context.getResources().getString(R.string.uploadCompleted)));
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                                    mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                                    mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                                } else {
                                    mBuilder.setSmallIcon(R.drawable.notification);
                                }
                                mBuilder.setAutoCancel(true)
                                        .setDefaults(Notification.DEFAULT_SOUND)
                                        .setProgress(0, 0, false)
                                        .setContentIntent(contentIntent);
//                                    .addAction(android.R.drawable.ic_menu_directions, "VIEW", contentIntent);
                               // mNotifyManager.notify(id, mBuilder.build());
                                    Upload_notify_notification( );
                            } else {
                                //Log.d("UploadAsyncTask:", "Notification is off");

                            }
                        } else {
                            if (mNotifyManager != null) {
                                mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                                        .setContentText(context.getResources().getString(R.string.unableProcessDocument))
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(context.getResources().getString(R.string.unableProcessDocument)));
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                                    mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                                    mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                                } else {
                                    mBuilder.setSmallIcon(R.drawable.notification);
                                }
                                mBuilder.setDefaults(Notification.DEFAULT_SOUND)
                                        //.setPriority(NotificationManager.IMPORTANCE_LOW)
                                        .setProgress(0, 0, false);
                               // mNotifyManager.notify(id, mBuilder.build());
                                Upload_notify_notification( );
                            }
                        }

                    } else if (response.statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                        if (mNotifyManager != null) {
                            mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                                    .setContentText(context.getResources().getString(R.string.unableProcessDocument))
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(context.getResources().getString(R.string.unableProcessDocument)));
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                                mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                                mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                            } else {
                                mBuilder.setSmallIcon(R.drawable.notification);
                            }
                            mBuilder.setDefaults(Notification.DEFAULT_SOUND)
                                    //.setPriority(NotificationManager.IMPORTANCE_LOW)
                                    .setProgress(0, 0, false);
                        }
                        if (mNotifyManager != null)  Upload_notify_notification( );//mNotifyManager.notify(id, mBuilder.build());
                    } else {

                        NotificationHandler.handlNotifcationError(context, mBuilder, mNotifyManager, id, response.statusCode);

                    }
                } else if (response == null && flag == true) {
                    NotificationHandler.unEmailHash(context, mBuilder, mNotifyManager, id);
                } else {
//            mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
//                    .setContentText(context.getResources().getString(R.string.uploadFailed))
//                    .setSmallIcon(R.drawable.notification)
//                    .setProgress(0, 0, false);
//            mNotifyManager.notify(id, mBuilder.build());
                    NotificationHandler.handlNotifcationError(context, mBuilder, mNotifyManager, id, 408);

//            ApiUtils.handleError(408, context);
                }
            } else {
                mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                        .setContentText(context.getResources().getString(R.string.noNetwork))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(context.getResources().getString(R.string.noNetwork)));
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                    mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                    mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    mBuilder.setSmallIcon(R.drawable.notification);
                }
                mBuilder.setDefaults(Notification.DEFAULT_SOUND)
                        .setProgress(0, 0, false);
               // mNotifyManager.notify(id, mBuilder.build());
                Upload_notify_notification( );
            }
        } catch (Exception e) {
            //Log.e("UploadAsyncTask", "onPostExecute" + e.toString());

        }
    }

    public static void Upload_notify_notification(){
        try {
            SharedPreferences notify_status = context.getSharedPreferences(GeneralConstants.NOTIFY_STATUS_PREF, Context.MODE_PRIVATE);
            String notifyVal = notify_status.getString(GeneralConstants.NOTIFY_STATUS, null);
            boolean notify_val = notifyVal == null || notifyVal.isEmpty() || notifyVal.equals("true");
            if (Build.VERSION.SDK_INT <Build.VERSION_CODES.TIRAMISU) {
                
                if (notify_val) {
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }
            if (CommonUtils.isNotify(context)) {
                if (notify_val) {
                    mNotifyManager.notify(id, mBuilder.build());
                    Log.e("uploadAsyncTask", "notification sent @@@@@@@@ "  );
                }

            }


        }catch (Exception e){
            Log.e("ShareAsyncTask", "notification error "  );
        }
    }
}


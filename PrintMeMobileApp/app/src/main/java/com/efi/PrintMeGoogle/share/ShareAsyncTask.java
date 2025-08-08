package com.efi.PrintMeGoogle.share;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.BarcodeActivity;
import com.efi.PrintMeGoogle.activities.MainActivity;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.notificationmanager.NotificationHandler;
import com.efi.PrintMeGoogle.utils.ApiUtils;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import static com.efi.PrintMeGoogle.constants.GeneralConstants.setUploading;

public class ShareAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String CHANNEL_ID = "efipoc";
    private static NotificationManager mNotifyManager;
    private static NotificationCompat.Builder mBuilder;
    NotificationChannel channel;
    static int id = 485612;
    boolean flag;
    private ArrayList<String> actualFileName;
    private ArrayList<File> fileName;
    private String errorMessage = "";
    static Context context;
    int size;
    private Response response;
    private View rootView;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public ShareAsyncTask(ArrayList filename, ArrayList actFileName, String errMessage, Context contxt, int size, AsyncResponse delegate) {
        this.fileName = filename;
        this.actualFileName = actFileName;
        this.errorMessage = errMessage;
        this.context = contxt;
        this.delegate = delegate;
        this.size = size;
        CommonUtils.getUniqueId(this.context);
    }

    protected void onPreExecute() {
        //show loading
        //Log..i("ShareAsyncTask", "Inside onPreExecute");
        //notify_sharedPref
        try {
            SharedPreferences notify_status = context.getSharedPreferences(GeneralConstants.NOTIFY_STATUS_PREF, Context.MODE_PRIVATE);
            String notifyVal = notify_status.getString(GeneralConstants.NOTIFY_STATUS, null);
            if (notifyVal == null || notifyVal.isEmpty() || notifyVal.equals("true")) {
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CharSequence name = context.getResources().getString(R.string.printMeUpload);
                    String description = context.getResources().getString(R.string.uploadInprogress);
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    channel = new NotificationChannel(CHANNEL_ID, name, importance);
                    channel.setDescription(description);
                    mNotifyManager.createNotificationChannel(channel);
                    Log.e("ShareAsyncTask", "notification ShareAsyncTask started @@@@@@@@@@ "  );
                }

            } else {
                //Log..d("ShareAsyncTask:", "Notification is off");
            }

        } catch (Exception e) {
            //Log..e("ShareAsyncTask", "onPreExecute" + e.toString());
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Log..i("ShareAsyncTask", "Inside doInBackground");
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
                //Log..i(GeneralConstants.AppLog, "No Owner Email found ");
                flag = true;
                return null;

            }
            boolean flag = true;
            //Log..d("ShareAsyncTask: ", "OwnerEmail :" + email);

            response = ApiUtils.uploadFileToPrintMe(fileName, actualFileName, email, context, flag);
            //Log..d("ShareAsyncTask: ", "" + response.statusCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {


        super.onProgressUpdate(values);


    }


    protected void onPostExecute(Void s) {
        try {
            if (Connectivity.isConnected(context)) {
                System.out.println("Inside connected" + Connectivity.isConnected(context));
                setUploading(false);

                if (response != null) {
                    if (response.statusCode == HttpURLConnection.HTTP_NO_CONTENT ||
                            response.statusCode == HttpURLConnection.HTTP_CREATED ||
                            response.statusCode == HttpURLConnection.HTTP_OK) {
                        String docId = null;
                        ArrayList<String> status = new ArrayList<String>();
                        //Log..d("ShareAsyncTask: ", "response.Status" + response.Status + response.Status.size() + fileName.size());
                        for (int i = 0; i < response.Status.size(); i++) {
                            if (response.Status.get(i).equals("4")) {
                                status.add(response.Status.get(i));
                            }
                        }
                        //Log..d("ShareAsyncTask: ", "response.Status" + status + response.toString());
                        if (status.size() != 0) {
                            try {
                                JSONObject object = new JSONObject(response.responseData);
                                docId = object.getString("DocID");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, BarcodeActivity.class);
                            intent.putExtra("docId", docId);
                            intent.putExtra("StatusSize", status);
                            intent.putExtra("filesize", size);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent contentIntent;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                contentIntent = PendingIntent.getActivity(context,
                                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                            }else {
                                contentIntent = PendingIntent.getActivity(context,
                                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            }

                          //  PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            if (mNotifyManager != null) {
                                mBuilder.setContentTitle(context.getResources().getString(R.string.printMeUpload))
                                        .setContentText(context.getResources().getString(R.string.uploadSuccessful, docId))
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(context.getResources().getString(R.string.uploadSuccessful,docId)));
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification));
                                    mBuilder.setSmallIcon(R.drawable.ic_transparent_notification);
                                    mBuilder.setColor(context.getResources().getColor(R.color.colorAccent));
                                } else {
                                    mBuilder.setSmallIcon(R.drawable.notification);
                                }
                                //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                mBuilder.setDefaults(Notification.DEFAULT_SOUND)
                                //mBuilder.setSound(defaultSoundUri);
                                        .setAutoCancel(true)
                                        .setContentIntent(contentIntent);
                                Share_notify_notification( );
                                Log.e("ShareAsyncTask", "notification called @@@@@@@@@@ "  );
                            } else {
                                //Log..d("ShareAsyncTask:", "Notification is off");

                            }
                            context.startActivity(intent);
                            ((Activity) context).finish();

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
                                        .setProgress(0, 0, false);
                                Share_notify_notification( );
                            }
                            delegate.processFinish(context.getResources().getString(R.string.unableProcessDocument));
                        }
                    } else if (response.statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                        if (mNotifyManager != null) {
                            if (fileName.size() == size) {
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
                                        .setProgress(0, 0, false);
                                Share_notify_notification( );
                            } else {
                                mNotifyManager.cancelAll();
                            }

                        }
                        delegate.processFinish(context.getResources().getString(R.string.unableProcessDocument));
                    } else {
//                    delegate.processFinish(context.getResources().getString(R.string.unableProcessDocument));
                        NotificationHandler.handlNotifcationError(context, mBuilder, mNotifyManager, id, response.statusCode);

                        ApiUtils.handleError(response.statusCode, context, true);
                    }
                } else if (response == null && flag == true && mNotifyManager != null) {
                    NotificationHandler.unEmailHash(context, mBuilder, mNotifyManager, id);

                    CommonUtils.showRegistrationAlert(context.getResources().getString(R.string.printmeRegistration), context);

                } else {
                    NotificationHandler.handlNotifcationError(context, mBuilder, mNotifyManager, id, 408);
                    ApiUtils.handleError(408, context, true);

                }
            } else {
                CommonUtils.showAlert(context.getResources().getString(R.string.noNetwork), context, true);
            }
        } catch (Exception e) {
            //Log..e("ShareAsyncTask", "onPostExecute" + e.toString());

        }
    }
    public static void Share_notify_notification(){
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

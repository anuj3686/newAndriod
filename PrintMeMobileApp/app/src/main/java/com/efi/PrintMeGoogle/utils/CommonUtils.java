package com.efi.PrintMeGoogle.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.RegistrationActivity;
import com.efi.PrintMeGoogle.constants.GeneralConstants;

import java.io.File;
import java.util.Locale;

public class CommonUtils {
    private static String TAG = "CommonUtils";

    public static void showAlert(String msg, final Context context, final boolean flag) {
        try{
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (flag == true) {
                            ((Activity) context).finish();
                        }
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        setAlertButtonStyle(alertDialog); // added so that button style will reflect in 5.1 os as well
        }catch (Exception e){
            //Log.d(TAG,"Exception showAlert"+e.getMessage());
        }

    }

    public static void checkFileReadPermission( Context context,Activity activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
private static void setAlertButtonStyle(AlertDialog alertDialog){
    // Get the alert dialog buttons reference
    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

    // Change the alert dialog buttons text and background color
    positiveButton.setTextColor(Color.parseColor("#FFFFFF"));
    positiveButton.setBackgroundColor(Color.parseColor("#008AD1"));
    positiveButton.setAllCaps(false); //so that it will not change the case of text(it should be as it is. eg-spanish)

}
    public static void showRegistrationAlert(String msg, final Context context) {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
//                        ShareActivity.finish();
                            Intent intent = new Intent(context, RegistrationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ((Activity) context).startActivity(intent);
                            ((Activity) context).finish();

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            setAlertButtonStyle(alertDialog);
        }catch (Exception e){
            //Log.d(TAG,"Exception showRegistrationAlert"+e.getMessage());

        }
    }

    public static Dialog showProgressDialog(Context context) {
        Dialog pd = new Dialog(context, android.R.style.Theme_Black);
        View view = LayoutInflater.from(context).inflate(R.layout.progressdialog, null);
        pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pd.getWindow().setBackgroundDrawableResource(R.color.transparent);
        pd.setContentView(view);
        return pd;
    }

    public static void dismissProgressDialog(Dialog dialog) {
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            //Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static boolean isRegistered(Context context) {
        SharedPreferences registered = context.getSharedPreferences(GeneralConstants.REGISTERED_PREF, Context.MODE_PRIVATE);
        return registered.getBoolean(GeneralConstants.REGISTERED, false);
    }
    public static boolean isNotify(Context context) {
        SharedPreferences notify = context.getSharedPreferences(GeneralConstants.NOTIFY_PREF, Context.MODE_PRIVATE);
        return notify.getBoolean(GeneralConstants.NOTIFY, false);
    }

//    public static boolean isUploading(Context context) {
//        SharedPreferences registered = context.getSharedPreferences(GeneralConstants.Uploading, Context.MODE_PRIVATE);
//        return registered.getBoolean(GeneralConstants.Uploading, false);
//    }

    public static String getTrimmedFileName(String fileName) {
        if (fileName.length() <= 30) {
            return fileName;
        } else {
            // Assuming 3 characters for dots to suggest truncation
            int fileNameFirstHalfLength = 11;
            // Trim the file name alone
            String trimmedFileNameWithExtension = fileName.substring(0, fileNameFirstHalfLength) + // First 11 characters
                    "..." + fileName.substring(fileName.length() - 16);
            // combine and send
            return trimmedFileNameWithExtension;
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public static boolean deleteDir(File dir) {
        try {
            //Log.i(GeneralConstants.AppLog, "Inside deleteDir");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

            return dir.delete();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deletefile(File file) {
        try {
            //Log.i(GeneralConstants.AppLog, "Inside deleteFile");
            if (file.isFile()) {
                return file.delete();
            } else {
                return false;
            }
        } catch (Exception e) {

            return false;
        }

    }
    public static void getUUID(Context context) {
        try {
            String androidId;
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                androidId = mngr.getDeviceId();
                //Log.d(" UUID deviceId ", androidId);

            } else {
                androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                //Log.d(" UUID androidId ", androidId);

            }
            GeneralConstants.setSerial_number(androidId);
            setUinqueId(context);
        } catch (Exception e) {
            //Log.e(" UUID Excep", e.getMessage());
        }
    }

    //keep serial/uuid in sharedPref, so that it will get fetch when native print or share will happen
    public static void setUinqueId(Context context) {
        try {
            String uniqueId = GeneralConstants.getSerial_number();
            //Log.d("setUinqueId id :", uniqueId);
            if (uniqueId != null && !uniqueId.isEmpty() && uniqueId !="unknown") {
                SharedPreferences unique_id = context.getSharedPreferences(GeneralConstants.UNIQUE_ID_PREF, Context.MODE_PRIVATE);
                unique_id.edit().putString(GeneralConstants.UNIQUE_ID, uniqueId).commit();
            }
        } catch (Exception e) {
            //Log.e(TAG, "Excep setUinqueId " + e.getMessage());
        }
    }

    public static void getUniqueId(Context context) { //calling this function before all api calls, so that uuid will set properly.
        try {
            String uniqueId=GeneralConstants.getSerial_number();
            if(uniqueId == null || uniqueId.isEmpty() || uniqueId =="unknown") {
                SharedPreferences unique_id = context.getSharedPreferences(GeneralConstants.UNIQUE_ID_PREF, Context.MODE_PRIVATE);
                uniqueId = unique_id.getString(GeneralConstants.UNIQUE_ID, null);
                //Log.d("getUniqueId from sp :", uniqueId);
                if (uniqueId != null && !uniqueId.isEmpty())
                    GeneralConstants.setSerial_number(uniqueId);
            }
        } catch (Exception e) {
            //Log.e(TAG, "getUniqueId Excep " + e.getMessage());
        }
    }
    public static void getCurrentLocale(Context context){
        try {
            String locale= Locale.getDefault().getLanguage();
            //Log.d(TAG,"locale default :"+locale);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                locale= context.getResources().getConfiguration().getLocales().get(0).getLanguage();
            else
                locale= context.getResources().getConfiguration().locale.getLanguage();

            if(locale !=null && !locale.isEmpty()) {
                //Log.d(TAG, "Locale :" + locale);
                GeneralConstants.setLocaleCode(locale);
            }
        } catch (Exception e) {
            //Log.e(":Excep ", "fetching locale" + e.getMessage());
        }

    }


}

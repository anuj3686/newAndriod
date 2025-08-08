package com.efi.PrintMeGoogle.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class Connectivity {


    static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    public static boolean isConnected(final Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    //Log.i("Inside Connectivity", String.valueOf(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)));
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

                } else {
                    //Log.i("Inside Connectivity", String.valueOf(false));
                    return false;
                }
            }
        } else {
            NetworkInfo info = Connectivity.getNetworkInfo(context);

            return (info != null && info.isConnected());
        }
        return false;
    }
}

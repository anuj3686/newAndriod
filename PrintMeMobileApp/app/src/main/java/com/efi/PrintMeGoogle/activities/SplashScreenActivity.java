package com.efi.PrintMeGoogle.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.utils.CommonUtils;

import java.io.File;

import static com.efi.PrintMeGoogle.constants.GeneralConstants.getUploading;

public class SplashScreenActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Set transparent status and navigation bars
        Window window = getWindow();
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        // Configure system bars behavior
        WindowInsetsControllerCompat windowInsetsController = 
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
        
        setContentView(R.layout.activity_splash_screen);
        context = this;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        boolean isUploading = getUploading();
        if (isUploading == false) {
            File dir = new File(getExternalCacheDir(), "PrintMeTemp");
            CommonUtils.deleteDir(dir);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    CommonUtils.getUUID(context); //setting the uuid in sharedPref here oly
                    if (CommonUtils.isRegistered(context)) {
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, RegistrationActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } catch (Exception e) {
                    //Log.e("SplashScreen Excep ", e.getMessage());
                }

            }
        }, 1500);
    }

}

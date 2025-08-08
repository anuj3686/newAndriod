package com.efi.PrintMeGoogle.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


import androidx.appcompat.app.AppCompatActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.fragments.ListOfDrnFragment;
import com.efi.PrintMeGoogle.fragments.SettingsFragment;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
   // public static boolean permission_granted=false;
    final int PERMISSION_REQUEST_CODE =112;
    final String PERMISSION_CODE="112";

    private Context context;
    private ActionBar actionbar;
    private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set to false to tell the system that your layout handles insets.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the custom status bar color. This is independent of the layout
        // and sets the background color of the status bar itself.
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
        
        // Make the system bars appear on top of the content
        WindowInsetsControllerCompat windowInsetsController = 
            ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            // Configure the behavior of the hidden system bars
            windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        context = this;

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU) {
                Log.e("Mainactivity", "permission Asking for notification @@@@@@@@ 1"  );
            if (!shouldShowRequestPermissionRationale(PERMISSION_CODE)){
                Log.e("Mainactivity", "permission @@@@@@@@@@@@@@@@@@@@@@@@@@ for notification @@@@@@@@ requared "  );
                getNotificationPermission();
            }
        }

        if (!CommonUtils.isRegistered(context)) {
            SharedPreferences registered = context.getSharedPreferences(GeneralConstants.REGISTERED_PREF, Context.MODE_PRIVATE);
            registered.edit().putBoolean(GeneralConstants.REGISTERED, true).commit();
        }

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_files);

        // load the store fragment by default
        actionbar.setTitle(getResources().getString(R.string.files));
    }

    public void getNotificationPermission(){
        try {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU) {
                Log.e("Mainactivity", "permission Asking for notification @@@@@@@@ 2"  );
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }

        }catch (Exception e){

        }
    }


        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                SharedPreferences permission_granted = context.getSharedPreferences(GeneralConstants.NOTIFY_PREF, Context.MODE_PRIVATE);
                Log.e("Mainactivity", "permission requestcode notification  " +PERMISSION_REQUEST_CODE );

                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_granted.edit().putBoolean(GeneralConstants.NOTIFY, true).commit();
                    Log.e("Mainactivity", "permission granted for notification  "  );
                }  else {

                    permission_granted.edit().putBoolean(GeneralConstants.NOTIFY, false).commit();
                    Log.e("Mainactivity", "permission Denied  notification  "  );
                }
                return;

        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            try {
                Fragment fragment;

                switch (item.getItemId()) {
                    case R.id.navigation_settings:
                        actionbar.setTitle(getResources().getString(R.string.settings_title));
                        fragment = new SettingsFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_files:
                        actionbar.setTitle(getResources().getString(R.string.files));
                        fragment = new ListOfDrnFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_location:
                        loadMap();
                }
            } catch (Exception e) {

                           }
            return false;

        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onBackPressed() {
        try {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    private void loadMap() {
        String uriString = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2MapPath;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            //Log.e("Exec", "onClick", ex);
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Select Browser"));
        }
    }

}

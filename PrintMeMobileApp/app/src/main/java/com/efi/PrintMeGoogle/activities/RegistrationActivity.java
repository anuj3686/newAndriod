package com.efi.PrintMeGoogle.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.utils.ApiUtils;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


public class RegistrationActivity extends AppCompatActivity {
    private EditText emailEdiText;
    private Button nextButton;
    private String emailPattern;
    private String emailAddress;
    private Context context;
    private static final String TAG = "RegistrationAtivity";
    private TextView registrationTitle, emailErrorText;
    private boolean isEmailExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true); // True for dark icons (light background), false for light icons (dark background)
            windowInsetsController.setAppearanceLightNavigationBars(true); // Also set navigation bar icons if needed
            windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
        setContentView(R.layout.activity_registration);
        context = this;
        //////Log.d(TAG, "started");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            initComponent();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                boolean isAddEmail = extras.getBoolean("addEmail");
                if (isAddEmail) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
                    registrationTitle.setText(getResources().getString(R.string.addEmail));
                }

            } else {
                if (actionBar != null) actionBar.hide();
                CommonUtils.checkFileReadPermission(context,RegistrationActivity.this);

            }

        } catch (Exception e) {
            //////Log.e(TAG, e.getMessage());
        }
        emailPattern =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@" +
                        "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" +
                        "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\." +
                        "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" +
                        "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|" +
                        "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        nextButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View view) {

                ////Log.i(TAG, "next button clicked");
                goAhead();

            }
        });

        emailEdiText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (editable != null && !editable.toString().equalsIgnoreCase("")) {
                        if (emailEdiText.getText().toString().trim().length() < 0) {
                            //emailEdiText.setError(getResources().getString(R.string.invalidEmail));
                            emailErrorText.setVisibility(View.VISIBLE);
                            nextButton.setEnabled(false);

                        } else {
                            if (emailEdiText.getText().toString().matches(emailPattern)) {
                                //emailEdiText.setError(null);
                                emailErrorText.setVisibility(View.INVISIBLE);
                                nextButton.setEnabled(true);
                            } else {
                                //emailEdiText.setError(getResources().getString(R.string.invalidEmail));
                                emailErrorText.setVisibility(View.VISIBLE);
                                nextButton.setEnabled(false);

                            }
                        }
                    }
                } catch (Exception e) {
                    ////Log.d(TAG, "Excep" + e.getMessage());
                }
            }
        });
        emailEdiText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT) {
                    goAhead();
                }
                return false;
            }
        });

    }

    private void goAhead() {
        emailAddress = emailEdiText.getText().toString().trim();
        if (emailAddress.length() > 0 && emailAddress.matches(emailPattern)) {
            emailErrorText.setVisibility(View.INVISIBLE);
            try {
                checkEmailAlreadyExists();
            } catch (Exception e) {
//                //Log.e(TAG, e.getMessage());
            }
        } else {
            //emailEdiText.setError(getResources().getString(R.string.invalidEmail));
            emailErrorText.setVisibility(View.VISIBLE);

        }
    }

    private void initComponent() {
        emailEdiText = findViewById(R.id.emailEdiText);
        nextButton = findViewById(R.id.nextButton);
        registrationTitle = findViewById(R.id.registrationTitle);
        emailErrorText = findViewById(R.id.email_error_text);
    }

    private boolean checkEmailAlreadyExists() {
        //Log.i(TAG, "Inside checkEmailAlreadyExists");

        SharedPreferences emailHash = context.getSharedPreferences(GeneralConstants.EMAIL_HASH_PREF, Context.MODE_PRIVATE);
        if (emailHash != null) {
            ////Log.i(TAG, "Inside emailHash");
            String json;

            json = emailHash.getString(GeneralConstants.EMAIL_HASH, null);
            ////Log.i(TAG, "JSOn data " + json);

            JSONArray jsonArray = null;
            try {
                if (json != null) {
                    jsonArray = new JSONArray(json);
                    //Log.d(TAG, jsonArray.toString());
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            isEmailExist = emailExistsOrNot(jsonArray, i);
                            if (isEmailExist) {
                                CommonUtils.showAlert(getResources().getString(R.string.emailExist), context, false);
                                emailAddress = "";
                                break;
                            }
                        }
                    }
                    if (!isEmailExist) {
                        if (Connectivity.isConnected(context)) {
                            new RegistrationAsyncTask(context).execute();
                        } else {
                            CommonUtils.showAlert(context.getResources().getString(R.string.noNetwork), context, false);
                        }
                    }

                } else {
                    if (Connectivity.isConnected(context)) {
                        new RegistrationAsyncTask(context).execute();
                    } else {
                        CommonUtils.showAlert(context.getResources().getString(R.string.noNetwork), context, false);
                    }
                }

            } catch (JSONException e) {
                //Log.e(TAG, e.getMessage());

            }

        }
        return false;
    }



    private boolean emailExistsOrNot(JSONArray jsonArray, int i) {
        try {

            JSONObject jObject = null;
            jObject = jsonArray.getJSONObject(i);
            String existing_email = jObject.getString("email");
            //Log.d("Existing Email>>>>", existing_email);

            //Log.d("Current Email", emailAddress);
            if (emailAddress.toUpperCase().equals(existing_email.toUpperCase())) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    private class RegistrationAsyncTask extends AsyncTask<Void, Void, Void> {
        Response response;
        Dialog dialog;
        Context context;

        public RegistrationAsyncTask(Context ctx) {
            context = ctx;
        }

        @Override
        protected void onPreExecute() {
            //show loader
            dialog = CommonUtils.showProgressDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Log.d(TAG, "Inside doInBackground");
            try {
                CommonUtils.getUniqueId(context);
                CommonUtils.getCurrentLocale(context);
                response = ApiUtils.emailRegistrationApi(emailAddress);
//                if (response != null) Log.d(TAG, "" + response.statusCode);
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.dismissProgressDialog(dialog);

            }
            return null;

        }
        @Override
        protected void onPostExecute(Void s) {
            try {
                CommonUtils.dismissProgressDialog(dialog);
                if (response != null) {
                    if (response.statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                        intent.putExtra("emailAddress", emailAddress);
                        startActivity(intent);

                    } else {
                        ApiUtils.handleError(response.statusCode, context, false);
                    }
                } else {
                    ApiUtils.handleError(408, context, false);
                }
            } catch (Exception e) {
                //Log.d(TAG, e.getMessage());
            }
        }


    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
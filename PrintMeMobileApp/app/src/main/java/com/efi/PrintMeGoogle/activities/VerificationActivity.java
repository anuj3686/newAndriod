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
import android.text.InputFilter;
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


public class VerificationActivity extends AppCompatActivity {
    private EditText codeEdiText;
    private Button okButton;
    private String emailAddress;
    private String codePattern;
    private String verification_code;
    private int count=0;
    private String referenceHash;
    private Response response;
    private Context context;
    TextView validationErrorText;
    private static final String TAG = "VerificationAtivity";
    private SharedPreferences emailHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true); // True for dark icons (light background), false for light icons (dark background)
            windowInsetsController.setAppearanceLightNavigationBars(true); // Also set navigation bar icons if needed
        }
        setContentView(R.layout.activity_verification);
        //Log.d(TAG," started");
        context=this;
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            initComponent();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                emailAddress = extras.getString("emailAddress");
                codePattern = "^[a-zA-Z0-9]+";
            }
        }catch (Exception e){
            //Log.e(TAG,e.getMessage());
        }
        emailHash = getApplicationContext().getSharedPreferences(GeneralConstants.EMAIL_HASH_PREF, Context.MODE_PRIVATE);

        okButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View view) {

                //Log.i(TAG,"ok button clicked");
                goAhead();
            }
        });


        codeEdiText.addTextChangedListener(new TextWatcher() {
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
                        if (codeEdiText.getText().toString().trim().length() < 0) {
                            //codeEdiText.setError(getResources().getString(R.string.invalidCode));
                            validationErrorText.setVisibility(View.VISIBLE);
                        } else {
                            if (codeEdiText.getText().toString().length() == 6 && codeEdiText.getText().toString().matches(codePattern)) {
                                //codeEdiText.setError(null);
                                validationErrorText.setVisibility(View.INVISIBLE);
                                okButton.setEnabled(true);
                            }else {
                                //codeEdiText.setError(getResources().getString(R.string.invalidCode));
                                validationErrorText.setVisibility(View.VISIBLE);
                                okButton.setEnabled(false);
                            }

                        }
                    }
                }catch (Exception e){
                    //Log.e(TAG,e.getMessage());
                }
            }
        });
        codeEdiText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT){
                    goAhead();
                }

                return false;
            }
        });

    }

    private void goAhead() {
        verification_code = codeEdiText.getText().toString().trim();
        if(verification_code.length() == 6 && verification_code.matches(codePattern)){
            validationErrorText.setVisibility(View.INVISIBLE);
            codeEdiText.getText().clear();
            okButton.setEnabled(false);
            if(Connectivity.isConnected(context)) {
                new VerificationAsyncTask(context).execute();
            }else{
                CommonUtils.showAlert(context.getResources().getString(R.string.noNetwork),context,false);
            }
        }else {
            //codeEdiText.setError(getResources().getString(R.string.invalidCode));
            validationErrorText.setVisibility(View.VISIBLE);
            okButton.setEnabled(false);
        }
    }

    private void initComponent() {
        codeEdiText = findViewById(R.id.codeEdiText);
        okButton = findViewById(R.id.okButton);
        validationErrorText = findViewById(R.id.validation_error_text);
        codeEdiText.setFilters(new InputFilter[]{new InputFilter.AllCaps(),new InputFilter.LengthFilter(6)});

    }




    private class VerificationAsyncTask extends AsyncTask<Void, Void, Void> {
        Dialog dialog;
        Context context;
        public VerificationAsyncTask(Context ctx) {
        context=ctx;
        }

        @Override
        protected void onPreExecute() {
            //show loader
            dialog= CommonUtils.showProgressDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                CommonUtils.getUniqueId(context);
                response = ApiUtils.codeValidationApi(emailAddress, verification_code);
                    if (response != null) {
                        //Log.d(TAG, "" + response.statusCode);
                        if (response.statusCode == HttpURLConnection.HTTP_OK && response.responseData != null && !response.responseData.isEmpty()) {
                            //take hash
                            try {
                                JSONObject jsonObject = new JSONObject(response.responseData);
                                referenceHash = jsonObject.get("ReferenceHash").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
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
                if (referenceHash != null && !referenceHash.isEmpty()) {
                    prepareData(emailHash, referenceHash);
                } else {
                    ApiUtils.handleError(response.statusCode, context,false);
                }
            }catch (Exception e){
                //Log.e(TAG,e.getMessage());
            }
        }

    }

    private void prepareData(SharedPreferences emailHash, String referenceHash) {
        String json;
        json = emailHash.getString(GeneralConstants.EMAIL_HASH, null);
        //Log.d(TAG,"json"+json);
        JSONArray jsonArray= null;
        try {

            if (json != null) {
                jsonArray = new JSONArray(json);
                //Log.d(TAG,"json"+jsonArray);

                //Log.d(TAG,"json length :"+jsonArray.length());
                count = jsonArray.length();
            }
            //Log.d(TAG,"count :"+count);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        try {
            //Log.d("TAG","Inside PrePareDAta");
            JSONObject emailWithHash = new JSONObject();
            emailWithHash.put("referenceHash", referenceHash);
            emailWithHash.put("email", emailAddress);
            emailWithHash.put("email_count", count); //to maintain the oldest email, if user deletes the primary email
            emailWithHash.put("status", "true"); //added due to re-registration functionality, since for new registration re-registration not required.
            pushPreparedData(emailHash, emailWithHash,jsonArray);

        } catch (JSONException e) {
            //Log.e(TAG,e.getMessage());
        }
    }

    private void pushPreparedData(SharedPreferences emailHash, JSONObject emailWithHash, JSONArray jsonArray) {
        SharedPreferences.Editor editor = emailHash.edit();
        if (count == 0) {
            JSONArray emailHashjsonArray = new JSONArray();
            //Log.d(TAG, "Registering first tym");
            emailHashjsonArray.put(emailWithHash);
            editor.putString(GeneralConstants.EMAIL_HASH, emailHashjsonArray.toString()).commit();
            //save ownerEmail
            SharedPreferences OwnerEmail = getApplicationContext().getSharedPreferences(GeneralConstants.OWNER_EMAIL_PREF, Context.MODE_PRIVATE);
             OwnerEmail.edit().putString(GeneralConstants.OWNER_EMAIL,this.emailAddress).commit();

        } else {
            //Log.d(TAG, "Registering multiple tyms");
            jsonArray.put(emailWithHash);
            editor.putString(GeneralConstants.EMAIL_HASH, jsonArray.toString()).commit();

        }
        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
        startActivity(intent);
        VerificationActivity.this.finish();

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

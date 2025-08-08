package com.efi.PrintMeGoogle.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.utils.ApiUtils;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class BarcodeActivity extends AppCompatActivity {

    ImageView qrCode;
    String docId;
    ArrayList Status;
    int fileSize;
    LinearLayout progress;
    RelativeLayout barcode_layout;
    private Context context;
    private String TAG = "BarcodeActivity";


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
            windowInsetsController.setAppearanceLightStatusBars(true); // True for dark icons (light background)
            windowInsetsController.setAppearanceLightNavigationBars(true); // Also set navigation bar icons
            windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
        
        setContentView(R.layout.fragment_barcode);
        progress=findViewById(R.id.progress);
        barcode_layout=findViewById(R.id.barcode_layout);
        barcode_layout.setVisibility(View.GONE);
        //Log.d(TAG, "onCreateView");
        try {
            Intent intent = getIntent();
            docId = intent.getExtras().getString("docId");
            Status = intent.getExtras().getIntegerArrayList("StatusSize");
            fileSize = intent.getExtras().getInt("filesize");
            context = this;
            if (Status != null && fileSize != 0) {
                if (Status.size() != fileSize) {
                    int val = fileSize - Status.size();
                    String message = context.getResources().getString(R.string.partialFail);
                    message = message.replace("%s", String.valueOf(val));
                    message = message.replace("%d", String.valueOf(fileSize));
                    //Log.d(TAG, "failure msg :" + message);
                    CommonUtils.showAlert(message, context, false);
                }
            }
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            if (Connectivity.isConnected(context)) {
                new GetBarcodedata(context).execute();
            } else {
                CommonUtils.showAlert(context.getResources().getString(R.string.noNetwork), context, false);
            }

            ImageView btnAdd = findViewById(R.id.close_btn);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
            //Log.e(TAG, "Exception in onCreate " + e.getMessage());
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class GetBarcodedata extends AsyncTask<Void, Void, Void> {
        Response response;
        Context context;

        public GetBarcodedata(Context ctx) {
            context = ctx;
        }

        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                CommonUtils.getUniqueId(context);
                response = ApiUtils.getBarCodeApi(docId);
                //Log.i("response2", response.responseData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void s) {
            progress.setVisibility(View.GONE);
            barcode_layout.setVisibility(View.VISIBLE);

            try {
                if (response != null) {
                    if (response.statusCode == HttpURLConnection.HTTP_OK) {
                        String responseData = response.responseData;

                        extractValues(responseData);
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        try {
                            qrCode = (ImageView) findViewById(R.id.imageView);
                            BitMatrix bitMatrix = multiFormatWriter.encode(docId, BarcodeFormat.CODE_128, 450, 150);
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            qrCode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }

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


        private void extractValues(String responseData) {
            String values[] = null;
            String documentId = "";
            //Log.i("Inside", "Extract");
            try {
                JSONObject object = new JSONObject(responseData);
                extractOtherDetails(object);
                docId = object.getString("DocID");
                //Log.d(TAG + " DocID", docId);
                JSONArray jsonarray = object.getJSONArray("Documents");
                //Log.i("Inside", String.valueOf(jsonarray.length()));
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    int docIdLength = docId.length();
                    //Log.i("Inside", String.valueOf(docIdLength));
                    if (docIdLength >= 2) {
                        extractDocumentDetails(jsonarray, i, docId);
                        //get the max number of characters usable for first and second part
                        double singleElementLength = Math.ceil(docIdLength / 2);
                        //the first part of the string
                        String docId1 = docId.substring(0, (int) singleElementLength);
                        //the second part of the string
                        String docId2 = docId.substring(docId.length() - (int) singleElementLength, docId.length());
                        documentId = docId1 + " " + docId2;
                        TextView t = (TextView) findViewById(R.id.docId);
                        t.setText(documentId);
                    } else {
                        //Log.i(TAG, "Not valid DocId");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void extractDocumentDetails(JSONArray jsonArray, int i, String docId) {
            //Log.i("Inside length ", jsonArray.toString());
            StringBuilder fileName = new StringBuilder();

            int lengthOfFile = 0;
            String status;
            String value = "4";
            TextView t = findViewById(R.id.filename);

            try {
                lengthOfFile = jsonArray.length();
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonObjectDoc = jsonArray.getJSONObject(j);
                    status = jsonObjectDoc.getString("Status");
                    //Log.d(TAG + " status", "" + status);

                    if (status.equals(value)) {
                        String trimmedFileName = CommonUtils.getTrimmedFileName(jsonObjectDoc.getString("Name"));
                        fileName.append(trimmedFileName);
                        fileName.append("\n");
                        if (j != lengthOfFile - 1) {
                            fileName.append("\n");
                        }
                        t.setText(fileName);
                        //Log.d(TAG + " status", "" + fileName);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        private void extractOtherDetails(JSONObject jsonObject) {
            String dateTime = "";
            String expireTime = "";
            try {

                dateTime = jsonObject.getString("Created");
                expireTime = jsonObject.getString("Expiration");
                String dateTimeZone = dateTime + ".000Z";
                String expireTimeZone = expireTime + ".000Z";
                //Log.d("LIST datetime :", "" + dateTime);
                String expires_in = expDateDiff(expireTimeZone);
                TimeZone tz = TimeZone.getDefault();
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                SimpleDateFormat output = new SimpleDateFormat("MMM dd, HH:mm");
                input.setTimeZone(TimeZone.getTimeZone(tz.getDisplayName()));
                Date date = null;
                try {
                    date = input.parse(dateTimeZone);
                    //Log.i("DATE", "" + date);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formatted = output.format(date);
                //Log.i("DATE", "" + formatted);
                TextView t = findViewById(R.id.uploadedAt);
                t.setText(formatted);
                TextView E = findViewById(R.id.expiresIn);
                E.setText(expires_in);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String expDateDiff(String EXPIREDATETIME) {
            TimeZone tz = TimeZone.getDefault();

            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            input.setTimeZone(TimeZone.getTimeZone(tz.getDisplayName()));
            String CURRENTDATETIME = input.format(new Date());

            Date CurrentDateTime = null;
            Date ExpireDateTime = null;
            try {
                CurrentDateTime = input.parse(CURRENTDATETIME);
                ExpireDateTime = input.parse(EXPIREDATETIME);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            long different = ExpireDateTime.getTime() - CurrentDateTime.getTime();
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;


            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;

            System.out.println(Math.abs(elapsedHours) + "h" + " " + Math.abs(elapsedMinutes) + "m");
            return Math.abs(elapsedHours) + context.getResources().getString(R.string.hour) + " " + Math.abs(elapsedMinutes) + context.getResources().getString(R.string.minute);
        }


    }
}

package com.efi.PrintMeGoogle.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.connection.ConnectionMain;
import com.efi.PrintMeGoogle.connection.Constants;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.constants.GeneralConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ApiUtils {

    private static String client_secret = GeneralConstants.Client_Secret;
    private static String client_id = GeneralConstants.Client_Id;
    private static String timeStamp;
    private static String signature;

    ApiUtils() { }
    public static HashMap<String, String> getHeaders(String signature) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("PrintMe-Serial", GeneralConstants.getSerial_number());
        headers.put("PrintMe-ClientId", client_id);
        headers.put("PrintMe-Signature", signature);
        headers.put("PrintMe-Timestamp", timeStamp + "");

        return headers;

    }

    public static HashMap<String, String> getUploadHeaders(String signature, String actualFileName) {
        HashMap<String, String> headers = new HashMap<>();
        //headers.put("Content-Type", "application/json");

        headers.put("PrintMe-ClientId", client_id);
        headers.put("PrintMe-Signature", signature);
        headers.put("PrintMe-Timestamp", timeStamp + "");
        headers.put("PrintMe-Serial", GeneralConstants.getSerial_number());
        headers.put("PrintMe-Original-Filename", actualFileName);


//        String encodedFileName = "";
//        try {
//            encodedFileName = URLEncoder.encode(actualFileName, StandardCharsets.UTF_8.name());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        if(!encodedFileName.isEmpty())
//            headers.put("PrintMe-Original-Filename", encodedFileName);

        return headers;

    }


    public static String GetSignature(String methodType, String path, String body) {
        timeStamp = Long.toString(System.currentTimeMillis() / 1000);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("timestamp=" + timeStamp);
        queryBuilder.append("method=" + methodType);
        queryBuilder.append("resource=" + path);
        String data = queryBuilder.toString().toLowerCase();
        data += body;
        //Log.i("GetSignature time", timeStamp);
        //Log.i("GetSignature method", methodType);
        //Log.i("GetSignature res", path);

        //Log.i("GetSignature data", data);
        //Log.i("GetSignature body", "" + body);
        //Log.i("GetSign client_secret", client_secret);


        try {
            signature = hash_hmac(data, client_secret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

    private static String hash_hmac(String str, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        String hash = Base64.encodeToString(sha256_HMAC.doFinal(str.getBytes()), Base64.DEFAULT);
        return hash;
    }

    public static Response processRequest(String requestJson, String api, String requestType, HashMap<String, String> headers, String queryParams, HashMap<String, String> uploadFiles) throws Exception {
        HashMap<String, Object> RequestParams = new HashMap<String, Object>();
        RequestParams.put(Constants.TLS_VERSION, "TLSv1.2");
        RequestParams.put(Constants.REQUEST, requestJson);
        RequestParams.put(Constants.SERVICE_URL, api);
        RequestParams.put(Constants.CONTENT_TYPE, "application/json");
        RequestParams.put(Constants.REQUEST_HEADERS, headers);
        RequestParams.put(Constants.QUERY_PARAMS, queryParams);
        RequestParams.put(Constants.CONNECTION_TIMEOUT, 3 * 1000);
        RequestParams.put(Constants.WRITE_TIMEOUT, 30 * 1000);
        RequestParams.put(Constants.READ_TIMEOUT, 30 * 1000);
        RequestParams.put(Constants.RequestType, requestType);
        RequestParams.put(Constants.UPLOAD_FILE, uploadFiles);
        return (Response) ConnectionMain.SendRequest(RequestParams);


    }


    public static Response emailRegistrationApi(String email) throws Exception {
        //Log.d("REG:","locale :"+GeneralConstants.getLocaleCode());
        JSONObject jsonObjectparams = new JSONObject();
        jsonObjectparams.put("Email", email);
        jsonObjectparams.put("Serial", GeneralConstants.getSerial_number());
        jsonObjectparams.put("Locale", GeneralConstants.getLocaleCode());
        String api = GeneralConstants.PM2RegistrationPath;
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2RegistrationPath;
        //Log.i("REG:", uri);

        signature = GetSignature(Constants.RequestType_POST, api, jsonObjectparams.toString());
        //Log.i("REG:", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("REG url", uri);
        //Log.i("REG headers", headers.toString());
        //Log.i("REG body", "" + jsonObjectparams);
        return processRequest(null, uri, Constants.RequestType_POST, headers, "" + jsonObjectparams, null);
    }

    public static Response codeValidationApi(String email, String verification_code) throws Exception {
        JSONObject jsonObjectparams = new JSONObject();
        jsonObjectparams.put("Serial", GeneralConstants.getSerial_number());
        jsonObjectparams.put("Code", verification_code);
        jsonObjectparams.put("Email", email);
        String api = GeneralConstants.PM2RegistrationPath;
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2RegistrationPath;
        //Log.i("CODE:", uri);

        signature = GetSignature(Constants.RequestType_PUT, api, jsonObjectparams.toString());
        //Log.i("CODE:", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("CODE url", uri);
        //Log.i("CODE headers", headers.toString());
        //Log.i("CODE body", jsonObjectparams.toString());

        return processRequest(null, uri, Constants.RequestType_PUT, headers, jsonObjectparams.toString(), null);
    }

    public static Response getDocumentInfo(String docId) throws Exception {
        String api = GeneralConstants.PM2DocumentsPath + "/" + docId + "wait=true";
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2DocumentsPath + "/" + docId + "?wait=true";
        //Log.i("CODE:", uri);

        signature = GetSignature(Constants.RequestType_GET, api, "");
        //Log.i("CODE:", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("CODE url", uri);
        //Log.i("CODE headers", headers.toString());

        return processRequest(null, uri, Constants.RequestType_GET, headers, null, null);
    }

    public static Response getBarCodeApi(String docId) throws Exception {
        String api = GeneralConstants.PM2DRNsPath + "/" + docId;
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2DRNsPath + "/" + docId;
        //Log.i("CODE:", uri);

        signature = GetSignature(Constants.RequestType_GET, api, "");
        //Log.i("CODE:", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("CODE url", uri);
//        Log.i("CODE headers", headers.toString());

        return processRequest(null, uri, Constants.RequestType_GET, headers, null, null);
    }


    public static Response listOfDrnsApi(ArrayList<String> referenceHash) throws Exception {
        //Log.d("LIST Array:", "" + referenceHash);
        JSONArray jsonArray = new JSONArray(referenceHash);
        JSONObject jsonObjectparams = new JSONObject();
        jsonObjectparams.put("ReferenceHash", jsonArray);
        String api = GeneralConstants.PM2ListDRNsGetPath;
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2ListDRNsGetPath;
        //Log.i("LIST url", uri);
        String body = jsonObjectparams.toString();
        body = body.replaceAll("\\\\", "");
        signature = GetSignature(Constants.RequestType_POST, api, body);
        //Log.i("LIST Sign", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("LIST headers", headers.toString());
        //Log.i("LIST body", "" + body);
        return processRequest(null, uri, Constants.RequestType_POST, headers, body, null);
    }

    public static Response uploadFileToPrintMe(ArrayList fileName, ArrayList actualFileName, String email, Context context, boolean flag) throws Exception {
        ArrayList docIDs = new ArrayList<>();
        ArrayList Status = new ArrayList<>();
        Response response = null;
        int maxsize = 10;
        for (int i = 0, j = 0; i < fileName.size() && j < actualFileName.size(); i++, j++) {
            //Log.i(GeneralConstants.AppLog, "Inside uploadFIleToPrintMe");
            String api = GeneralConstants.PM2DocumentsPath;
            String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2DocumentsPath;

            //Log.i(GeneralConstants.AppLog, "URI: " + uri);

            signature = GetSignature(Constants.RequestType_POST, api, "");
            //Log.i(GeneralConstants.AppLog, signature);
            HashMap<String, String> headers = getUploadHeaders(signature, actualFileName.get(j).toString());
            //Log.i(GeneralConstants.App//Log. uri);
            //Log.i(GeneralConstants.AppLog, headers.toString());
            //Log.i("REG body",""+jsonObjectparams);
            HashMap<String, String> queryparm = new HashMap<String, String>();
            //headers.put("Content-Type", "application/json");
            queryparm.put("fileName", fileName.get(i).toString());
            queryparm.put("actualFileName", actualFileName.get(j).toString());

            response = processRequest(null, uri, Constants.RequestType_POST, headers, "", queryparm);
            try {

                int statuscode = response.statusCode;
                //Log.d(GeneralConstants.App//Log. "statuscode " + statuscode);
                String data = response.responseData;
                //Log.i(GeneralConstants.AppLog, data);
                JSONObject docidJson = new JSONObject(response.responseData);
                String docID = docidJson.getString("ID");
                //Log.i(GeneralConstants.AppLog, docID);
                int doc = Integer.parseInt(docID);
                Response responseDocInfo = getDocumentInfo(docID);
                JSONObject docInfo = new JSONObject(responseDocInfo.responseData);
                String status = docInfo.getString("Status");
                Status.add(status);
                if (flag == true) {
                    ProgressBar progressBar = (ProgressBar) ((Activity) context).findViewById(R.id.progressBar);
                    progressBar.setProgress(Status.size() * (100 / fileName.size()));
                }
                docIDs.add(doc);
                //Log.i(GeneralConstants.AppLog, "12343 " + status);
            } catch (IllegalStateException e) {
            } catch (Exception e) {
                //Log.d("Exception ", "uploadFileToPrintMe");
            }

            File file = new File(fileName.get(i).toString());
            CommonUtils.deletefile(file);

        }
        try {
            Response responseDrns = uploadDrns(docIDs, email, context);
            response = responseDrns;

        } catch (IllegalStateException e) {

        } catch (Exception e) {
            //Log.d("Exception  ", "uploadFileToPrintMe");
        }
        response.Status = Status;
        return response;

    }


    public static Response uploadDrns(ArrayList docIDs, String email, Context context) throws Exception {
        //check "send Response by Email" option is on/off ?
        int NotificationRequired = 1; //since by default is true
        SharedPreferences email_response_status = context.getSharedPreferences(GeneralConstants.EMAIL_RESPONSE_STATUS_PREF, Context.MODE_PRIVATE);
        String responseVal = email_response_status.getString(GeneralConstants.EMAIL_RESPONSE_STATUS, null);
        //Log.d("uploadDrns", "" + responseVal);
        if (responseVal == null || responseVal.isEmpty() || responseVal.equals("true"))
            NotificationRequired = 1;
        else NotificationRequired = 0;
        JSONArray jsArray = new JSONArray(docIDs);
        JSONObject jsonObjectparams = new JSONObject();
        jsonObjectparams.put("DocumentIds", jsArray);
        jsonObjectparams.put("OwnerEmail", email);
        jsonObjectparams.put("UploadClient", GeneralConstants.UploadClientIdAndroid);
        jsonObjectparams.put("EmailTo", GeneralConstants.DefaultCustomEmail);
        jsonObjectparams.put("IsNotificationRequired", NotificationRequired);
        String api = GeneralConstants.PM2DRNsPath + "clientType=android";
        String uri = GeneralConstants.PM2APIBasePath + GeneralConstants.PM2DRNsPath + "?clientType=android";
        //Log.i("uploadDrns:", uri);

        signature = GetSignature(Constants.RequestType_POST, api, jsonObjectparams.toString());
        //Log.i("uploadDrns:", signature);
        HashMap<String, String> headers = getHeaders(signature);
        //Log.i("uploadDrns url", uri);
        //Log.i("uploadDrns headers", headers.toString());
        //Log.i("uploadDrns body", "" + jsonObjectparams);
        return processRequest(null, uri, Constants.RequestType_POST, headers, "" + jsonObjectparams, null);

    }

    public static void handleError(int error, Context context, boolean flag) {
        //Log.i("Error: ", "" + error);
        switch (error) {
            case 502:
            case 400:
            case 401:
                CommonUtils.showAlert(context.getResources().getString(R.string.general), context, flag);
                break;
            case 404:
                CommonUtils.showAlert(context.getResources().getString(R.string.codeNotFound), context, flag);
                break;
            case 429:
                CommonUtils.showAlert(context.getResources().getString(R.string.rateLimit), context, flag);
                break;
            case 408:
            case 504:
            default:
                CommonUtils.showAlert(context.getResources().getString(R.string.timeout), context, flag);
                break;
        }

    }
}

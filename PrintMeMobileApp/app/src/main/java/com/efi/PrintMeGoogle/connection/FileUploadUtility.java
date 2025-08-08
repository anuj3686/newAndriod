package com.efi.PrintMeGoogle.connection;

import android.util.Log;

import java.io.File;
import java.net.Proxy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadUtility implements IConnection {

    public String tlsVersion;
    public String requestData;
    public String serviceURL;
    public String contentType;
    public int connectionTimeOut;
    public int writeTimeOut;
    public int readTimeOut;
    public String RequestType;
    public HashMap<String, String> headers;
    public String queryParams;
    public HashMap<String, Object> uploadFiles;
    private static final String boundary = "*****";
    //private static final String LINE_FEED = "\r\n";
    private static final String TAG = FileUploadUtility.class.getSimpleName();

    public FileUploadUtility (HashMap params){
        //Log.i(TAG, "FileUploadUtility()");

        tlsVersion = params.get(Constants.TLS_VERSION) == null ? "TLSv1.2" : (String) params.get(Constants.TLS_VERSION);
        requestData = params.get(Constants.REQUEST) == null ? "" : (String) params.get(Constants.REQUEST);
        serviceURL = params.get(Constants.SERVICE_URL) == null ? "" : (String) params.get(Constants.SERVICE_URL);
        contentType = params.get(Constants.CONTENT_TYPE) == null ? "" : (String) params.get(Constants.CONTENT_TYPE);
        connectionTimeOut = params.get(Constants.CONNECTION_TIMEOUT) == null ? 0 : (Integer) params.get(Constants.CONNECTION_TIMEOUT);
        writeTimeOut = params.get(Constants.WRITE_TIMEOUT) == null ? 0 : (Integer) params.get(Constants.WRITE_TIMEOUT);
        readTimeOut = params.get(Constants.READ_TIMEOUT) == null ? 0 : (Integer) params.get(Constants.READ_TIMEOUT);
        RequestType = params.get(Constants.RequestType) == null ? "" : (String) params.get(Constants.RequestType);
        headers = params.get(Constants.REQUEST_HEADERS) == null ? null : (HashMap<String, String>) params.get(Constants.REQUEST_HEADERS);
        queryParams = params.get(Constants.QUERY_PARAMS) == null ? null : (String) params.get(Constants.QUERY_PARAMS);
        uploadFiles = params.get(Constants.UPLOAD_FILE) == null ? null : (HashMap<String, Object>) params.get(Constants.UPLOAD_FILE);
    }

    @Override
    public Object SendRequest() throws Exception {

        MediaType mediaType=null;
        if(contentType!=null || !contentType.isEmpty())
            mediaType = MediaType.parse(contentType);

        RequestBody body = null;
        if(requestData!=null)
            body = RequestBody.create(requestData.getBytes(),mediaType);
        Request request = getRequest(body);

        Response response = getNewHttpClient().newCall(request).execute();
        com.efi.PrintMeGoogle.connection.Response serviceResponse = new com.efi.PrintMeGoogle.connection.Response();
        serviceResponse.statusCode = response.code();
        serviceResponse.responseData = response.body().string();
        //Log.d(TAG,"Status Code: "+serviceResponse.statusCode);
        //Log.d(TAG,"Response: "+serviceResponse.responseData);
        return serviceResponse;
    }

    private OkHttpClient getNewHttpClient() {

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .cache(null)
                .connectTimeout(connectionTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .proxy(Proxy.NO_PROXY);

        return client.build();
    }

    private Request getRequest(RequestBody body)
    {
        Builder builder=new Builder();
        builder.url(serviceURL);

        if(uploadFiles!=null)
        {
            String fname = uploadFiles.get("fileName").toString();
            //String actualname = uploadFiles.get("actualFileName").toString();
            File fileUpload = new File(fname);
            String justName = fileUpload.getName();
            //Log.d(TAG,"File Upload Path: "+fileUpload.getAbsolutePath());
            //Log.d(TAG,"File Upload Name: "+fileUpload.getName());
            MediaType mediaType=MediaType.parse("application/octet-stream");
            MultipartBody.Builder Filebuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            Filebuilder.addFormDataPart("uploadFile", justName,RequestBody.create(fileUpload,mediaType));

            body=Filebuilder.build();
            builder.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        }

        if(RequestType!=null)
        {
            if(RequestType.equals(Constants.RequestType_GET))
            {
                builder.get();
            }
            else if(RequestType.equals(Constants.RequestType_PUT))
            {
                builder.put(body);
            }
            else if(RequestType.equals(Constants.RequestType_POST))
            {
//                Log.d(TAG,"Post method");
                builder.post(body);
            }
        }


        if(headers!=null)
        {
            Headers.Builder headerBuilder = new Headers.Builder();
            for(String headerKey:headers.keySet())
            {
                if(headerKey.equals("PrintMe-Original-Filename"))
                    headerBuilder.addUnsafeNonAscii(headerKey, headers.get(headerKey).replace("\n", ""));
                else
                    headerBuilder.add(headerKey, headers.get(headerKey).replace("\n", ""));

            }
            Headers headers = headerBuilder.build();
            builder.headers(headers);
        }
        return builder.build();

    }

}

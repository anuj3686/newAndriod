package com.efi.PrintMeGoogle.connection;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.efi.PrintMeGoogle.constants.GeneralConstants;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpsUrlConnection implements IConnection {

    private static String charset = "UTF-8";
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
    private static final String LINE_FEED = "\r\n";
    private String TAG="HttpsUrlConnection";


    public HttpsUrlConnection(HashMap<String, Object> params) {
        //Log.i(TAG, "Inside HttpsUrlconn");

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
        // TODO Auto-generated method stub
        HttpsURLConnection urlConnection = null;

        // Will contain the raw JSON response as a string.
        String server_response = "";
        try {
            URL url = new URL(serviceURL);

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(readTimeOut * 1000);
            urlConnection.setConnectTimeout(connectionTimeOut * 1000);
            try {
                setRequestProperty(urlConnection);
            } catch (UnknownHostException e) {
                throw e;
            } catch (ConnectException e) {
                throw e;
            } catch (SocketTimeoutException e) {
                throw e;
            } catch (Exception e) {
                //Log.i(TAG, "Excep" + e.getMessage());

                throw e;

            }
            //Log.i(TAG, "after setProperty");

            // Read the input stream into a String
            int responseCode = urlConnection.getResponseCode();
            //Log.i(TAG+" responseCode", "" + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                server_response = readStream(urlConnection.getInputStream());

                //Log.i(TAG+"response:", server_response);
            } else {
                server_response = readStream(urlConnection.getErrorStream());
                //Log.i(TAG+"response:", server_response);
            }
            Response serviceResponse = new Response();
            serviceResponse.statusCode = responseCode;
            serviceResponse.responseData = server_response;
            return serviceResponse;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            //Log.i(TAG+"Exc:", e.getMessage());

            throw e;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    private void setRequestProperty(HttpsURLConnection urlConnection) throws Exception {
        //Log.i("REG", "setRequestProperty");

        if (headers != null) {
            for (String headerKey : headers.keySet()) {
                urlConnection.setRequestProperty(headerKey, headers.get(headerKey));
            }
        }
        if (RequestType != null) {
            try {
                urlConnection.setRequestMethod(RequestType);
            } catch (ProtocolException e) {
                // TODO Auto-generated catch block
            }
            if (RequestType.equals(Constants.RequestType_GET)) {
                try {
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(false);
                    urlConnection.connect();
                } catch (Exception e) {
                    throw e;
                }
            } else if (RequestType.equals(Constants.RequestType_PUT) ||
                    RequestType.equals(Constants.RequestType_POST)) {
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                if (uploadFiles != null) {
                    urlConnection.setRequestProperty("Content-Type",
                            "multipart/form-data; boundary=" + boundary);
                }

                try {
                    OutputStream os = urlConnection.getOutputStream();
                    DataOutputStream request = new DataOutputStream(os);

                    if (!requestData.isEmpty()) {
                        request.writeBytes(requestData);
                        request.flush();
                        request.close();
                        os.close();
                    } else {
                        //Log.i("REG", "else");
                        if (uploadFiles != null) {
							/*if(queryParams != null)
							{
								addFormField(request);
								//addFormField(writer);
							}*/
                            addFilePart(request);
                            request.writeBytes(LINE_FEED);
                            request.flush();
                            request.writeBytes("--" + boundary + "--" + LINE_FEED);
//                            //Log.i("REG request", "" + );
                            request.close();

//			                addFilePart(writer, os);
//			                writer.append(LINE_FEED);
//			                writer.flush();
//			                writer.append("--" + boundary + "--" + LINE_FEED);
//			                writer.close();

                        } else if (queryParams != null) {
                            //String urlParameters = getPostDataString(queryParams);
                            byte[] urlParameters = queryParams.getBytes("UTF-8");
                            //Log.i("REG urlParameters", "" + urlParameters);
                            request.write(urlParameters);
                            request.flush();
                            request.close();
                        }
                        os.close();
                    }
                } catch (Exception e) {
                    //Log.i("Exce setRequestProperty", e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * Adds a form field to the request
     *
     * @throws IOException
     */
    public void addFormField(DataOutputStream writer) throws IOException {

        //for(String queryKey:queryParams.keySet())
        //{
        String filName = uploadFiles.get("actualFileName").toString();
        writer.writeBytes("--" + boundary + LINE_FEED);
        writer.writeBytes("Content-Disposition: form-data; name=\"" + filName + "\"" + LINE_FEED);
        writer.writeBytes("Content-Type: text/plain; charset=" + charset + LINE_FEED);
        writer.writeBytes(LINE_FEED);
        //writer.writeBytes(queryParams.get(queryKey) + LINE_FEED);
        writer.flush();
        //}

    }

    public void addFilePart(DataOutputStream writer)
            throws IOException {
        //for(String uploadFile:uploadFiles.keySet())
        //{
        String fname = uploadFiles.get("fileName").toString();
        String actualname = uploadFiles.get("actualFileName").toString();
        //if(myType instanceof File)
        //{
        //File fileUpload=(File)myType;
        //File dir = new File(fname);
        //deleteDir(dir);
        //if(!dir.exists())
        ////Log.i(GeneralConstants.AppLog, "File doesn't exist during upload");
        File fileUpload = new File(fname);
        //Log.i(GeneralConstants.AppLog, "file.getPath() " + fileUpload.getPath());
        //Log.i(GeneralConstants.AppLog, "Actual Name: " + actualname);
        //Log.i(GeneralConstants.AppLog, "content type " + URLConnection.guessContentTypeFromName(URLEncoder.encode(actualname, "UTF-8")));

        //File fileUpload = new File(Environment.getExternalStorageDirectory()+File.separator+"PrintMeTemp"+File.separator+fname);
        //String fileName = fileUpload.getName();
        String contentType = URLConnection.guessContentTypeFromName(URLEncoder.encode(fileUpload.getName(), "UTF-8"));
        //Log.i(GeneralConstants.AppLog, "filename: " + URLEncoder.encode(actualname, "UTF-8"));
        if (contentType == null) {
            //Log.i(GeneralConstants.AppLog, "contentType is null ");
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileUpload.getName());
            //Log.i(GeneralConstants.AppLog, "extension " + extension);
            if (extension != null) {
                contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                //Log.i(GeneralConstants.AppLog, "contentType " + contentType);
            }
            if (contentType == null) {
                //Log.i(GeneralConstants.AppLog, "contentType is null setting it to jpeg");
                contentType = "image/*"; // fallback type. You might set it to */*
            }
        }
        writer.writeBytes("--" + boundary + LINE_FEED);
        writer.writeBytes(
                "Content-Disposition: form-data; name=\"" + "uploadedfile1"
                        + "\"; filename=\"" + fname + "\"" + LINE_FEED);
        writer.writeBytes(
                "Content-Type: "
                        + contentType + LINE_FEED);
        writer.writeBytes(
                "Content-Description: "
                        + actualname + LINE_FEED);
        //writer.writeBytes("Content-Transfer-Encoding: binary"+LINE_FEED);
        writer.writeBytes(LINE_FEED);

        FileInputStream inputStream = new FileInputStream(fileUpload);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            writer.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        writer.writeBytes(LINE_FEED);
        //}
        //}
    }


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            //result.append(URLEncoder.encode(entry.getKey(), charset));
            //result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), charset));
        }
        return result.toString();
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return response.toString();
    }


}

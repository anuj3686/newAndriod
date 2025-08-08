package com.efi.PrintMeGoogle.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.share.ShareAsyncTask;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import static com.efi.PrintMeGoogle.constants.GeneralConstants.setUploading;


public class ShareActivity extends AppCompatActivity {
    long size = 0;
    ArrayList<File> LargeFile = new ArrayList<File>();
    Context context = this;
    ProgressBar progressBar;
    TextView bottomtext;
    TextView toptext;
    String type;
    final MimeTypeMap mime = MimeTypeMap.getSingleton();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadprogressdailog);
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            //Log.i("ShareActivity action", action);

            type = intent.getType();
            //Log..i("ShareActivity Type", intent.getType());
            setUploading(true);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            bottomtext = (TextView) findViewById(R.id.progreesBarBottomText);
            toptext = (TextView) findViewById(R.id.progreesBarTopText);
            if (Connectivity.isConnected(this)) {

                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    toptext.setText(this.getResources().getString(R.string.uploading));
                    bottomtext.setText(this.getResources().getString(R.string.please_wait));
                    handleSendFile(intent);
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                    handleSendMultipleFile(intent);

                } else {
                    CommonUtils.showAlert(getResources().getString(R.string.unableProcessDocument),this,true);
                }
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                bottomtext.setVisibility(View.INVISIBLE);
                toptext.setText(this.getResources().getString(R.string.noNetwork));
                CommonUtils.showAlert(this.getResources().getString(R.string.noNetwork), this, true);

            }
        } catch (Exception e) {
            //Log..i("ShareActivity onCreate", e.toString());
        }
    }

    void handleSendFile(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            ArrayList<Uri> imageUris = new ArrayList<>();
            imageUris.add(imageUri);
            handleSendRequest(imageUris);
        } else {
            CommonUtils.showAlert(this.getResources().getString(R.string.printErrorAndroid), this, true);

        }
    }

    void handleSendMultipleFile(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        int maxsize = 10;
        if (imageUris != null) {
            if (imageUris.size() <= maxsize) {
                if (imageUris.size() > 1) {
                    toptext.setText(this.getResources().getString(R.string.uploading_file, String.valueOf(imageUris.size())));
                    bottomtext.setText(this.getResources().getString(R.string.please_wait_while_uploaded));

                } else {
                    toptext.setText(this.getResources().getString(R.string.uploading));
                    bottomtext.setText(this.getResources().getString(R.string.please_wait));
                }
                handleSendRequest(imageUris);

            } else {
                progressBar.setVisibility(View.INVISIBLE);
                bottomtext.setVisibility(View.INVISIBLE);
                CommonUtils.showAlert(this.getResources().getString(R.string.number_of_files), this, true);
                //Log..i("REG", "Can't share more than 10 file");

            }
        } else {
            CommonUtils.showAlert(this.getResources().getString(R.string.printErrorAndroid), this, true);

        }

    }


    public void handleSendRequest(ArrayList imageUris) {
        ArrayList<String> actualFileName = getactualFileName(imageUris);
//        Log.i("REG Filename", actualFileName.toString());
        final ArrayList<String> files = getFile(imageUris);

        if (files != null && actualFileName != null) {
            int fileCount = imageUris.size();
            ShareAsyncTask shareAsyncTask = (ShareAsyncTask) new ShareAsyncTask(files, actualFileName, "", this, fileCount, new ShareAsyncTask.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    System.out.println(output);

                    progressBar.setVisibility(View.INVISIBLE);
                    bottomtext.setVisibility(View.INVISIBLE);
                    if (size <= 70) {
                        CommonUtils.showAlert(output, context, true);
                    } else {
                        CommonUtils.showAlert(context.getResources().getString(R.string.fileSizeError), context, true);
                        CommonUtils.deletefile(LargeFile.get(0));
                        toptext.setText(context.getResources().getString(R.string.fileSizeError));
                    }
                }
            }).execute();
        }
    }


    public ArrayList getactualFileName(ArrayList uris) {
        String result = null;
        long filesize = 0;
        ArrayList<String> FinalResult = new ArrayList<String>();
        int extIndex = -1;
        for (int i = 0; i < uris.size(); i++) {
            Uri uri = (Uri) uris.get(i);
            if (uri.getScheme().equals("content")) {
//                try {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        filesize = Integer.parseInt(String.valueOf(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) / (1024 * 1024)));
                    }
                } finally {
                    cursor.close();
                }
//                } catch (Exception e) {
//                    Log.i("Exception", e.toString());
//                }
            }
            if (result != null) {
                extIndex = result.lastIndexOf(".");

                if (extIndex == -1 || extIndex == 0)
                    result += ".pdf";
            } else if (result == null) {
                try {
                    result = fetchFileName(uri);
                } catch (Exception e) {
                    //Log.i("Exception", e.toString());
                }
                if (result == null) {
                    result = uri.getPath();
                    int cut = result.lastIndexOf('/');
                    if (cut != -1) {
                        result = result.substring(cut + 1);
                        int index = result.lastIndexOf('.');
                        if (index == -1) {
                            String mimeExtension = mime.getExtensionFromMimeType(type);
                            result = result + "." + mimeExtension;
                        }
                    }
                }
            }


            //Log.i("result", result);
            if (filesize <= 70) {
                FinalResult.add(result);
            }
        }
        //Log.i("ACTUAL", FinalResult.toString());
        return FinalResult;
    }

    private String fetchFileName(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        String filename = null;
//        try {
        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor mCursor = cr.query(uri, projection, null, null, null);
        if (mCursor != null) {
            try {
                if (mCursor.moveToFirst()) {
                    filename = mCursor.getString(0);
                    int cut = filename.lastIndexOf('/');
                    if (cut != -1) {
                        filename = filename.substring(cut + 1);
                    }
                }
            } finally {
                mCursor.close();
            }
        }
        //Log.i("filename", filename);
        return filename;
    }


    public String getFileName(Uri uri) {
        String result = null;
        int extIndex = -1;
        if (uri != null) {
            if (uri.getScheme().equals("content")) {
//                try {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        size = Integer.parseInt(String.valueOf(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) / (1024 * 1024)));
                    }
                } finally {
                    cursor.close();
                }
//                } catch (Exception e) {
//                    Log.i("Exception", e.toString());
//                }
            }
            if (result == null) {
                try {
                    result = fetchFileName(uri);
                } catch (Exception e) {
                    //Log.i("Exception", e.toString());
                }
                if (result == null) {
                    result = uri.getPath();
                    int cut = result.lastIndexOf('/');
                    if (cut != -1) {
                        result = result.substring(cut + 1);
                        int index = result.lastIndexOf('.');
                        if (index == -1) {
                            String mimeExtension = mime.getExtensionFromMimeType(type);
                            result = result + "." + mimeExtension;
                        }
                    }
                }
            }
        }


        //Log.i("getFileName", result);
        return result;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (requestCode == 1) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                    finishAffinity();
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            //Log.e("ShareActivity", e.toString());
        }
    }

    private ArrayList<String> getFile(ArrayList uris) {
        //Log.i(GeneralConstants.AppLog, "Inside getFile");
        ArrayList<String> ResultFile = new ArrayList<String>();
        if (uris != null) {
            for (int i = 0; i < uris.size(); i++) {
                Uri imgUri = (Uri) uris.get(i);
                //Log.e("ShareActivity", imgUri.toString());
                ParcelFileDescriptor inputPFD = null;
                try {
                    inputPFD = getContentResolver().openFileDescriptor(imgUri, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //Log.e("ShareActivity", e.toString());
                    if (e.getMessage().equals("Permission denied")) {
                        CommonUtils.checkFileReadPermission(context, ShareActivity.this);


                    }
                }

                File dir = new File(getExternalCacheDir(), "PrintMeTemp");
                if (!dir.exists())
                    dir.mkdir();
                //Log.i("REG", dir.getPath());
                Random rnd = new Random();
                int rndnum = rnd.nextInt(10000);
                String fname = getFileName(imgUri);
                if (fname != null) {
                    String fileName = rndnum + "-" + fname;
                    File file = new File(dir, fileName);
                    if (file.exists()) {
                        System.out.println("FILE EMPTY");
                    } else {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                        }
                    }
                    //Log.i(GeneralConstants.AppLog, "file.getPath() " + file.getPath());
                    try {
                        InputStream inputStream;
                        if (inputPFD != null) {

                            FileDescriptor fd = inputPFD.getFileDescriptor();
                            inputStream = new FileInputStream(fd);
                        } else {
//                            System.out.println("inputStream"+inputStream.available());
//                            if (inputStream == null) {
                            try {
                                inputStream = getContentResolver().openInputStream(imgUri);
                            } catch (IOException e) {
                                inputStream = new FileInputStream(imgUri.toString());
                            }
//                            }
                        }

                        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.getPath()));
                        final byte[] buffer = new byte[1024];
                        int countBytes;
                        while ((countBytes = inputStream.read(buffer)) != -1) {
//                            Log.i(GeneralConstants.AppLog, "buffer " + buffer);
                            outputStream.write(buffer, 0, countBytes);
                        }
                        outputStream.close();

                    } catch (final FileNotFoundException e) {

                    } catch (final IOException e) {
                        //Log.i("IOException", e.toString());
                    }
                    size = Integer.parseInt(String.valueOf(file.length() / (1024 * 1024)));
                    if (size <= 70) {
                        ResultFile.add(file.toString());
                    } else {
                        LargeFile.add(file);
                    }
                } else {
                    //Log.i("ShareActivity", "Unable to fetch files");
                }
            }
        }
        return ResultFile;
    }


}

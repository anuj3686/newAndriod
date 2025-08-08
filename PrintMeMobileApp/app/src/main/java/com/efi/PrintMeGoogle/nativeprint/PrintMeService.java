package com.efi.PrintMeGoogle.nativeprint;

import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.constants.GeneralConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.efi.PrintMeGoogle.constants.GeneralConstants.setUploading;

public class PrintMeService extends PrintService {

    @Override
    protected android.printservice.PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        //Log.i(GeneralConstants.AppLog, "Inside onCreatePrinterDiscoverySession");
        final android.printservice.PrinterDiscoverySession printerDiscoverySession = new PrinterDiscoverySession() {
            @Override
            public void onStartPrinterDiscovery(final List<PrinterId> priorityList) {
                //Log.i(GeneralConstants.AppLog, "Inside onStartPrinterDiscovery");
            }

            @Override
            public void onStopPrinterDiscovery() {
                //Log.i(GeneralConstants.AppLog, "Inside onStopPrinterDiscovery");
            }

            @Override
            public void onValidatePrinters(final List<PrinterId> printerIds) {
                //Log.i(GeneralConstants.AppLog, "Inside onValidatePrinters");
            }

            @Override
            public void onStartPrinterStateTracking(final PrinterId printerId) {
                //Log.i(GeneralConstants.AppLog, "Inside onStartPrinterStateTracking");
            }

            @Override
            public void onStopPrinterStateTracking(final PrinterId printerId) {
                //Log.i(GeneralConstants.AppLog, "Inside onStopPrinterStateTracking");
            }

            @Override
            public void onDestroy() {
                //Log.i(GeneralConstants.AppLog, "Inside onDestroy");
            }
        };
        final PrinterId printerId = generatePrinterId("123");

        final PrinterCapabilitiesInfo printerCapabilitiesInfo = new PrinterCapabilitiesInfo.Builder(printerId)
                .addMediaSize(PrintAttributes.MediaSize.NA_LETTER, true)
                .addMediaSize(PrintAttributes.MediaSize.NA_TABLOID, false)
                .addMediaSize(PrintAttributes.MediaSize.ISO_A4, false)
                .addMediaSize(PrintAttributes.MediaSize.NA_LEGAL, false)
                .addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                .addResolution(new PrintAttributes.Resolution("111", "asd", 300, 300), true)
                .setColorModes(PrintAttributes.COLOR_MODE_COLOR
                        | PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        final PrinterInfo printerInfo = new PrinterInfo.Builder(printerId, getString(R.string.send_to_print), PrinterInfo.STATUS_IDLE)
                .setCapabilities(printerCapabilitiesInfo).setDescription(getString(R.string.secure_printing)).build();

        final List<PrinterInfo> printerInfoList = new ArrayList<>();
        printerInfoList.add(printerInfo);

        printerDiscoverySession.addPrinters(printerInfoList);

        return printerDiscoverySession;
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        printJob.cancel();
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {
        try {
            //Log.i(GeneralConstants.AppLog, "Inside onPrintJobQueued");
            String actualFileName = getActualFileName(printJob);
            //Log.i(GeneralConstants.AppLog, "actualFileName: " + actualFileName);
            File file = getFile(printJob);
            Random rnd = new Random();
            int intentID = rnd.nextInt(10000);
            printJob.start();
            //Log.i(GeneralConstants.AppLog, "printJob started");
            ArrayList<String> actualFileNames = new ArrayList<String>();
            ArrayList<String> fileName = new ArrayList<String>();
            actualFileNames.add(actualFileName);
            fileName.add(file.toString());
            setUploading(true);
            long filesize = Integer.parseInt(String.valueOf(file.length() / (1024 * 1024)));
            new UploadAsyncTask(fileName, actualFileNames, "", filesize, getApplicationContext()).execute();
            printJob.complete();
            //Log.i(GeneralConstants.AppLog, "printJob completed");
        } catch (Exception e) {
            //Log.d("PrintService", "Exception onPrintJobQueued" + e.getMessage());

        }

    }

    private File getFile(final PrintJob printJob) {
        //Log.i(GeneralConstants.AppLog, "Inside getFile");
        File dir = new File(getExternalCacheDir(), "PrintMeTemp");
        //deleteDir(dir);
        if (!dir.exists())
            dir.mkdir();

        File file = new File(dir, getFileName(printJob) + ".pdf");
        //Log.i(GeneralConstants.AppLog, "file.getPath() " + file.getPath());
        try {
            final InputStream inputStream = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());

            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.getPath()));
            final byte[] buffer = new byte[1024];
            int countBytes = 0;
            while ((countBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, countBytes);
            }
            outputStream.close();
        } catch (final FileNotFoundException e) {

        } catch (final IOException e) {

        }
        //Log.d("Printmeservice: ", file.toString());

        return file;
    }

    private String getFileName(final PrintJob printJob) {
        //Log.i(GeneralConstants.AppLog, "Inside getFileName");
        String fileName = printJob.getDocument().getInfo().getName();
        if (fileName.equals("Unknown document name")) {
            fileName = printJob.getInfo().getLabel();
        }
        int extIndex = -1;
        if (fileName != null)
            extIndex = fileName.lastIndexOf(".");

        if (extIndex != -1 && extIndex != 0)
            fileName = fileName.substring(0, extIndex);

        Random rnd = new Random();
        int rndnum = rnd.nextInt(10000);
        fileName = rndnum + "-" + fileName;
        //Log.d("Printmeservice: ", fileName);

        return fileName;
    }


    private String getActualFileName(final PrintJob printJob) {
        //Log.i(GeneralConstants.AppLog, "Inside getActualFileName");
        String fileName = printJob.getDocument().getInfo().getName();
        if (fileName.equals("Unknown document name")) {
            fileName = printJob.getInfo().getLabel();
        }


        int extIndex = -1;
        if (fileName != null)
            extIndex = fileName.lastIndexOf(".");

        if (extIndex == -1 || extIndex == 0)
            fileName += ".pdf";
        return fileName;
    }





}

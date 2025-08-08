package com.efi.PrintMeGoogle.adapters;

public class ReleaseCodeListData {
    private String docId;
    private String files;
    private String upload_time;
    public ReleaseCodeListData(String doc_id, String noOfFile , String time) {
        docId = doc_id;
        files=noOfFile;
        upload_time=time;

    }

    public String getDocId() {
        return docId;
    }


    public String getFiles() {
        return files;
    }

    public String getUpload_time() {
             return upload_time;
    }


}

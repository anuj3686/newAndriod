package com.efi.PrintMeGoogle.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.adapters.DrnsRecyclerViewAdapter;
import com.efi.PrintMeGoogle.adapters.ReleaseCodeListData;
import com.efi.PrintMeGoogle.connection.Response;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.utils.ApiUtils;
import com.efi.PrintMeGoogle.utils.CommonUtils;
import com.efi.PrintMeGoogle.utils.Connectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ListOfDrnFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView list_of_drns_recyclerview;
    private SwipeRefreshLayout list_of_drn_swipe_layout;
    private View view;
    private String referenceHash[];
    private String TAG = "LIST";
    private DrnsRecyclerViewAdapter mAdapter;
    private LinearLayout mnoReleaseCodeLinearLayout;
    private LinearLayout recyclerViewLayout;
    private ArrayList<String> refHashArrayList = new ArrayList<>();
    private SharedPreferences emailHash;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list_of_drn, container, false);
        try {
            emailHash = view.getContext().getSharedPreferences(GeneralConstants.EMAIL_HASH_PREF, Context.MODE_PRIVATE);
            initComponent(view);
        }catch (Exception e){
            ApiUtils.handleError(404, view.getContext(), false);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getEmailWithHash();
    }

    private void initComponent(View view) {
        list_of_drn_swipe_layout = view.findViewById(R.id.list_of_drn_swipe_layout);
        list_of_drns_recyclerview = view.findViewById(R.id.list_of_drns_recyclerview);
        mnoReleaseCodeLinearLayout = view.findViewById(R.id.noReleaseCodeLayout);
        list_of_drn_swipe_layout.setOnRefreshListener(this);
        recyclerViewLayout = view.findViewById(R.id.recyclerViewLayout);
        list_of_drns_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));


    }


    @Override
    public void onRefresh() {
        try {
            if (Connectivity.isConnected(getContext())) {
                if (refHashArrayList != null && refHashArrayList.size() > 0)
                    new ListOfDrnsAsyncTask(view.getContext()).execute();
                else {
                    //Log.i(TAG, "RefrenceHash is null");
                    list_of_drn_swipe_layout.setRefreshing(false);

                }
            } else {
                CommonUtils.showAlert(getContext().getResources().getString(R.string.noNetwork), getContext(), false);
                list_of_drn_swipe_layout.setRefreshing(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getEmailWithHash() {
        try {
        if (emailHash != null) {
            String json;
            json = emailHash.getString(GeneralConstants.EMAIL_HASH, null);
            //Log.i(TAG, "JSOn data " + json);
            JSONArray jsonArray = null;

                if (json != null) {
                    jsonArray = new JSONArray(json);
                    //Log.d(TAG, "" + jsonArray);
                    if (jsonArray != null) {
                        String value = String.valueOf(jsonArray.length());
                        //Log.i(TAG, "emaailHash length " + value.length());
                        referenceHash = new String[value.length()];
                        //Log.i(TAG, "referenceHash length " + referenceHash);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jObject = null;
                            jObject = jsonArray.getJSONObject(i);
                            if (refHashArrayList != null && refHashArrayList.size() > 0) {
                                refHashArrayList.add(jObject.getString("referenceHash"));
                                //Log.d(TAG, "Multiple Hash " + refHashArrayList);
                            } else {
                                refHashArrayList.add(jObject.getString("referenceHash"));
                                //Log.d(TAG, "Single Hash " + refHashArrayList.toString());
                            }
                        }
                        if (Connectivity.isConnected(getContext())) {
                            new ListOfDrnsAsyncTask(view.getContext()).execute();
                        } else {
                            CommonUtils.showAlert(getContext().getResources().getString(R.string.noNetwork), getContext(), false);
                        }
                    } else {
                        ApiUtils.handleError(404, view.getContext(), false);
                    }
                } else {
                    ApiUtils.handleError(404, view.getContext(), false);
                }


        } else {
            ApiUtils.handleError(404, view.getContext(), false);
        }
        } catch (JSONException e) {
            ApiUtils.handleError(404, view.getContext(), false);
        }catch (Exception e){
            ApiUtils.handleError(404, view.getContext(), false);
        }
    }

    private class ListOfDrnsAsyncTask extends AsyncTask<Void, Void, Void> {
        Response response;
        Dialog dialog;
        Context context;
        private List<ReleaseCodeListData> releaseCodeList;
        ReleaseCodeListData releaseCodeListData;
        public ListOfDrnsAsyncTask(Context ctx) {

            context = ctx;
            releaseCodeList = new ArrayList<>();

        }

        @Override
        protected void onPreExecute() {
            //show loader
            dialog = CommonUtils.showProgressDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                CommonUtils.getUniqueId(context);
                response = ApiUtils.listOfDrnsApi(refHashArrayList);
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
                if (list_of_drn_swipe_layout.isRefreshing())
                    list_of_drn_swipe_layout.setRefreshing(false);
                if (response != null) {
                    if (response.statusCode == HttpURLConnection.HTTP_OK) {
                        String value = response.responseData;
                        //Log.d(TAG, value);

                        if (value == null || response.responseData.isEmpty() || response.responseData.equals("[]")) {
                            mnoReleaseCodeLinearLayout.setVisibility(View.VISIBLE);
                            recyclerViewLayout.setVisibility(View.GONE);
                        } else {
                            recyclerViewLayout.setVisibility(View.VISIBLE);
                            mnoReleaseCodeLinearLayout.setVisibility(View.GONE);
                            extractValues(value);
                        }
                        //Log.d("LIST Adapter", releaseCodeList.toString());
                        if (releaseCodeList == null || releaseCodeList.size() < 1) {
                            mnoReleaseCodeLinearLayout.setVisibility(View.VISIBLE);
                            recyclerViewLayout.setVisibility(View.GONE);
                        } else {
                            mAdapter = new DrnsRecyclerViewAdapter(releaseCodeList, context, list_of_drns_recyclerview);
                            list_of_drns_recyclerview.setAdapter(mAdapter);
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
            String docId = "";

            try {
                JSONArray jsonarray = new JSONArray(responseData);

                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    docId = jsonobject.getString("DocID");
                    //Log.d("LIST DocID", docId);

                    //splitting docID, so that we can meet UI
                    //total length of the string
                    int docIdLength = docId.length();
                    if (docIdLength >= 2) {
                        //get the max number of characters usable for first and second part
                        double singleElementLength = Math.ceil(docIdLength / 2);
                        //the first part of the string
                        String docId1 = docId.substring(0, (int) singleElementLength);
                        //the second part of the string
                        String docId2 = docId.substring(docId.length() - (int) singleElementLength, docId.length());
                        docId = docId1 + " " + docId2;
                    }
//                    else //Log.d(TAG, "Not valid DocId");
                    extractDocumentDetails(jsonobject, i, docId);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }




        }

        private void extractDocumentDetails(JSONObject jsonobject, int i, String docId) {
            String file = "";
            String filename = "";
            int lengthOfFile = 0;
            int status;
            JSONArray jsonArray = null;

            try {
                jsonArray = (JSONArray) jsonobject.get("Documents");
                //Log.d("LIST length ", "" + jsonArray.length());
                lengthOfFile = jsonArray.length();
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonObjectDoc = jsonArray.getJSONObject(j);
                    status = jsonObjectDoc.getInt("Status");
                    //Log.d("LIST status", "" + status);
                    if (status == 4) {
                        //Log.d(TAG, "Valid DRN");
                        filename = CommonUtils.getTrimmedFileName(jsonObjectDoc.getString("Name"));
                    } else {
                        //Log.d(TAG, "Invalid DRN");
                        lengthOfFile--;
                    }
                    if (lengthOfFile > 1) {
                        file = context.getResources().getString(R.string.files);
                        file = lengthOfFile + " " + file;
                    } else if (lengthOfFile == 1) {
                        file = filename;
//                                    CommonUtils.getTrimmedFileName(jsonObjectDoc.getString("Name"));

                    } else {
                        //Log.d(TAG, "Not valid file length");

                    }
                }
                extractOtherDetails(lengthOfFile, jsonArray, file, docId);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        private void extractOtherDetails(int lengthOfFile, JSONArray jsonArray, String file, String docId) {
            String dateTime = "";
            try {
                if (lengthOfFile > 0) {
                    try {
                        JSONObject jsonObjectDoc = jsonArray.getJSONObject(0);
                        dateTime = jsonObjectDoc.getString("Created");
                        //Log.d("LIST datetime", "" + dateTime);
                        //converting this server time to EST time
                        if (dateTime != null && dateTime != "") {
//                        dateTime = dateTime + '.000Z'; // ISO format
//                            Date frmtDate = new Date(dateTime);
//                            dateTime = frmtDate.toString();
                        }
                        //Log.d("LIST datetime :", "" + dateTime);
                        dateTime = context.getResources().getString(R.string.uploadedAt) + " : " + extractDateTime(dateTime);


                        ;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    releaseCodeListData = new ReleaseCodeListData(docId, file, dateTime);
                    releaseCodeList.add(releaseCodeListData);

                } else {
                    //Log.d(TAG, "Uncompleted Drn's");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String extractDateTime(String dateTime) {
            String dateTimeZone = dateTime + ".000Z";
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
            String formatted_upload_time = output.format(date);
            //Log.i("DATE", "" + formatted_upload_time + dateTime);
            return formatted_upload_time;
        }


    }
}

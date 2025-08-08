package com.efi.PrintMeGoogle.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.RegistrationActivity;
import com.efi.PrintMeGoogle.adapters.SettingsRecyclerViewAdapter;
import com.efi.PrintMeGoogle.constants.GeneralConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p>
 * to handle interaction events.
 * <p>
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private View view;
    private String TAG = "SETTINGS";
    private LinearLayout terms,privacy,help;
    private TextView addEmailTextView;
    private ArrayList<String> emailList;
    private RecyclerView emailListRecyclerView;
    private SettingsRecyclerViewAdapter settingsRecyclerViewAdapter;
    private SwitchCompat email_response_switch,notify_switch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        initComponent();
        setSwitchButtonStatus();
        getEmailWithHash();
        return view;
    }

    private void setSwitchButtonStatus() {
        try {
            //email_response_sharedPref
            SharedPreferences email_response_status = getActivity().getSharedPreferences(GeneralConstants.EMAIL_RESPONSE_STATUS_PREF, Context.MODE_PRIVATE);
            String responseVal=email_response_status.getString(GeneralConstants.EMAIL_RESPONSE_STATUS,null);
            if(responseVal == null || responseVal.isEmpty()) email_response_switch.setChecked(true);
            else email_response_switch.setChecked(Boolean.parseBoolean(email_response_status.getString(GeneralConstants.EMAIL_RESPONSE_STATUS, null)));
            //notify_sharedPref
            SharedPreferences notify_status = getContext().getSharedPreferences(GeneralConstants.NOTIFY_STATUS_PREF, Context.MODE_PRIVATE);
            String notifyVal = notify_status.getString(GeneralConstants.NOTIFY_STATUS,null);
            if(notifyVal == null || notifyVal.isEmpty()) notify_switch.setChecked(true);
            else notify_switch.setChecked(Boolean.parseBoolean(notify_status.getString(GeneralConstants.NOTIFY_STATUS, null)));
        }catch (Exception e){
//            Log.d(TAG,e.getMessage());
        }

    }

    private void getEmailWithHash() {
        try {
            SharedPreferences emailHash = getContext().getSharedPreferences(GeneralConstants.EMAIL_HASH_PREF, Context.MODE_PRIVATE);
            emailList = new ArrayList<>();
            if (emailHash != null) {
                String json;
                json = emailHash.getString(GeneralConstants.EMAIL_HASH, null);
                JSONArray jsonArray = null;
                try {
                    if (json != null) {
                        jsonArray = new JSONArray(json);
                        //Log.d(TAG, jsonArray.toString());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject jObject = null;
                                    jObject = jsonArray.getJSONObject(i);
                                    String existing_email = jObject.getString("email");
                                    //Log.d("Existing Email>>>>", existing_email);
                                    if (existing_email != null && existing_email != "")
                                        emailList.add(existing_email);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            settingsRecyclerViewAdapter = new SettingsRecyclerViewAdapter(emailList, getContext());
                            emailListRecyclerView.setAdapter(settingsRecyclerViewAdapter);

                        }
                    }

                } catch (JSONException e) {
                    //Log.e(TAG, e.getMessage());

                }

            }
        }catch (Exception e){
            //Log.e(TAG,e.getMessage());
        }
    }


    private void initComponent() {
        try {
            terms = view.findViewById(R.id.termsLayout);
            privacy = view.findViewById(R.id.privacyLayout);
            help = view.findViewById(R.id.helpLayout);
            addEmailTextView = view.findViewById(R.id.addEmailTextView);
            emailListRecyclerView = view.findViewById(R.id.emailListRecyclerView);
            email_response_switch = view.findViewById(R.id.email_response_switch);
            notify_switch = view.findViewById(R.id.notify_switch);
            emailListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            terms.setOnClickListener(this);
            privacy.setOnClickListener(this);
            help.setOnClickListener(this);
            addEmailTextView.setPaintFlags(addEmailTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            addEmailTextView.setText(getResources().getString(R.string.addEmail));
            addEmailTextView.setOnClickListener(this);
            email_response_switch.setOnCheckedChangeListener(this);
            notify_switch.setOnCheckedChangeListener(this);
        }catch (Exception e){
            //Log.d(TAG,"Excep initComponent"+e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        Fragment fragment=new UrlFragment();

        switch (v.getId()) {
            case R.id.addEmailTextView:
                //Log.d(TAG,"addEmail");
                Intent intent = new Intent(getContext(), RegistrationActivity.class);
                intent.putExtra("addEmail", true);
                startActivity(intent);
                break;
            case R.id.termsLayout:
                //Log.d(TAG, "terms");
                loadFragment(fragment, GeneralConstants.Terms);
                break;
            case R.id.privacyLayout:
                //Log.d(TAG, "privacy");
                loadFragment(fragment, GeneralConstants.Privacy);
                break;
            case R.id.helpLayout:
            default:
                //Log.d(TAG, "help");
                loadFragment(fragment, GeneralConstants.Help);
                break;

        }

    }

    private void loadFragment(Fragment fragment,String name) {

        // load fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(GeneralConstants.PageName, name);
        fragment.setArguments(args);
       //Inflate the fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.email_response_switch:
                //Log.i("email_response_switch", isChecked + "");
                //save state
                SharedPreferences email_response_status = getContext().getSharedPreferences(GeneralConstants.EMAIL_RESPONSE_STATUS_PREF, Context.MODE_PRIVATE);
                email_response_status.edit().putString(GeneralConstants.EMAIL_RESPONSE_STATUS, String.valueOf(isChecked)).commit();
                break;
            case R.id.notify_switch:
                //Log.i("notify_switch", isChecked + "");
                //save state
                SharedPreferences notify_status = getContext().getSharedPreferences(GeneralConstants.NOTIFY_STATUS_PREF, Context.MODE_PRIVATE);
                notify_status.edit().putString(GeneralConstants.NOTIFY_STATUS,String.valueOf(isChecked)).commit();
                break;
        }
    }
}

package com.efi.PrintMeGoogle.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.utils.ApiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsItemViewHolder> {
    ArrayList<String> OwnerEmailList=new ArrayList<>();
    private Context mContext;
    Bundle bundle = new Bundle();
    private String TAG="SETTINGS";

    public SettingsRecyclerViewAdapter(ArrayList<String> ownerEmailList, Context context) {
        OwnerEmailList = ownerEmailList;
        mContext = context;
        //Log.d(TAG, OwnerEmailList.toString());
    }

    @Override
    public SettingsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_recycler_view_list_item, parent, false);
        return new SettingsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SettingsItemViewHolder holder, final int position) {
        try {
            if (OwnerEmailList.size() > 1) {
                holder.delete_icon_layout.setEnabled(true);  //layout instead of imageview, since delete icon was small .So to increase the clickable surroundings area
                holder.delete_icon_imageView.setImageResource(R.drawable.ic_mobileapp_delete_icon_enabled);
            }
            else{
                holder.delete_icon_layout.setEnabled(false);
                holder.delete_icon_imageView.setImageResource(R.drawable.ic_mobileapp_delete_icon_disabled);
            }
            holder.emailAddress_textview.setText(OwnerEmailList.get(position));
            holder.delete_icon_layout.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onClick(View v) {
                    //Log.d("delete layout", "clicked");
                    deleteEmailAddress(v, position);
                    //refresh the list
                    refreshEmailList(position);

                }
            });
        }catch (Exception e){
            //Log.d(TAG,e.getMessage());
        }



    }

    private void refreshEmailList(int position) {
        OwnerEmailList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, OwnerEmailList.size());
        notifyDataSetChanged();

    }

    private void deleteEmailAddress(View v, int position) {

            //delete from list as well as sharedPref too
            SharedPreferences emailHash = v.getContext().getSharedPreferences(GeneralConstants.EMAIL_HASH_PREF, Context.MODE_PRIVATE);
            if (emailHash != null) {
                String json;
                json = emailHash.getString(GeneralConstants.EMAIL_HASH, null);
                JSONArray jsonArray = null;
                try {
                    if (json != null) {
                        jsonArray = new JSONArray(json);
                        //Log.d(TAG, "" + jsonArray);
                        if (jsonArray != null) {
                            //Log.d(TAG,"ownerList pos"+OwnerEmailList.get(position));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = null;
                                jObject = jsonArray.getJSONObject(i);
                                //Log.d(TAG, "Before deletion length:" + jsonArray.length());
                                //Log.d(TAG, "Before deletion count:" + jObject.getString("email_count"));
                                //Log.d(TAG,"json_email "+jObject.getString("email"));
                                if (jObject.getString("email").equalsIgnoreCase(OwnerEmailList.get(position))) {
                                    //before deletion check the counter for OwnerEmail
                                    int count = Integer.parseInt(jObject.getString("email_count"));

                                    //get OwnerEmail
                                    SharedPreferences OwnerEmail = v.getContext().getSharedPreferences(GeneralConstants.OWNER_EMAIL_PREF, Context.MODE_PRIVATE);
                                    //Log.d(TAG,"owner_email "+OwnerEmail.getString(GeneralConstants.OWNER_EMAIL, null));

                                    if (OwnerEmail.getString(GeneralConstants.OWNER_EMAIL, null).equalsIgnoreCase(OwnerEmailList.get(position))) {
                                        count++; //so that will make next email as OwnerEmail(for native upload parameter)
                                        jObject = jsonArray.getJSONObject(count);
                                        jObject.put("email_count",0 );

                                        //Log.d(TAG, "making next emailId as OwnerEmail>>" + jObject.getString("email"));
                                        OwnerEmail.edit().putString(GeneralConstants.OWNER_EMAIL, jObject.getString("email")).commit();
                                        deleteAndUpdateEmailList(jsonArray,i,emailHash);

                                    } else deleteAndUpdateEmailList(jsonArray, i, emailHash);
                                    break;
                                }
                            }
                        } else {
                            ApiUtils.handleError(504, v.getContext(),false);
                        }
                    } else {
                        ApiUtils.handleError(504, v.getContext(),false);
                    }
                } catch (JSONException e) {
                    //Log.e(TAG, e.getMessage());
                }catch (Exception e){
                    //Log.e(TAG,e.getMessage());
                }

            }
        }
    private void deleteAndUpdateEmailList(JSONArray jsonArray, int i, SharedPreferences emailHash) {
        jsonArray.remove(i);
        emailHash.edit().putString(GeneralConstants.EMAIL_HASH, jsonArray.toString()).commit();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return OwnerEmailList.size();
    }


}




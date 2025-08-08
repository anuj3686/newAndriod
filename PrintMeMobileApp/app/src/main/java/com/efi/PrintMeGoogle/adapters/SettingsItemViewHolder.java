package com.efi.PrintMeGoogle.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.efi.PrintMeGoogle.R;

public class SettingsItemViewHolder extends RecyclerView.ViewHolder {
    public TextView emailAddress_textview;
    public ImageView delete_icon_imageView;
    public LinearLayout delete_icon_layout;

    public SettingsItemViewHolder(View itemView) {
        super(itemView);
        emailAddress_textview = itemView.findViewById(R.id.email_TextView);
        delete_icon_imageView=itemView.findViewById(R.id.delete_icon_imageView);
        delete_icon_layout=itemView.findViewById(R.id.delete_icon_layout);
    }

}

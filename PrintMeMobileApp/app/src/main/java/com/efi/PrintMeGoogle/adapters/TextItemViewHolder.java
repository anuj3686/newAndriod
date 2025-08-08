package com.efi.PrintMeGoogle.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.efi.PrintMeGoogle.R;

public class TextItemViewHolder extends RecyclerView.ViewHolder {
    public TextView doc_id_textview,doc_name_textview,uploaded_time_textview;
    public ImageView forward_icon_imageView, file_type_icon;

    public TextItemViewHolder(View itemView) {
        super(itemView);
        doc_id_textview = itemView.findViewById(R.id.doc_id_textview);
        doc_name_textview=itemView.findViewById(R.id.doc_name_or_number_textview);
        uploaded_time_textview=itemView.findViewById(R.id.uploaded_time_textview);
        forward_icon_imageView=itemView.findViewById(R.id.forward_icon_imageView);
        file_type_icon = itemView.findViewById(R.id.file_type_icon);
    }

}

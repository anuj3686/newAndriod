package com.efi.PrintMeGoogle.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.activities.BarcodeActivity;
import com.efi.PrintMeGoogle.utils.CommonUtils;

import java.util.List;


public class DrnsRecyclerViewAdapter extends RecyclerView.Adapter<TextItemViewHolder> {
    List<ReleaseCodeListData> releaseCodeList;
    private RecyclerView mRecyclerView;
    private Context mContext;


    public DrnsRecyclerViewAdapter(List<ReleaseCodeListData> releaseCodeListDetail, Context context, RecyclerView recyclerView) {
        releaseCodeList = releaseCodeListDetail;
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        //Log.d("LIST releaseCodeList ", releaseCodeList.toString());
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                //Log.d("LIST", "icon clicked");
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                final ReleaseCodeListData myListData = releaseCodeList.get(itemPosition);
                String docId = myListData.getDocId();
                Intent intent = new Intent(mContext, BarcodeActivity.class);
                intent.putExtra("docId", docId);
                mContext.startActivity(intent);
            } catch (Exception e) {
                //Log.d("onClick", e.toString());

            }
        }
    };

    @Override
    public TextItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drns_recycler_view_list_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new TextItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextItemViewHolder holder, int position) {
        final ReleaseCodeListData myListData = releaseCodeList.get(position);
        String fileExtension = CommonUtils.getFileExtension(myListData.getFiles());
        int image;
        if (fileExtension.isEmpty()) {
            image = R.drawable.ic_files;
        } else {
            image = getFileTypeThumbnail(fileExtension);
        }
        holder.file_type_icon.setImageResource(image);
        holder.doc_id_textview.setText(myListData.getDocId());
        holder.doc_name_textview.setText(myListData.getFiles());
        holder.uploaded_time_textview.setText(myListData.getUpload_time());

    }

    int getFileTypeThumbnail(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                return R.drawable.ic_pdf;
            case "txt":
                return R.drawable.ic_txt;
            case "png":
                return R.drawable.ic_png;
            case "jpg":
            case "jpeg":
            case "bmp":
            case "jpe":
            case "gif":
            case "tiff":
            case "tif":
            case "image":
            case "jfif":
                return R.drawable.ic_img;
            case "docx":
            case "doc":
                return R.drawable.ic_doc;
            case "ppt":
            case "pptx":
                return R.drawable.ic_ppt;
            case "xls":
            case "xlsx":
                return R.drawable.ic_xls;
            default:
                return R.drawable.ic_other_file;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return releaseCodeList.size();
    }

}


package com.efi.PrintMeGoogle.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import com.efi.PrintMeGoogle.R;
import com.efi.PrintMeGoogle.constants.GeneralConstants;
import com.efi.PrintMeGoogle.utils.CommonUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p>
 * to handle interaction events.
 * <p>
 * create an instance of this fragment.
 */
public class UrlFragment extends Fragment {
    private Dialog dialog;
    private String TAG = "UrlFragment";
    private String url="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View RootView = inflater.inflate(R.layout.fragment_map, container, false);
        try {
        //Retrieve the value
        String value = getArguments().getString(GeneralConstants.PageName);
        //Log..d(TAG,value);
        WebView myWebView = RootView.findViewById((R.id.webview));

            myWebView.setInitialScale(1);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            myWebView.getSettings().setJavaScriptEnabled(true);
            myWebView.getSettings().setBuiltInZoomControls(true);
            myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            myWebView.setScrollbarFadingEnabled(true);

            if (value != null && !value.isEmpty()) {
                dialog = CommonUtils.showProgressDialog(getContext());
                dialog.show();
                switch (value) {
                    case GeneralConstants.Terms:
                        url = GeneralConstants.TermsOfUseUrl;
                        break;
                    case GeneralConstants.Privacy:
                        url = GeneralConstants.PrivacyPolicyUrl;
                        break;
                    case GeneralConstants.Help:
                    default:
                        url = GeneralConstants.HelpUrl;
                        break;
                }

                myWebView.loadUrl(url);

                // Force links and redirects to open in the WebView instead of in a browser
                myWebView.setWebViewClient(new WebViewClient() {

                    public void onPageFinished(WebView view, String url) {
                        CommonUtils.dismissProgressDialog(dialog);
                    }
                });
            }
        }catch (Exception e){
            //Log..d(TAG,"Exception :"+e.getMessage());
        }
        return RootView;
    }
}

package com.inputstick.apps.kp2aplugin.slides;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.inputstick.apps.kp2aplugin.PluginHelper;
import com.inputstick.apps.kp2aplugin.R;

public class DownloadSlide extends Fragment {
	
	private Button buttonDownload;
	private boolean isInstalled;
	
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ARG_TARGET_PACKAGE = "targetPackage";

    public static DownloadSlide newInstance(int layoutResId, String targetPackage) {
    	DownloadSlide slide = new DownloadSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putString(ARG_TARGET_PACKAGE, targetPackage);
        slide.setArguments(args);

        return slide;
    }

    private int layoutResId;
    private String targetPackage;

    public DownloadSlide() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if((getArguments() != null) && (getArguments().containsKey(ARG_LAYOUT_RES_ID))) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
        if((getArguments() != null) && (getArguments().containsKey(ARG_TARGET_PACKAGE))) {
        	targetPackage = getArguments().getString(ARG_TARGET_PACKAGE);
        }        

    }

    @Nullable
    @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(layoutResId, container, false);
		buttonDownload = (Button) view.findViewById(R.id.buttonDownload);
		buttonDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !isInstalled) {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + targetPackage)));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + targetPackage)));
					}
				}
			}
		});
		return view;		
	}

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }	
    
    @Override
    public void  onResume() {
    	super.onResume();
    	if (PluginHelper.isPackageInstalled(getActivity(), targetPackage)) {
    		buttonDownload.setText(R.string.slide_button_done);
    		buttonDownload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
    		isInstalled = true;
    	} else {    	
    		buttonDownload.setText(R.string.slide_button_download);
    		buttonDownload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_download, 0);
    		isInstalled = false;    		
    	}
    }

}
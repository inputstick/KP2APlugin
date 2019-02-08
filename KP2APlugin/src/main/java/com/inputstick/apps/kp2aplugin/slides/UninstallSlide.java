package com.inputstick.apps.kp2aplugin.slides;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.inputstick.apps.kp2aplugin.Const;
import com.inputstick.apps.kp2aplugin.PluginHelper;
import com.inputstick.apps.kp2aplugin.R;

public class UninstallSlide extends Fragment {
	
	private Button buttonUninstall;
	private boolean isInstalled;

    public UninstallSlide() {    	
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }    

    @Nullable
    @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.slide_uninstall, container, false);
		buttonUninstall = (Button) view.findViewById(R.id.buttonUninstall);
		buttonUninstall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isInstalled) {
					startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:" + Const.PACKAGE_PLUGIN_OLD)));
				}
			}
		});
		return view;
	}

    @Override
    public void  onResume() {
    	super.onResume();
    	if (PluginHelper.isPackageInstalled(getActivity(), Const.PACKAGE_PLUGIN_OLD)) {
    		buttonUninstall.setText(R.string.slide_button_uninstall);
    		buttonUninstall.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_uninstall, 0);
    		isInstalled = true;
    	} else {
    		buttonUninstall.setText(R.string.slide_button_done);
    		buttonUninstall.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
    		isInstalled = false;
    	}
    }

}

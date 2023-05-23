package com.inputstick.apps.kp2aplugin.slides;

import keepass2android.pluginsdk.Strings;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.inputstick.apps.kp2aplugin.PluginHelper;
import com.inputstick.apps.kp2aplugin.PreferencesHelper;
import com.inputstick.apps.kp2aplugin.R;

public class EnableSlide extends Fragment {
	
	private static final long TIMEOUT = 5000;
	
	private Button buttonEnable;
	private boolean isEnabled;
	private long lastAttempt;

    public EnableSlide() {    	
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }    

    @Nullable
    @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.slide_enable, container, false);
		buttonEnable = (Button) view.findViewById(R.id.buttonEnable);
		buttonEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				if ( !isEnabled) {
					try {
						long now = System.currentTimeMillis();
						if (now > lastAttempt + TIMEOUT) {
							lastAttempt = now;
							Intent i = new Intent( Strings.ACTION_EDIT_PLUGIN_SETTINGS);
							i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, getActivity().getPackageName());
							//on some devices activity didn't start correctly with default flags, if kp2a was just installed
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(i);
						} else {
							long sec = ((lastAttempt + TIMEOUT - now) / 1000) + 1;
							String msg = getActivity().getString(R.string.slide_text_wait1) + String.valueOf(sec) + getActivity().getString(R.string.slide_text_wait2);   
							Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();				
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		return view;
	}

    @Override
    public void  onResume() {
    	super.onResume();
    	if (PluginHelper.isPluginEnabled(getActivity())) {
    		buttonEnable.setText(R.string.slide_button_done);
    		buttonEnable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);    		
    		isEnabled = true; 
    		PreferencesHelper.setSetupCompleted(PreferenceManager.getDefaultSharedPreferences(getActivity()));
    	} else {
    		buttonEnable.setText(R.string.slide_button_enable);
    		buttonEnable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_enable, 0);
    		isEnabled = false;
    	}
    }

}
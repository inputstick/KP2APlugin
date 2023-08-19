package com.inputstick.apps.kp2aplugin;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.github.paolorotolo.appintro.AppIntro2;
import com.inputstick.apps.kp2aplugin.slides.AlertWindowSlide;
import com.inputstick.apps.kp2aplugin.slides.DownloadSlide;
import com.inputstick.apps.kp2aplugin.slides.EnableSlide;
import com.inputstick.apps.kp2aplugin.slides.InfoSlide;
import com.inputstick.apps.kp2aplugin.slides.LayoutSlide;
import com.inputstick.apps.kp2aplugin.slides.NotificationsSlide;
import com.inputstick.apps.kp2aplugin.slides.UninstallSlide;

public class SetupWizardActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(InfoSlide.newInstance(R.layout.slide_intro));
        if (( !PluginHelper.isPackageInstalled(this, Const.PACKAGE_KP2A)) && ( !PluginHelper.isPackageInstalled(this, Const.PACKAGE_KP2A_NO_NET)))  {
        	addSlide(DownloadSlide.newInstance(R.layout.slide_download_kp2a, Const.PACKAGE_KP2A));
        }        
        if ( !PluginHelper.isPackageInstalled(this, Const.PACKAGE_UTILITY)) {
        	addSlide(InfoSlide.newInstance(R.layout.slide_hardware));
        	addSlide(DownloadSlide.newInstance(R.layout.slide_download_utility, Const.PACKAGE_UTILITY));        	
        }
        if (PluginHelper.isPackageInstalled(this, Const.PACKAGE_PLUGIN_OLD)) {
        	addSlide(new UninstallSlide());        
        }
        if ( !PluginHelper.isPluginEnabled(this)) {
        	addSlide(new EnableSlide());	
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if ( !notificationManager.areNotificationsEnabled()) {
                addSlide(new NotificationsSlide());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if ( !Settings.canDrawOverlays(this)) {
                addSlide(new AlertWindowSlide());
            }
        }
                
        addSlide(new LayoutSlide());
        addSlide(InfoSlide.newInstance(R.layout.slide_field)); 
        addSlide(InfoSlide.newInstance(R.layout.slide_entry));
        addSlide(InfoSlide.newInstance(R.layout.slide_settings));
        addSlide(InfoSlide.newInstance(R.layout.slide_mac)); 
        addSlide(InfoSlide.newInstance(R.layout.slide_done));                         
    }
    
    @Override
    public void onDestroy() {
    	disconnect();
    	super.onDestroy();
    }
    
    private void disconnect() {
    	if (InputStickService.isRunning) {
    		ActionHelper.forceStopService(this);
    	}    	
    }
    

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
    	PreferencesHelper.setSetupCompleted(PreferenceManager.getDefaultSharedPreferences(this));
    	finish();
    }

    @Override
    public void onSlideChanged() {
    	disconnect();
    }
    
    @Override
    public void onBackPressed() {
    	showSkipSetupDialog();
    }
    
    private void showSkipSetupDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);				
		alert.setTitle(R.string.setup_skip_title);
		alert.setMessage(R.string.setup_skip_text);
		alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				PreferencesHelper.setSetupCompleted(PreferenceManager.getDefaultSharedPreferences(SetupWizardActivity.this));
				disconnect();
				finish();
			}
		});
		alert.setNegativeButton(R.string.no, null);				
		alert.show();    	
    }
    
}

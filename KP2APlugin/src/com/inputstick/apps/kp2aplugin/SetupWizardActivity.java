package com.inputstick.apps.kp2aplugin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.inputstick.apps.kp2aplugin.slides.DownloadSlide;
import com.inputstick.apps.kp2aplugin.slides.EnableSlide;
import com.inputstick.apps.kp2aplugin.slides.InfoSlide;
import com.inputstick.apps.kp2aplugin.slides.LayoutSlide;
import com.inputstick.apps.kp2aplugin.slides.SlidesUtils;
import com.inputstick.apps.kp2aplugin.slides.UninstallSlide;

public class SetupWizardActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
    	SlidesUtils.init(this);
        addSlide(InfoSlide.newInstance(R.layout.slide_intro));
                
        if (( !SlidesUtils.isPackageInstalled(Const.PACKAGE_KP2A)) && ( !SlidesUtils.isPackageInstalled(Const.PACKAGE_KP2A_NO_NET)))  {
        	addSlide(DownloadSlide.newInstance(R.layout.slide_download_kp2a, Const.PACKAGE_KP2A));
        }        
        if ( !SlidesUtils.isPackageInstalled(Const.PACKAGE_UTILITY)) {
        	addSlide(InfoSlide.newInstance(R.layout.slide_hardware));
        	addSlide(DownloadSlide.newInstance(R.layout.slide_download_utility, Const.PACKAGE_UTILITY));        	
        }
        if (SlidesUtils.isPackageInstalled(Const.PACKAGE_PLUGIN_OLD)) {
        	addSlide(new UninstallSlide());        
        }
        if ( !SlidesUtils.isEnabled()) {
        	addSlide(new EnableSlide());	
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
    
    
    private static boolean shouldDisconnect = false;
    
    public static void shouldDisconnect() {    	   	    	
    	shouldDisconnect = true;
    }
    
    private void disconnect() {
    	if (shouldDisconnect) {
        	Intent serviceIntent = new Intent(this, InputStickService.class);
        	serviceIntent.setAction(Const.SERVICE_DISCONNECT);
        	startService(serviceIntent);    		
    		shouldDisconnect = false;
    	}    	
    }
    

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
    	SlidesUtils.setAsCompleted();
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
				SlidesUtils.setAsCompleted();
				disconnect();
				finish();
			}
		});
		alert.setNegativeButton(R.string.no, null);				
		alert.show();    	
    }
    
}

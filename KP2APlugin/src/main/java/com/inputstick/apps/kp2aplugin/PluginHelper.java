package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.AccessManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PluginHelper {

	public static boolean isPluginEnabled(Context ctx) {
		return !AccessManager.getAllHostPackages(ctx).isEmpty();
	}
	
	public static boolean isPackageInstalled(Context ctx, String packageToCheck) {
		PackageManager pm = ctx.getPackageManager();
	    try {
	        pm.getPackageInfo(packageToCheck, PackageManager.GET_ACTIVITIES);
	        return true;			        
	    } catch (NameNotFoundException e) {			
	    	return false;
	    }
	}	

}

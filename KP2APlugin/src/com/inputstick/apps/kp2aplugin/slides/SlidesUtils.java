package com.inputstick.apps.kp2aplugin.slides;

import keepass2android.pluginsdk.AccessManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class SlidesUtils {	
	
	private static Context ctx;
	private static SharedPreferences prefs;
	
	public static void init(Context ctx) {
		SlidesUtils.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);		
	}
	
	public static boolean isEnabled() {
		return !AccessManager.getAllHostPackages(ctx).isEmpty();
	}
	
	public static boolean isPackageInstalled(String packageToCheck) {
		PackageManager pm = ctx.getPackageManager();
	    try {
	        pm.getPackageInfo(packageToCheck, PackageManager.GET_ACTIVITIES);
	        return true;			        
	    } catch (NameNotFoundException e) {			
	    	return false;
	    }
	}
	
	public static String getLayout() {
		return prefs.getString("kbd_layout", "en-US");
	}
	
	public static void setLayout(String value) {
		//Toast.makeText(ctx, "SET TO: " + value, Toast.LENGTH_SHORT).show();
		prefs.edit().putString("kbd_layout", value).commit();
	}
	
	
	public static void setAsCompleted() {
		//Toast.makeText(ctx, "SETUP COMPLETED:" + id, Toast.LENGTH_SHORT).show(); 
		prefs.edit().putBoolean("setup_completed", true).commit();
	}
	

}

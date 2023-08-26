package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.AccessManager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings;

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

	public static int checkPluginConfig(Context ctx) {
		if (AccessManager.getAllHostPackages(ctx).isEmpty()) {
			return Const.CONFIG_PLUGIN_NOT_ENABLED;
		} else {
			//higher priority:
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
				if ( !notificationManager.areNotificationsEnabled()) {
					return Const.CONFIG_PERMISSION_NOTIFICATIONS;
				}
			}
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				if ( !Settings.canDrawOverlays(ctx)) {
					return Const.CONFIG_PERMISSION_ALERT_WINDOW;
				}
			}
		}
		return Const.CONFIG_OK;
	}

}

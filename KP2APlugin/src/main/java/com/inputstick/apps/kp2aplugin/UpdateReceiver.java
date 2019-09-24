package com.inputstick.apps.kp2aplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            /*if ("1.13".equals(versionName)) {
            ...
            }*/
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
	}

}

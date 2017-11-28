package com.inputstick.apps.kp2aplugin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            if ("1.13".equals(versionName)) {
        		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        		mBuilder.setContentTitle(context.getString(R.string.app_name));
        		mBuilder.setContentText("If the plugin no longer works (how to fix)");
        		mBuilder.setSmallIcon(R.drawable.ic_notification);

        		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        		resultIntent.setData(Uri.parse("http://blog.inputstick.com/2017/11/if-your-kp2a-plugin-no-longer-works-fix.html"));
        		PendingIntent pending = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);        		
        		mBuilder.setContentIntent(pending);

        		mNotificationManager.notify(Const.UPDATE_NOTIFICATION_ID, mBuilder.build());
            }
        } catch (NameNotFoundException e) {
        }
	}

}

package com.inputstick.apps.kp2aplugin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.inputstick.api.hid.HIDKeycodes;

public class ClipboardService extends Service {
	
	private static final int NOTIFICATION_ID = 0;
	private static final int ACTION_DISABLE = 1;
	private static final int ACTION_EXTEND = 2;
	
	private static final int MAX_ALLOWED_TIME = 900; //max allowed duration
	private static final int MAX_EXTEND_TIME = 180; //duration can be extended by this period
	private static final int MAX_TEXT_LENGTH = 64;
	
	private int remainingTime;;
	
	private String layout;

	Handler delayhandler = new Handler();
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {		
			remainingTime--;
			if (remainingTime <= 0) {
				stopSelf();	
			} else {
				showNotification();
				delayhandler.postDelayed(mUpdateTimeTask, 1000);
			}			
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, R.string.text_clipboard_disabled, Toast.LENGTH_SHORT).show();
		cancelNotification();		
		delayhandler.removeCallbacksAndMessages(null);
		if (myClipBoard != null) {
			myClipBoard.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
			myClipBoard = null;
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int action = 0;				
		Bundle b = intent.getExtras();
		if (b != null) {
			action = b.getInt(Const.EXTRA_NOTIFICATION_ACTION, 0);
		}
		
		if (action == ACTION_DISABLE) {
			remainingTime = 0;
		} else if (action == ACTION_EXTEND) {
			remainingTime += MAX_EXTEND_TIME;	
			if (remainingTime > MAX_ALLOWED_TIME) {
				remainingTime = MAX_ALLOWED_TIME;
			}
			showNotification();		
		} else {
			layout = intent.getStringExtra(Const.EXTRA_LAYOUT);
			if (myClipBoard == null) {
				myClipBoard = (ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
				myClipBoard.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
			}		
			delayhandler.removeCallbacksAndMessages(null);
			delayhandler.postDelayed(mUpdateTimeTask, 1000);
			remainingTime = Const.CLIPBOARD_TIMEOUT_MS / 1000;
			Toast.makeText(this, R.string.text_clipboard_copy_now, Toast.LENGTH_LONG).show();
			showNotification();		
		}				
			
		return START_NOT_STICKY;
	}

	
	ClipboardManager myClipBoard ;
	ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
	    public void onPrimaryClipChanged() {
	        ClipData clipData = myClipBoard.getPrimaryClip();
	        if (clipData.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
	           String text = clipData.getItemAt(0).getText().toString();
	           if (text != null) {
	        	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ClipboardService.this);
	        	   UserPreferences userPrefs = ActionManager.getUserPrefs();
	        	   if ((text.length() > MAX_TEXT_LENGTH) && (prefs.getBoolean("clipboard_check_length", true))) {
	        		   Toast.makeText(ClipboardService.this, R.string.text_clipboard_too_long, Toast.LENGTH_LONG).show();
	        	   } else {
		        	   ActionManager.queueText(text, layout);
		        	   if (userPrefs.isClipboardAutoEnter()) {
		        		   ActionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);
		        	   }
		        	   if (userPrefs.isClipboardAutoDisable()) {
		        		   delayhandler.removeCallbacksAndMessages(null);
		        		   stopSelf();
		        	   }
	        	   }
	           }
	    	}
	    }
	};
	
	
	
	private void showNotification() {				
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

		mBuilder.setContentTitle(getString(R.string.app_name));		
		mBuilder.setContentText(getString(R.string.text_clipboard_notification_info) + " (" + remainingTime + ")");
		mBuilder.setSmallIcon(R.drawable.ic_notification);
				
		Intent disableActionIntent = new Intent(this, ClipboardService.class);
		disableActionIntent.putExtra(Const.EXTRA_NOTIFICATION_ACTION, ACTION_DISABLE);
		PendingIntent disableActionPendingIntent = PendingIntent.getService(this, ACTION_DISABLE, disableActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent extendActionIntent = new Intent(this, ClipboardService.class);
		extendActionIntent.putExtra(Const.EXTRA_NOTIFICATION_ACTION, ACTION_EXTEND);
		PendingIntent extendActionPendingIntent = PendingIntent.getService(this, ACTION_EXTEND, extendActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);		
		
		mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		mBuilder.addAction(0, getString(R.string.disable), disableActionPendingIntent);
		mBuilder.addAction(0, "+3min", extendActionPendingIntent);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());	
	}	

	private void cancelNotification() {
		NotificationManager nMgr = (NotificationManager)(getSystemService(Context.NOTIFICATION_SERVICE));
		nMgr.cancel(NOTIFICATION_ID);    	    	
    }
    
}

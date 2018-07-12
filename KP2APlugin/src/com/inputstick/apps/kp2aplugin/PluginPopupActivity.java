package com.inputstick.apps.kp2aplugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

public class PluginPopupActivity extends Activity {
	
	private static final int INTERVAL = 3000;
	private static final int MAX_TIME = 10 * 60 * 1000; //max keep alive time
		
	private boolean isSecure;
	private int totalTime;
	
	//keeps InputStick connection alive, keeps InputStickService alive (in case KP2A db is closed/locked)		
	private final Handler mHandler = new Handler();
	private final Runnable tick = new Runnable() {
	    public void run() {
	    	if (totalTime < MAX_TIME) {
		    	InputStickService.extendConnectionTime(INTERVAL);
		    	InputStickService.extendServiceKeepAliveTime(INTERVAL);
		    	totalTime += INTERVAL;
	    	}
	    	mHandler.postDelayed(this, INTERVAL);	    	
	    }
	};
	
	private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action != null) {				
				if (action.equals(Const.BROADCAST_FORCE_FINISH_SECURE)) {
					Toast.makeText(PluginPopupActivity.this, R.string.text_activity_closed, Toast.LENGTH_SHORT).show(); 
				}
			}
			
			finish();
		}
	};
	
	protected void setSecure() {
		isSecure = true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme(android.R.style.Theme_Holo_Dialog);
		if (isSecure) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		}
		
		if (savedInstanceState == null) {	
			totalTime = 0;
		} 
		
		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Const.BROADCAST_FORCE_FINISH_ALL);
		if (isSecure) {
			filter.addAction(Const.BROADCAST_FORCE_FINISH_SECURE);
		}
		registerReceiver(finishReceiver, filter);
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();			
		mHandler.post(tick);
	}
	
	@Override
	protected void onPause() {	
		mHandler.removeCallbacks(tick);	    
	    super.onPause();		
	}
	
	@Override
	protected void onDestroy() {		
		mHandler.removeCallbacks(tick);	   //when finish() is used onPause will not be called
		unregisterReceiver(finishReceiver);
		super.onDestroy();
	}

}

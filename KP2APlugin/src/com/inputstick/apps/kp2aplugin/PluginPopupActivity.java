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
	
	private static final int INTERVAL = 1000;
	private static final int MAX_KEEP_ALIVE_EXTENSION_TIME = 10 * 60 * 1000; //do not allow to increase keep alive time by more than this
	private static final int REMAINING_TIME_INITIAL_VALUE = 60 * 1000;
	private static final int REMAINING_TIME_DISPLAY_THRESHOLD = 15 * 1000; //display time left as title when below this value
		
	private boolean mProtectDisplayedContent;
	private boolean mHasEntryData;
	
	private boolean mCountdownEnabled;
	private int mRemainingTime;
	
	private int mTotalTime;
	
	//keeps InputStick connection alive, keeps InputStickService alive (in case KP2A db is closed/locked)		
	private final Handler mHandler = new Handler();
	private final Runnable tick = new Runnable() {
	    public void run() {
	    	if (mTotalTime < MAX_KEEP_ALIVE_EXTENSION_TIME) {
		    	InputStickService.extendConnectionTime(INTERVAL);
		    	InputStickService.extendServiceKeepAliveTime(INTERVAL);
		    	mTotalTime += INTERVAL;
	    	}
	    	
	    	if (mCountdownEnabled) {
		    	if (mRemainingTime <= 0) {
		    		finish();
		    	} else {
		    		mRemainingTime -= INTERVAL;
		    		updateTitle();
		    	}
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
					Toast.makeText(PluginPopupActivity.this, R.string.text_activity_closed, Toast.LENGTH_LONG).show(); 
				}
			}			
			finish();
		}
	};
	
	protected void setOptions(boolean protectDisplayedContent, boolean hasEntryData, boolean countdownEnabled) {
		mProtectDisplayedContent = protectDisplayedContent;
		mHasEntryData = hasEntryData;
		mCountdownEnabled = countdownEnabled;
	}
	
	private void updateTitle() {
		if (mRemainingTime < REMAINING_TIME_DISPLAY_THRESHOLD) {
			setTitle(getString(R.string.text_time_left) + " " + (mRemainingTime/1000) + "s");
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme(android.R.style.Theme_Holo_Dialog);
		if (mProtectDisplayedContent) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		}
		
		if (savedInstanceState == null) {				
			mTotalTime = 0;			
			if (mCountdownEnabled) {
				mRemainingTime = REMAINING_TIME_INITIAL_VALUE;
				updateTitle();
			}
		} 
		
		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Const.BROADCAST_FORCE_FINISH_ALL);
		if (mHasEntryData) {
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
